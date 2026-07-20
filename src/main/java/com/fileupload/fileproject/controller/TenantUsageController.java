package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TenantUsageController {


    private final TenantService tenantService;

    TenantUsageController(TenantService tenantService){
        this.tenantService = tenantService;
    }


    @GetMapping("/private/storageDetails")
    public ResponseEntity<Map<String,Double>> getStorageDetails()
    {
        Long tenantId = TenantContext.getTenantId();

        return new ResponseEntity<>(tenantService.totalStorageDetails(tenantId), HttpStatus.OK);

    }
}
