package com.fileupload.fileproject.service;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fileupload.fileproject.Exception.FileExpiredException;
import com.fileupload.fileproject.Exception.FileNotReadyException;
import com.fileupload.fileproject.entity.Files;

import com.fileupload.fileproject.enums.Status;
import com.fileupload.fileproject.repository.FilesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class FileService {


    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private FilesRepository fileRepo;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    @Transactional
    public Map<String,Object> uploadId(String fileName,String fileSize,String fileType)
    {
        try{

            String uniqueId = UUID.randomUUID().toString();
            String objectName = uniqueId + "_" + fileName;

            log.info("S3 Client Region: " + s3Client.getRegionName());
            log.info("Attempting to connect to bucket: " + bucketName);
            try {
                // This will throw an exception if credentials/region are wrong
                s3Client.headBucket(new HeadBucketRequest(bucketName));
                log.info("S3 Connection Verified: Bucket exists and is accessible.");
            } catch (AmazonServiceException e) {
                log.error("S3 Connection Failed: {}", e.getErrorMessage());
                if (e.getStatusCode() == 400) {
                    log.error("Check your REGION. Current config is likely mismatched.");
                }
                throw new RuntimeException("Could not connect to S3: " + e.getErrorMessage());
            }



            log.info("objectName is created = " + objectName);

            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, objectName);
            InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
            String uploadId = initResponse.getUploadId();

            log.info("uploadId is created = " + uploadId);
            Files file = new Files();
            file.setFileId(objectName);
            file.setFileName(fileName);
            file.setFileSize(fileSize);
            file.setFileType(fileType);
            file.setUploaded_at(new Date(System.currentTimeMillis()));
            file.setDownload_count(0);
            file.setStatus(Status.PENDING);

            fileRepo.save(file);


            long size = Long.parseLong(fileSize);
            long chunkSize = Math.max(5L * 1024 * 1024, (long) Math.ceil((double) size / 10000));
            double totalChunk = (double) size/ chunkSize;
            int partCount =(int) Math.ceil(totalChunk);



            Map<String, Object> response = new HashMap<>();
            response.put("uploadId", uploadId);
            response.put("s3Key",objectName);
            response.put("fileName", fileName);
            response.put("chunkSize",chunkSize);
            response.put("totalChunk", partCount);

            log.info("deftal of response after saving it in database = > " + response.toString());
            return response;

        }catch(Exception ex)
        {
            log.error("Error {}",ex);
            log.error("error in uploadId method {}", ex.getMessage());
            throw new InternalError("SomeThing Went Wrong !Please Try After Some Time");
        }
    }


    @Transactional                         // Srting partNumber, String uploadId
    public Map<String, Object> preSignedUrl(String partNumber,String uploadId,String s3Key)
    {
            log.info("preSignedUrl uploadId = " + uploadId);
            log.info("preSignedUrl partnumber = " + partNumber);
            log.info("presigneUrl s3key = " + s3Key);


              try{
                  Date expiration = new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000); // 1 hour


                      GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, s3Key)
                              .withMethod(HttpMethod.PUT)
                              .withExpiration(expiration);
                      request.addRequestParameter("partNumber", partNumber);
                      request.addRequestParameter("uploadId", uploadId);

                      URL url = s3Client.generatePresignedUrl(request);

                      log.info("preSigned Url recived = " + url);
                  Map<String, Object> response = new HashMap<>();
                  response.put("url", url.toString());
//                  response.put("expiration", expiration);

                  return response;


              }catch(Exception ex)
              {
                  log.error("Error {}", ex);
                  log.error("error in preSignedUrl methos {}" , ex.getMessage());
                  throw new RuntimeException("SomeThing Went Wrong");
              }
          }


          @Transactional
          public Map<String,Object> completeMultipartUpload(List<Map<String,Object>> etags,
                                              String s3Key,
                                              String uploadId) {
              try {
                  List<PartETag> pTagList = new ArrayList<>();

                  etags.stream().forEach((e) -> {
                      Integer partNumber = (Integer) e.get("partNumber");
                      String etag = (String) e.get("etag");
                      pTagList.add(new PartETag(partNumber, etag));
                  });

                  log.info("CompleteMultipartUpload etags size = " + etags.size());

                  pTagList.sort(Comparator.comparingInt(PartETag::getPartNumber));


                  CompleteMultipartUploadRequest
                      completeRequest = new CompleteMultipartUploadRequest(
                              bucketName,
                              s3Key,
                              uploadId,
                              pTagList
                      );






                      CompleteMultipartUploadResult result = s3Client.completeMultipartUpload(completeRequest);



                  System.out.println("file id  = " + s3Key);

                  Files file = fileRepo.findByFileId(s3Key);

                  if(file == null)
                  {
                      throw new InternalError("SomeThing went wrong! Please Try After some time");
                  }

                  // setFile Status as success
                  file.setStatus(Status.SUCCESS);

                  //set file expireed time

                  String downloadUrl = "http://localhost:3000/" + s3Key;

                  Map<String, Object> response = new HashMap<>();
                  response.put("s3Key", s3Key);
                  response.put("downloadUrl", downloadUrl);

                  return response;

              }catch(Exception ex)
              {
                  log.error("Error {}",ex);
                  log.error("error in mehtod completeMultipartUpload  = {}", ex.getMessage());

                  throw new RuntimeException("SomeThing Went Wrong");
              }
          }





     @Transactional
     public Map<String,Object> downloadFile(String s3Key)
     {

         try {

             Files file = fileRepo.findByFileId(s3Key);

             Status status = file.getStatus();
             if(status == Status.PENDING)
             {
                 throw new FileNotReadyException("File Is Not Ready To Download");
             }




             Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 60 * 1000);


             GeneratePresignedUrlRequest generatePresignedUrlRequest =
                     new GeneratePresignedUrlRequest(bucketName, s3Key)
                             .withMethod(HttpMethod.GET)
                             .withExpiration(expiration);

             URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

             Map<String, Object> response = new HashMap<>();
             response.put("downloadUrl", url.toString());
             response.put("s3Key", s3Key);
             response.put("expiresAt", expiration.toString());
             response.put("fileSize", file.getFileSize());
             response.put("fileType", file.getFileType());
             response.put("fileName", file.getFileName());



             file.setDownload_count(file.getDownload_count() + 1);

             return response;
         }catch(Exception e)
         {
             log.error("Error {}",e);

             log.error("error in download method = {}" , e.getMessage());
             throw new RuntimeException();
         }
     }
}
