package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.context.LookupContext;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.AuditAction;
import com.fileupload.fileproject.repository.TenantLookupRepository;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.requestDto.TenantAdminLoginDto;
import com.fileupload.fileproject.responseDto.LoginResponseDto;
import com.fileupload.fileproject.responseDto.TenantSummaryDto;
import com.fileupload.fileproject.responseDto.UserSummaryDto;
import com.fileupload.fileproject.service.AuditLogService;
import com.fileupload.fileproject.service.SecurityCustomService;
import com.fileupload.fileproject.util.CustomUserDetails;
import com.fileupload.fileproject.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@AllArgsConstructor
public class LoginController {


    private final AuthenticationManager authManager;

    private final SecurityCustomService securityCustomService;

    private final TenantRepository  tenantRepository;

    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final TenantLookupRepository tenantLookupRepository;



    private String getClientIp(HttpServletRequest request)
    {

        String xfheader = request.getHeader("X-Forwarded-For");

        if(xfheader == null)
        {
            return request.getRemoteAddr();
        }

        return xfheader.split(",")[0];
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginMethod(@RequestBody TenantAdminLoginDto dto,
                                              HttpServletRequest request)
    {
          String email = dto.getEmail();
          String password = dto.getPassword();

          String subdomain = LookupContext.getSubdomain();

          Long tenantId = tenantLookupRepository.findByEmailAndSubdomain(email,subdomain).getTenantId();


          String ip = getClientIp(request);

          try{

              Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

              String jwt = "";

              CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

              Users user = userDetails.getUserEntity();

              jwt = jwtUtil.generateToken(
                      user.getId(),
                      user.getEmail(),
                      user.getTenant().getTenantKey(),
                      user.getTenant().getSubdomain(),
                      user.getRole().name(),
                      user.getTenant().getTenantid()
              );


              auditLogService.log(user.getTenant().getTenantid(), email, AuditAction.USER_LOGIN_SUCCESS, ip, "Successful login", null);


              UserSummaryDto userDto = new UserSummaryDto(
                      user.getId(),
                      user.getEmail(),
                      user.getFirstName(),
                      user.getLastName(),
                      user.getRole().name()
              );

              TenantSummaryDto tenantDto = new TenantSummaryDto(
                      user.getTenant().getTenantid(),
                      user.getTenant().getOrganisationName(),
                      user.getTenant().getSubdomain(),
                      user.getTenant().getSubscriptionEndsAt(),
                      user.getTenant().getPlanType()
              );

              LoginResponseDto response =
                      new LoginResponseDto(jwt, userDto, tenantDto);

              return new ResponseEntity<>(response, HttpStatus.OK);


          }catch(Exception e)
          {
              System.out.println(e);
              auditLogService.log(tenantId, null, AuditAction.USER_LOGIN_FAILED, ip, null, "Login failed for: " + email);
              return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
          }
    }
}
