package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.requestDto.GoogleRequestDto;
import com.fileupload.fileproject.service.GoogleService;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class GoogleController {


    @Autowired
    private GoogleService googleService;

    @RequestMapping("/oauth2/callback/google")
    public ResponseEntity<?> handleGoogle(@RequestBody GoogleRequestDto googleRequestDto, HttpServletResponse response)
    {
        try{

            String token = googleService.handleGoogleCallback(googleRequestDto.getCode());
            log.info("token is created = {}" , token );

            Map<String,Object> map = new HashMap<>();
            map.put("token",token);

            return new ResponseEntity<>(map, HttpStatus.OK);

        }catch(Exception ex)
        {
            log.error("Error {} ", ex);
            log.error("error in handleGoogle controller method {} ", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());

        }

    }
}
