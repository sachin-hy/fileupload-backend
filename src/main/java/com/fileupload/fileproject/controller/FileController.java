package com.fileupload.fileproject.controller;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.requestDto.FileRequestDto;
import com.fileupload.fileproject.requestDto.FileShareRequestDto;
import com.fileupload.fileproject.requestDto.PresignedUrlRequestDto;
import com.fileupload.fileproject.responseDto.RecentShareDto;
import com.fileupload.fileproject.responseDto.SharedFileDto;
import com.fileupload.fileproject.service.FileService;
import com.fileupload.fileproject.service.FileShareService;
import com.fileupload.fileproject.service.TenantService;
import io.minio.*;
import io.minio.CreateMultipartUploadResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.CompleteMultipartUpload;
import io.minio.messages.ListMultipartUploadsResult;
import io.minio.messages.Upload;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api/private")
@AllArgsConstructor
public class FileController {

    private final FileService fileService;
    private final TenantService tenantService;
    private final FileShareService fileShareService;



    private String getClientIp(HttpServletRequest request)
    {

        String xfheader = request.getHeader("X-Forwarded-For");

        if(xfheader == null)
        {
            return request.getRemoteAddr();
        }

        return xfheader.split(",")[0];
    }

    @PostMapping("/files/initiate")
    public ResponseEntity<?> initiateFileUpload(@RequestBody FileRequestDto fileRequestDto,
                                         HttpServletRequest request) {


           log.info("initiate file upload");
           String ip = getClientIp(request);

           Map<String,Object> response = fileService.uploadId(fileRequestDto.getFileName(),
                                                                 fileRequestDto.getFileSize(),
                                                                 fileRequestDto.getFileType(),
                                                                  ip);

           return new ResponseEntity<>(response, HttpStatus.OK);


    }


    @PostMapping("/files/presigned-url")
    public ResponseEntity<?> generatePresignedUrl(@RequestBody PresignedUrlRequestDto requestDto)
    {

        Map<String,Object> response = fileService.preSignedUrl(requestDto.getPartNumber(),
                                                               requestDto.getUploadId(),
                                                               requestDto.getS3Key());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PostMapping("/files/complete")
    public ResponseEntity<?> completeMultipartUpload(@RequestBody List<Map<String,Object>> etags,
                                                     @RequestParam("s3Key") String s3Key,
                                                     @RequestParam("uploadId") String uploadId,
                                                     HttpServletRequest request)
    {

              log.info("completeMultipartUpload controller is called  " + etags.size());
              log.info("s3Key: controller =  " + s3Key);
              log.info("uploadId: controller =  " + uploadId);

              String ip = getClientIp(request);

              Map<String,Object> response = fileService.completeMultipartUpload(etags,s3Key,uploadId,ip);
              return new ResponseEntity<>(response, HttpStatus.OK);
    }




    @GetMapping("/files")
    public ResponseEntity<?> returnAllFilesByTenantId()
    {
        List<Map<String,Object>> result = fileService.getTenantFileList();

        return new ResponseEntity<>(result,HttpStatus.OK);
    }



    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileId") String fileId,
                                           HttpServletRequest request)
    {

        String ip = getClientIp(request);

        Map<String,Object> response = fileService.downloadFile(Long.parseLong(fileId),ip);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }



    @GetMapping("/shares/me")
    public ResponseEntity<?> getFilesSharedWithMe()
    {
        List<SharedFileDto> result = fileShareService.getFilesSharedWithMe();

        return new ResponseEntity<>(result,HttpStatus.OK);

    }

    /* get info of all the files in organisation*/
    @GetMapping("/share")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<?> getFileShare()
    {
        List<RecentShareDto> result = fileShareService.getFilesShareInfo();

        return new ResponseEntity<>(result,HttpStatus.OK);

    }


    // share the uploaded file with others



    @GetMapping("/recent-activity")
    public ResponseEntity<?> recentActivity()
    {
         String email = SecurityContextHolder.getContext().getAuthentication().getName();
          List<RecentShareDto> result = fileShareService.recentActivity(email);
          return ResponseEntity.ok(result);
    }


    @GetMapping("/users/count")
    public ResponseEntity<?> totalUsers()
    {
         Long tenantId = TenantContext.getTenantId();
         return new ResponseEntity<> (tenantService.totalUsers(tenantId),HttpStatus.OK);
    }

    @GetMapping("/files/count")
    public ResponseEntity<?> totalFiles(){
        Long tenantId = TenantContext.getTenantId();

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ResponseEntity<>(tenantService.totalFiles(email,tenantId),HttpStatus.OK);
    }

    // fetch total users and files in a tenant

    @GetMapping("/users/files/count")
    public ResponseEntity<?> totalUsersAndFiles(){

        Long tenantId = TenantContext.getTenantId();

        return new ResponseEntity<>(tenantService.totalUsersAndFiles(tenantId),HttpStatus.OK);
    }


}
