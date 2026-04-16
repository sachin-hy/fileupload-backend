package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.requestDto.TenantAdminLoginDto;
import com.fileupload.fileproject.service.SecurityCustomService;
import com.fileupload.fileproject.util.CustomUserDetails;
import com.fileupload.fileproject.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TenantAdminLogin {


    private final AuthenticationManager authManager;

    private final SecurityCustomService securityCustomService;

    private final TenantRepository  tenantRepository;

    private final JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<?> loginTenantAdmin(@RequestBody TenantAdminLoginDto dto)
    {
          String email = dto.getEmail();
          String password = dto.getPassword();
          String subdomain = dto.getSubdomain();

          try{

              Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

              String jwt = "";

              CustomUserDetails userDetails = securityCustomService.loadUserByUsername(email);


              jwt = jwtUtil.generateToken(userDetails.getUsername(),
                      userDetails.getUserEntity().getTenant().getTenantKey(),
                      subdomain,
                      userDetails.getUserEntity().getRole().name(),
                      userDetails.getUserEntity().getTenant().getTenantid());

             return new ResponseEntity<>(jwt, HttpStatus.OK);
          }catch(Exception e)
          {
              return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
          }
    }
}
