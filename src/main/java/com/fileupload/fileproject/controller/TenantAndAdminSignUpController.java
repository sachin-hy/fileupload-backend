package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.requestDto.TenantRegistrationDto;
import com.fileupload.fileproject.service.TenantService;
import com.fileupload.fileproject.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Data
@RequestMapping("/api")
@AllArgsConstructor
public class TenantAndAdminSignUpController {

    private final TenantService tenantService;
    private final JwtUtil jwtUtil;

    private String getClientIp(HttpServletRequest request)
    {

        String xfheader = request.getHeader("X-Forwarded-For");

        if(xfheader == null)
        {
            return request.getRemoteAddr();
        }

        return xfheader.split(",")[0];
    }


    @PostMapping("/public/register/tenant")
    public ResponseEntity<?> registerTenant( @RequestBody TenantRegistrationDto dto,
                                             HttpServletRequest request)
    {

         String ip = getClientIp(request);

         Users tenantAdmin = tenantService.register(dto,ip);

//         String jwtToken =  jwtUtil.generateToken(tenantAdmin.getEmail(),
//                                  tenantAdmin.getTenant().getTenantKey(),
//                                 tenantAdmin.getTenant().getSubdomain(),
//                                 tenantAdmin.getRole().toString(),
//                                 tenantAdmin.getTenant().getTenantid());

         return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
