package com.fileupload.fileproject.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileupload.fileproject.Config.SecurityConfiguration;
import com.fileupload.fileproject.Filter.JWTFilter;
import com.fileupload.fileproject.controller.FileController;
import com.fileupload.fileproject.requestDto.FileRequestDto;
import com.fileupload.fileproject.requestDto.PresignedUrlRequestDto;
import com.fileupload.fileproject.service.FileService;
import com.fileupload.fileproject.service.SecurityCustomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private SecurityCustomService securityCustomService;

    @MockitoBean
    private JWTFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testGetUploadId_Success() throws Exception {

        FileRequestDto fileRequestDto = new FileRequestDto();
        fileRequestDto.setFileName("test.jpg");
        fileRequestDto.setFileSize("9987");
        fileRequestDto.setFileType("jpg");

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("uploadId", "abcdid");
        expectedResponse.put("s3Key", "abcname");
        expectedResponse.put("fileName", "test.jpg");

        when(fileService.uploadId(any(), any(), any())).thenReturn(expectedResponse);

        mockMvc.perform(post("/uploadId")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fileRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testgetPresignedUrl_Success() throws Exception {
        String partNumber = "1";
        String uploadId = "testuploadid";
        String objectName = "testobjectname";

        PresignedUrlRequestDto presignedUrlRequestDto = new PresignedUrlRequestDto();
        presignedUrlRequestDto.setPartNumber(partNumber);
        presignedUrlRequestDto.setUploadId(uploadId);
        presignedUrlRequestDto.setObjectName(objectName);


        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("url", "testurl");

        when(fileService.preSignedUrl(anyString(),anyString(),anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post("/presignedurl")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(presignedUrlRequestDto)))
                .andExpect(status().isOk());
    }



    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void testcompleteMultipartUpload_Success() throws Exception {
         List<Map<String, Object>> etags = new ArrayList<>();

        Map<String, Object> etagPart1 = new HashMap<>();
        etagPart1.put("partNumber", 1);
        etagPart1.put("eTag", "a1b2c3d4e5f67890");
        etags.add(etagPart1);


         String s3Key = "tests3Key";
         String uploadId = "testUploadId";

         String expectedDownloadUrl = "testDownloadUrl";
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("s3Key", s3Key);
        expectedResponse.put("downloadUrl", expectedDownloadUrl);

        when(fileService.completeMultipartUpload(anyList(),anyString(),anyString())).thenReturn(expectedResponse);


         mockMvc.perform(post("/completeUpload")
                        .with(csrf())
                        .param("s3Key", s3Key)
                        .param("uploadId", uploadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(etags))
                )
                .andExpect(status().isOk());



    }




}
