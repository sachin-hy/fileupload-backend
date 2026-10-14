package com.fileupload.fileproject.support;

import com.fileupload.fileproject.context.LookupContext;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.context.UserIDContext;
import com.fileupload.fileproject.entity.FileMetadata;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.PlanType;
import com.fileupload.fileproject.enums.TenantStatus;
import com.fileupload.fileproject.enums.UploadStatus;
import com.fileupload.fileproject.enums.UserRole;
import com.fileupload.fileproject.enums.UserStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;


public final class TestData {

    public static final Long TENANT_ID = 1L;
    public static final String TENANT_KEY = "TENANT_KEY_1";
    public static final String SUBDOMAIN = "acme.example.com";
    public static final String USER_EMAIL = "alice@acme.com";

    private TestData() {
    }

    public static Tenant tenant(PlanType plan) {
        Tenant tenant = new Tenant();
        tenant.setTenantid(TENANT_ID);
        tenant.setTenantKey(TENANT_KEY);
        tenant.setSubdomain(SUBDOMAIN);
        tenant.setOrganisationName("Acme");
        tenant.setAdminEmail("admin@acme.com");
        tenant.setPlanType(plan);
        tenant.setStorageQuotaGB(plan.getStorageQuotaGB());
        tenant.setMaxUsers(plan.getMaxUsers());
        tenant.setStatus(TenantStatus.ACTIVE);
        return tenant;
    }

    public static Users user(Long id, String email, Tenant tenant, UserRole role) {
        return Users.builder()
                .id(id)
                .email(email)
                .firstName("First" + id)
                .lastName("Last" + id)
                .passwordHash("hash")
                .tenant(tenant)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static FileMetadata file(Long id, Tenant tenant, Users owner, UploadStatus status, long size) {
        FileMetadata file = new FileMetadata();
        file.setId(id);
        file.setTenant(tenant);
        file.setUploadedBy(owner);
        file.setFileName("report.pdf");
        file.setOriginalFileName("report.pdf");
        file.setFileSize(size);
        file.setContentType("application/pdf");
        file.setStorageKey(TENANT_KEY + "/uuid_report.pdf");
        file.setBucketName("test-bucket");
        file.setUploadStatus(status);
        file.setIsDeleted(false);
        return file;
    }


    public static void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    public static void clearAllContexts() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
        LookupContext.clear();
        UserIDContext.clear();
    }
}
