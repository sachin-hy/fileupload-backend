package com.fileupload.fileproject.service;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import com.fileupload.fileproject.Exception.FileNotReadyException;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.FileMetadata;

import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.TenantUsage;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.AuditAction;
import com.fileupload.fileproject.enums.UploadStatus;
import com.fileupload.fileproject.repository.FileMetadataRepository;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.TenantUsageRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.util.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final TenantRepository tenantRepo;

    private final TenantUsageRepository tenantUsageRepo;

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final FileMetadataRepository fileMetadataRepo;

    private final AuditLogService auditLogService;

    private final UsersRepository usersRepo;


    @Transactional
    public FileMetadata checkTenantSizeLimit(Long fileSize,String fileName,String fileType,String ip)
    {
        Long tenantId = TenantContext.getTenantId();

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Users currentUser = usersRepo.findByEmailAndTenant_Tenantid(email,tenantId).orElseThrow(() -> new UsernameNotFoundException("No user found"));

//        Users currentUser = ((CustomUserDetails) SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getPrincipal())
//                .getUserEntity();


        Tenant tenant = tenantRepo.findById(tenantId).orElseThrow(() -> new RuntimeException("No Tenant present"));

        Long totalStorage = tenant.getStorageQuotaGB()*1024L*1024L*1024L;

        //acquring lock on the current tenantusage row
        TenantUsage tenantUsage = tenantUsageRepo.findAndLockByTenantId(tenantId).orElseThrow(() -> new RuntimeException("No TenantUsage present"));

        Long usedStorage = tenantUsage.getUsedStorageGB()*1024L*1024L*1024L;

        // get the pending file size which are currently uploading by other user in same
        // organisation
        Long pendingFileSize = fileMetadataRepo.getSumOfFileSizesByTenantAndStatus(tenantId,UploadStatus.INITIATED);

        /* check the total storage did not exced the plan limit*/

        if(usedStorage + fileSize + pendingFileSize > totalStorage)
        {
            throw new RuntimeException("Storage Limit Exceeded");
        }

        FileMetadata file = FileMetadata.builder()
                .uploadedBy(currentUser)
                .fileName(fileName)
                .originalFileName(fileName)
                .fileSize(fileSize)
                .contentType(fileType)
                .uploadStatus(UploadStatus.INITIATED)
                .tenant(tenant)
                .bucketName(bucketName)
                .downloadCount(0)
                .isDeleted(false)
                .build();

        try {
            auditLogService.log(tenantId, currentUser.getEmail(), AuditAction.UPLOAD_INITIATED, ip, "Upload Initiated by current user", null);
        } catch (Exception e) {
            log.error("Failed to audit log UPLOAD_INITIATED: {}", e.getMessage());
        }
        return file;
    }




    @Transactional
    public Map<String,Object> uploadId(String fileName,String fileSize,String fileType,String ip) {


        Long tenantId = TenantContext.getTenantId();

        Long newfilesize = Long.parseLong(fileSize);

        FileMetadata file = checkTenantSizeLimit(newfilesize,fileName,fileType,ip);

        try{

            String uniqueId = UUID.randomUUID().toString();
            // generating the s3key for the file
            String objectName = TenantContext.getTenantKey() + "/"  + uniqueId + "_" + fileName;

            log.info("S3 Client Region: " + s3Client.getRegionName());
            log.info("Attempting to connect to bucket: " + bucketName);
            try {

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


            file.setStorageKey(objectName);
            file.setUploadId(uploadId);
            fileMetadataRepo.save(file);


            long size = Long.parseLong(fileSize);
            long chunkSize = Math.max(5L * 1024L * 1024L, (long) Math.ceil((double) size / 10000));
            double totalChunk = (double) size/ chunkSize;
            int partCount =(int) Math.ceil(totalChunk);



            Map<String, Object> response = new HashMap<>();
            response.put("uploadId", uploadId);
            // s3key is used for download and locate the file
            response.put("s3Key",objectName);
            response.put("fileName", fileName);
            response.put("chunkSize",chunkSize);
            response.put("totalChunk", partCount);


             return response;

        }catch(Exception ex)
        {
            log.error("error in uploadId method {}", ex.getMessage());

            try {
                auditLogService.log(TenantContext.getTenantId(), null, AuditAction.UPLOAD_FAILED, ip, null ,ex.getMessage());
            } catch (Exception e) {
                log.error("Failed to audit log UPLOAD_FAILED: {}", e.getMessage());
            }


            fileMetadataRepo.delete(file);
            throw new InternalError("SomeThing Went Wrong !Please Try After Some Time");
        }
    }




                           // Srting partNumber, String uploadId
    public Map<String, Object> preSignedUrl(String partNumber,String uploadId,String s3Key)
    {
            log.info("preSignedUrl uploadId = " + uploadId);
            log.info("preSignedUrl partnumber = " + partNumber);
            log.info("presigneUrl s3key = " + s3Key);


            String currentTenantKey = TenantContext.getTenantKey();

            if (!s3Key.startsWith(currentTenantKey + "/")) {
                throw new RuntimeException("Access Denied");
            }

            try{
                   Date expiration = new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000); // 1 hour


                   // pass the bucket name and s3key
                    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, s3Key)
                              .withMethod(HttpMethod.PUT)
                              .withExpiration(expiration);
                    //tell the part number or chunk number of the file
                    request.addRequestParameter("partNumber", partNumber);
                    // tell the upload id of the file
                    request.addRequestParameter("uploadId", uploadId);


                    // generating the url for upload
                    URL presignedUrl = s3Client.generatePresignedUrl(request);

                    String url = presignedUrl.toString()
                        .replace("http://minio:9000",
                                "http://localhost:9000");

                     Map<String, Object> response = new HashMap<>();
                     response.put("url", url.toString());

                     return response;


            }catch(Exception ex)
            {
                log.error("error in preSignedUrl methos {}" , ex.getMessage());
                throw new RuntimeException("SomeThing Went Wrong");
            }
          }





          // after all the oarts have been uploaded we need to complete the upload part
          public Map<String,Object> completeMultipartUpload(List<Map<String,Object>> etags,
                                              String s3Key,
                                              String uploadId,
                                              String ip)
          {

              try {
                  List<PartETag> pTagList = new ArrayList<>();

                  // filter the etags
                  etags.stream().forEach((e) -> {
                      Integer partNumber = (Integer) e.get("partNumber");
                      String etag = (String) e.get("etag");
                      pTagList.add(new PartETag(partNumber, etag));
                  });

                  log.info("CompleteMultipartUpload etags size = " + etags.size());
                  // sort the etags by there partnumber
                  pTagList.sort(Comparator.comparingInt(PartETag::getPartNumber));

                  CompleteMultipartUploadRequest
                      completeRequest = new CompleteMultipartUploadRequest(
                              bucketName,
                              s3Key,
                              uploadId,
                              pTagList
                      );

                  s3Client.completeMultipartUpload(completeRequest);

                  auditLogService.log(TenantContext.getTenantId(), null, AuditAction.UPLOAD_COMPLETED, ip, "File upload completed",null);

                 return updateDatabaseAfterComplition(s3Key);

              }catch(Exception ex)
              {

                  auditLogService.log(TenantContext.getTenantId(), null, AuditAction.UPLOAD_FAILED, ip,null, ex.getMessage());
                  log.error("error in mehtod completeMultipartUpload  = {}", ex.getMessage());
                  throw new RuntimeException("SomeThing Went Wrong");
              }
          }

    @Transactional
    private Map<String, Object> updateDatabaseAfterComplition(String s3Key) {

        Long tenantid = TenantContext.getTenantId();


        FileMetadata file = fileMetadataRepo.findByStorageKeyAndTenant_Tenantid(s3Key,tenantid);

        TenantUsage tenantUsage = tenantUsageRepo.findByTenant_Tenantid(tenantid);

        tenantUsage.addStorage(file.getFileSize());
        tenantUsage.incrementFileCount();
        tenantUsage.incrementTotalUploads();

        file.setUploadStatus(UploadStatus.COMPLETED);

        fileMetadataRepo.save(file);
        tenantUsageRepo.save(tenantUsage);

        Map<String, Object> response = new HashMap<>();
        response.put("fileId", file.getId());

        System.out.println("file id " + file.getId());
        return response;

}



    public List<Map<String, Object>> getTenantFileList(){

        Long tenantId = TenantContext.getTenantId();

        return fileMetadataRepo.findByTenant_TenantidAndUploadStatus(tenantId, UploadStatus.COMPLETED)
                .stream().map(file -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", file.getId());
                    map.put("name", file.getOriginalFileName());
                    map.put("size", file.getFileSize());
                    map.put("uploadedBy", file.getUploadedBy().getFullName());
                    map.put("createdAt", file.getCreatedAt());
                    return map;
                }).toList();
    }

     @Transactional
     public Map<String,Object> downloadFile(Long fileId, String ip)
     {

         Long tenantId = TenantContext.getTenantId();
         Tenant tenant = tenantRepo.findById(tenantId).orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));

         FileMetadata file = fileMetadataRepo.findByIdAndTenant(fileId,tenant).orElseThrow(() -> new EntityNotFoundException("Access Denied: You do not have permission to download this file."));

         try {
             String s3Key = file.getStorageKey();

             UploadStatus status = file.getUploadStatus();

             if (file.getUploadStatus() != UploadStatus.COMPLETED) {
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
             response.put("fileName" , file.getOriginalFileName());
             response.put("fileSize" , file.getFileSize());

             file.setDownloadCount(file.getDownloadCount() + 1);

             fileMetadataRepo.save(file);

             auditLogService.log(file.getTenant().getTenantid(), null, AuditAction.FILE_DOWNLOADED, ip, "file downloaded" ,null);

             return response;

         }catch(Exception e)
         {
             auditLogService.log(file.getTenant().getTenantid(), null, AuditAction.DOWNLOAD_FAILED, ip, null , e.getMessage());
             log.error("error in download method = {}" , e.getMessage());
             throw new RuntimeException();
         }
     }
}
