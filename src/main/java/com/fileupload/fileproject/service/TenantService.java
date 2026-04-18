package com.fileupload.fileproject.service;


import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.UserRole;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.requestDto.TenantRegistrationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Data
@AllArgsConstructor
public class TenantService {


    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository userRepository;

    @Transactional
    public Users register(TenantRegistrationDto dto) {

        Tenant tenant = Tenant.builder()
                 .organisationName(dto.getOrganisationName())
                 .subdomain(dto.getSubdomain())
                 .tenantKey("TENANT_KEY_" + UUID.randomUUID().toString())
                 .adminEmail(dto.getAdminEmail())
                 .build();

        Tenant saveTenant =  tenantRepository.save(tenant);

        Users admin = Users.builder()
                .tenant(saveTenant)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getAdminEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(UserRole.TENANT_ADMIN)
                .build();

        Users tenantAdmin = userRepository.save(admin);

       return tenantAdmin;
    }
}
