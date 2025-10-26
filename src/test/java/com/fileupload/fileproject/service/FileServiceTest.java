package com.fileupload.fileproject.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fileupload.fileproject.entity.Files;
import com.fileupload.fileproject.enums.Status;
import com.fileupload.fileproject.repository.FilesRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private FilesRepository filesRepository;


    @Mock
    private AmazonS3 s3Client;


    @InjectMocks
    private FileService fileService;


    @Test
    public void testuploadId_Success()
    {

        String fileName = "test.mov";
        String fileSize = "10485760";
        String fileType = "video/quicktime";


        InitiateMultipartUploadResult mockS3Result = new InitiateMultipartUploadResult();
        String expectedUploadId = "test-upload-id-123";

        mockS3Result.setUploadId(expectedUploadId);

        when(s3Client.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class)))
                .thenReturn(mockS3Result);

        when(filesRepository.save(any(Files.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Map<String,Object> result = fileService.uploadId(fileName, fileSize, fileType);

        ArgumentCaptor<Files> fileCaptor = ArgumentCaptor.forClass(Files.class);
        verify(filesRepository).save(fileCaptor.capture());

        Files capturedFile  = fileCaptor.getValue();

        assertTrue(result.containsKey("uploadId"));
        assertEquals(expectedUploadId, result.get("uploadId"));
        assertTrue(result.containsKey("s3Key"));
        assertEquals(capturedFile.getFileName(), result.get("fileName"));
        assertTrue(result.containsKey("chunkSize"));
        assertTrue(result.containsKey("totalChunk"));

    }


    @Test
    public void testuploadId_Fail(){

        String fileName = "test.mov";
        String fileSize = "10485760";
        String fileType = "video/quicktime";

        InitiateMultipartUploadResult mockS3Result = new InitiateMultipartUploadResult();

        String expectedUploadId = "test-upload-id-123";
        mockS3Result.setUploadId(expectedUploadId);

        when(s3Client.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class))).thenThrow(new InternalError("SomeThing Went Wrong"));


      //  when(filesRepository.save(any(Files.class))).thenAnswer( invocation ->  invocation.getArgument(0));

//        Map<String,Object> result = fileService.uploadId(fileName, fileSize, fileType);

        InternalError exception = assertThrows(InternalError.class, ()->
        {
            fileService.uploadId(fileName, fileSize, fileType);
        });

        verify(filesRepository,never()).save(any(Files.class));

    }


    @Test
    public void testpreSignedUrl_Success() throws MalformedURLException {
        String partNumber = "1";
        String uploadId = "test-upload-id-123";
        String objectName = "demoobjectName";

        String presignedUrl = "https://presigned-url.com/upload";
        URL url  = new URL(presignedUrl);

        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(url);


        Map<String,Object> result = fileService.preSignedUrl(partNumber,uploadId,objectName);




        ArgumentCaptor<GeneratePresignedUrlRequest>  requestCaptor = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(s3Client).generatePresignedUrl(requestCaptor.capture());

        GeneratePresignedUrlRequest generatePresignedUrlRequest = requestCaptor.getValue();

        assertEquals(partNumber,generatePresignedUrlRequest.getRequestParameters().get("partNumber"));
        assertEquals(uploadId,generatePresignedUrlRequest.getRequestParameters().get("uploadId"));
        assertEquals(presignedUrl, result.get("url"));
        assertEquals(objectName, generatePresignedUrlRequest.getKey());
    }

    @Test
    public void testpreSignedUrl_Fail()
    {
        String partNumber = "1";
        String uploadId = "test-upload-id-123";
        String objectName = "demoobjectName";

        String presignedUrl = "https://presigned-url.com/upload";

        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenThrow(new RuntimeException("SomeThing Went Wrong"));

        RuntimeException exception = assertThrows(RuntimeException.class, ()->{
            fileService.preSignedUrl(partNumber,uploadId,objectName);
        });

        assertEquals("SomeThing Went Wrong",exception.getMessage());


    }


    @Test
    public void testcompleteMultipartUpload_Success()
    {
        List<Map<String, Object>> etags = new ArrayList<>();
        Map<String, Object> etag1 = new HashMap<>();
        etag1.put("etag", "abcd");
        etag1.put("partNumber", Integer.valueOf(1));
        etags.add(etag1);
        Map<String, Object> etag2 = new HashMap<>();
        etag2.put("etag", "dcba");
        etag2.put("partNumber", Integer.valueOf(2));
        etags.add(etag2);

        String s3Key = "demoS3Key";
        String uploadId =  "test-upload-id-123";

        CompleteMultipartUploadResult completeMultipartUploadResult = new CompleteMultipartUploadResult();



        Files mockFile = new Files();
        mockFile.setFileId(s3Key);
        mockFile.setStatus(Status.PENDING);



        when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class))).thenReturn(completeMultipartUploadResult);

        when(filesRepository.findByFileId(s3Key)).thenReturn(mockFile);

        String downloadUrl = "http://localhost:3000/" + s3Key;

        Map<String, Object> result = fileService.completeMultipartUpload(etags,s3Key,uploadId);

        ArgumentCaptor<CompleteMultipartUploadRequest> requestCaptor = ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);
        verify(s3Client).completeMultipartUpload(requestCaptor.capture());
        CompleteMultipartUploadRequest completeMultipartUploadRequest = requestCaptor.getValue();

        assertEquals(s3Key, result.get("s3Key"));
        assertEquals(uploadId, completeMultipartUploadRequest.getUploadId());
        assertEquals(downloadUrl, result.get("downloadUrl"));


    }

    @Test
    public void testcompleteMultipartUpload_Fail()
    {
        List<Map<String, Object>> etags = new ArrayList<>();
        Map<String, Object> etag1 = new HashMap<>();
        etag1.put("etag", "abcd");
        etag1.put("partNumber", Integer.valueOf(1));
        etags.add(etag1);
        Map<String, Object> etag2 = new HashMap<>();
        etag2.put("etag", "dcba");
        etag2.put("partNumber", Integer.valueOf(2));
        etags.add(etag2);

        String s3Key = "demoS3Key";

        String uploadId =  "test-upload-id-123";
        String downloadUrl = "http://localhost:3000/" + s3Key;

        CompleteMultipartUploadResult completeMultipartUploadResult = new CompleteMultipartUploadResult();

        when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class))).thenThrow(new RuntimeException("SomeThing Went Wrong"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
        {
           fileService.completeMultipartUpload(etags,s3Key,uploadId);
        });

        assertEquals("SomeThing Went Wrong",exception.getMessage());


    }

}


