package com.fileupload.fileproject.service;


import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.TenantUsage;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.AuditAction;
import com.fileupload.fileproject.enums.PlanType;
import com.fileupload.fileproject.enums.UserRole;
import com.fileupload.fileproject.enums.UserStatus;
import com.fileupload.fileproject.repository.FileShareRepository;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.TenantUsageRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.requestDto.TenantRegistrationDto;
import com.fileupload.fileproject.responseDto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository userRepository;
    private final TenantUsageRepository tenantUsageRepository;
    private final AuditLogService auditLogService;
    private final FileShareRepository fileShareRepository;


    @Transactional
    public Users register(TenantRegistrationDto dto, String ip) {
        try {

            Tenant tenant = Tenant.builder()
                    .tenantKey("TENANT_KEY_" + UUID.randomUUID().toString())
                    .subdomain(dto.getSubdomain())
                    .organisationName(dto.getOrganisationName())
                    .description(dto.getDescription())
                    .adminEmail(dto.getAdminEmail())
                    .planType(PlanType.FREE)
                    .storageQuotaGB(PlanType.FREE.getStorageQuotaGB())
                    .maxUsers(PlanType.FREE.getMaxUsers())
                    .build();

            Tenant saveTenant = tenantRepository.save(tenant);

            ///  save tenatuasge for current tenant
            TenantUsage tenantUsage = TenantUsage.builder()
                    .tenant(saveTenant)
                    .usedStorageBytes(0L)
                    .fileCount(0)
                    .totalUploads(0L)
                    .build();

            tenantUsageRepository.save(tenantUsage);

            /// /save user admin ////
            Users admin = Users.builder()
                    .tenant(saveTenant)
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .email(dto.getAdminEmail())
                    .passwordHash(passwordEncoder.encode(dto.getPassword()))
                    .phone(dto.getPhone())
                    .role(UserRole.TENANT_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            Users tenantAdmin = userRepository.save(admin);

            String details = "Tenant: " + saveTenant.getOrganisationName() + ", Admin: " + dto.getAdminEmail();
            auditLogService.log(saveTenant.getTenantid(), tenantAdmin.getEmail(), AuditAction.TENANT_REGISTERED, ip, details,null);
            return tenantAdmin;
        }catch (Exception e) {

            auditLogService.log(null, null, AuditAction.TENANT_REGISTRATION_FAILED, ip, null, e.getMessage());
            throw e;
        }
    }


    @Cacheable(value = "tenants", key = "#tenantId")
    public Tenant getTenantByIdCached(Long tenantId) {
         return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant space not found"));
    }



    @Transactional(readOnly = true)
    public List<UserDto> getTeamMembers(Long currentTenantId) {
       List<Users> users = userRepository.findByTenant_Tenantid(currentTenantId);

        return users.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole().toString(),
                        user.getStatus().toString()
                ))
                .toList();

    }


    @Transactional(readOnly = true)
    public Integer totalUsers(Long tenantId) {

        return tenantRepository.findById(tenantId).get().getUsers().size();
        
    }

    @Transactional(readOnly = true)
    public Integer totalFiles(String email,Long tenantId) {

       return fileShareRepository.countSharedFilesByEmailAndTenantId(email,tenantId);

    }

    @Transactional(readOnly = true)
    public Map<String, Double> totalStorageDetails(Long tenantId) {
        Object result = tenantUsageRepository.fetchStorageMetrics(tenantId);

        Object[] row = (Object[]) result;

        Map<String, Double> map = new HashMap<>();

        Long usedStorageBytes = (Long) row[0];
        Long quotaStorageGB = (Long) row[1];

        double calculatedUsedGB = usedStorageBytes / (1024.0 * 1024.0 * 1024.0);

        double roundedUsedGB = Math.round(calculatedUsedGB * 100.0) / 100.0;

        map.put("usedStorageGB", roundedUsedGB);
        map.put("quotaStorageGB", quotaStorageGB.doubleValue());

        return map;
    }


    @Transactional(readOnly = true)
    public Map<String,Integer> totalUsersAndFiles(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId).get();

        Map<String,Integer> map = new HashMap<>();
        map.put("totalUsers",tenant.getUsers().size());
        map.put("totalFiles",tenant.getFiles().size());
        map.put("maxUsers",tenant.getMaxUsers());

        return map;
    }
}
