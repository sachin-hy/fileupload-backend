package com.fileupload.fileproject.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    TENANT_ADMIN("Tenant Admin", "Full access to tenant resources"),

    MEMBER("Member", "Standard user access"),
    VIEWER("Viewer", "Read-only access");

    private final String displayName;
    private final String description;

    public boolean hasAdminPrivileges() {
        return this == TENANT_ADMIN ;
    }
}
