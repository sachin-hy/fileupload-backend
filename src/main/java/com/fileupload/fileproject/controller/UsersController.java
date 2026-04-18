package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.service.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> findAll()
    {
         List<Map<String, Object>> result =  usersService.findAll();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
