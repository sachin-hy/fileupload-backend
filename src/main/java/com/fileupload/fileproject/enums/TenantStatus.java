package com.fileupload.fileproject.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TenantStatus {

    ACTIVE("Active", "Tenant is active and operational"),
    SUSPENDED("Suspended", "Tenant is temporarily suspended"),
    INACTIVE("Inactive", "Tenant is inactive"),
    TRIAL("Trial", "Tenant is in trial period");

    private final String displayName;
    private final String description;

}
