package com.fileupload.fileproject.controller;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileupload.fileproject.requestDto.FileRequestDto;
import com.fileupload.fileproject.requestDto.PresignedUrlRequestDto;
import com.fileupload.fileproject.service.FileService;
import io.minio.*;
import io.minio.CreateMultipartUploadResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.CompleteMultipartUpload;
import io.minio.messages.ListMultipartUploadsResult;
import io.minio.messages.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.minio.messages.ListMultipartUploadsResult;


import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
public class FileController {

    @Autowired
   private FileService fileService;

    @PostMapping("/uploadId")
    public ResponseEntity<?> getUploadId(@RequestBody FileRequestDto fileRequestDto) {


           log.info("uploadid controller is called  ");
           Map<String,Object> response =fileService.uploadId(fileRequestDto.getFileName(),
                                                                 fileRequestDto.getFileSize(),
                                                                 fileRequestDto.getFileType());

           return new ResponseEntity<>(response, HttpStatus.OK);


    }

    @PostMapping("/presignedurl")
    public ResponseEntity<?> getPresignedUrl(@RequestBody PresignedUrlRequestDto requestDto)
    {

        Map<String,Object> response = fileService.preSignedUrl(requestDto.getPartNumber(),
                                                               requestDto.getUploadId(),
                                                               requestDto.getS3Key());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/completeUpload")
    public ResponseEntity<?> completeMultipartUpload(@RequestBody List<Map<String,Object>> etags,
                                                     @RequestParam("s3Key") String s3Key,
                                                     @RequestParam("uploadId") String uploadId)
    {

              log.info("completeMultipartUpload controller is called  " + etags.size());
              log.info("s3Key: controller =  " + s3Key);
              log.info("uploadId: controller =  " + uploadId);


              Map<String,Object> response = fileService.completeMultipartUpload(etags,s3Key,uploadId);
              return new ResponseEntity<>(response,HttpStatus.OK);
    }



    @GetMapping("/download/{s3Key}")
    public ResponseEntity<?> downloadFile(@PathVariable("s3Key") String s3Key)
    {
        System.out.println("S3kay = " + s3Key) ;
        Map<String,Object> response = fileService.downloadFile(s3Key);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }



}
