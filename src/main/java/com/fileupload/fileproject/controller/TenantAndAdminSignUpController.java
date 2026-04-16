package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.requestDto.TenantRegistrationDto;
import com.fileupload.fileproject.service.TenantService;
import com.fileupload.fileproject.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Data
@AllArgsConstructor
public class TenantAndAdminSignUpController {

    private final TenantService tenantService;
    private final JwtUtil jwtUtil;


    @PostMapping("/register")
    public ResponseEntity<?> registerTenant( @RequestBody TenantRegistrationDto dto)
    {
         Users tenantAdmin = tenantService.register(dto);

         String jwtToken =  jwtUtil.generateToken(tenantAdmin.getEmail(),
                                  tenantAdmin.getTenant().getTenantKey(),
                                 tenantAdmin.getTenant().getSubdomain(),
                                 tenantAdmin.getRole().toString(),
                                 tenantAdmin.getTenant().getTenantid());

         return new ResponseEntity<>(jwtToken, HttpStatus.CREATED);
    }
}
