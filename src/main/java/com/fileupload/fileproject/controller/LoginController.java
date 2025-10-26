package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.requestDto.LoginRequestDto;
import com.fileupload.fileproject.responseDto.LoginResponseDto;
import com.fileupload.fileproject.service.SecurityCustomService;
import com.fileupload.fileproject.service.UserService;
import com.fileupload.fileproject.util.JwtUtil;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Slf4j
@RestController
public class LoginController {

    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private SecurityCustomService securityCustomService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response)
    {

        String email	 = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        try {


            Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));

            String jwt = "";


            UserDetails userDetails = securityCustomService.loadUserByUsername(email);

            jwt = jwtUtil.generateToken(userDetails.getUsername());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);


            Map<String,Object> map = new HashMap<>();
            map.put("token",jwt);

            log.info("login controller = {}", jwt);
            return new ResponseEntity<>(map, HttpStatus.OK);

        }catch(Exception e)
        {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

    }

}
