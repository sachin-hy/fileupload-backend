package com.fileupload.fileproject.controller;

import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.requestDto.RegisterRequestDto;
import com.fileupload.fileproject.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class SignUpController {


    @Autowired
    private UserService usersService;

    @RequestMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody RegisterRequestDto registerRequest)
    {



        try {

            Users user = usersService.saveUser(registerRequest);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }catch(Exception ex)
        {
            log.error("Error {}", ex);
            log.error("error in signup method controller {}",ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
