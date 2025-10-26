package com.fileupload.fileproject.service;


import com.fileupload.fileproject.Exception.InternalError;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.repository.UserRepository;
import com.fileupload.fileproject.responseDto.LoginResponseDto;
import com.fileupload.fileproject.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class GoogleService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Autowired
    private WebClient webClient;

    @Value("${spring.tokenEndPoint}")
    private String tokenEndpoint;

    @Value("${spring.userInfoEndpoint}")
    private String userInfoEndpoint;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityCustomService securityCustomService;

    @Autowired
    private JwtUtil jwtUtil;




    @Transactional
    public String handleGoogleCallback(String code)
    {
        try{

            log.info("google callback function is called  ");
            Map<String, Object> tokenResponse = webClient.post()
                        .uri(tokenEndpoint)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .body(BodyInserters
                                .fromFormData("code", code)
                                .with("client_id", clientId)
                                .with("client_secret", clientSecret)
                                .with("redirect_uri", redirectUri)
                                .with("grant_type", "authorization_code")
                        )
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();



            if (tokenResponse == null || !tokenResponse.containsKey("id_token")) {
                throw new InternalError("SomeThing Went Wrong! Try After SomeTime");
            }

            String accessToken = tokenResponse.get("access_token").toString();
            String idToken = tokenResponse.get("id_token").toString();

           userInfoEndpoint = userInfoEndpoint +  idToken;

            Map<String, Object> userInfo = webClient.get()
                        .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(Map.class)
                         .block();


            if (userInfo == null || !userInfo.containsKey("email")) {
                throw new InternalError("SomeThing Went Wrong! Try After SomeTime");
            }


            String token = createToken(userInfo.get("email").toString(),userInfo.get("name").toString());


             return token;
        }catch(Exception ex)
        {
            log.error("Error {} ", ex);
             log.error("Error in handleGoogleCallback {}  : {}", ex , ex.getMessage());
             throw new InternalError("SomeThing Went Wring try Again");
        }
    }


    @Transactional
    public String createToken(String email,String name)
    {
        Optional<Users> user = userRepository.findByEmail(email);


        if(user.isEmpty()) {
            Users u = new Users();
            u.setEmail(email);
            u.setFirstName(name);
            u.setPassword(passwordEncoder.encode(name));
            u.setLastName(name);
            userRepository.save(u);

        }

        UserDetails userDetails = securityCustomService.loadUserByUsername(email);

        String token = jwtUtil.generateToken(userDetails.getUsername());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails,null,userDetails.getAuthorities());
       SecurityContextHolder.getContext().setAuthentication(auth);

        return token;

    }
}
