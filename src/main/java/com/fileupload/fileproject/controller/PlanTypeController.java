package com.fileupload.fileproject.controller;

import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.requestDto.PlanUpdateRequestDto;
import com.fileupload.fileproject.service.PlanTypeUpdateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class PlanTypeController {

    private final PlanTypeUpdateService planTypeUpdateService;


    private String getClientIp(HttpServletRequest request)
    {
        String xfheader = request.getHeader("X-Forwarded-For");

        if (xfheader == null) {
            return request.getRemoteAddr();
        }

        return  xfheader.split(",")[0];
    }


//    @PostMapping("/private/plans/update")
//
//    public ResponseEntity<?> updatePlanType(@RequestBody PlanUpdateRequestDto dto,
//                                            HttpServletRequest request)
//    {
//         String ip = getClientIp(request);
//        Long tenantId = TenantContext.getTenantId();
//         planTypeUpdateService.updatePlan(dto,ip, tenantId);
//         return ResponseEntity.ok("Plan updated successfully");
//    }





    @GetMapping("/public/plans")
    public ResponseEntity<?>  getPublicPlans(HttpServletRequest request){
        return new ResponseEntity<>(planTypeUpdateService.getFrontendPlans(), HttpStatus.OK);
    }

    @PutMapping("/private/tenant/upgrade")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> upgradePlan(@RequestBody PlanUpdateRequestDto planUpdateRequestDto,
                                          HttpServletRequest request)
    {
        String ip = getClientIp(request);
        Long tenantId = TenantContext.getTenantId();

        planTypeUpdateService.updatePlan(planUpdateRequestDto, ip, tenantId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
