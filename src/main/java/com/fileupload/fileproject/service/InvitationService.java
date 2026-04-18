package com.fileupload.fileproject.service;


import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.TenantInvitation;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.UserRole;
import com.fileupload.fileproject.repository.TenantInvitationRepository;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.util.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvitationService {

    private final TenantRepository tenantRepository;
    private final EmailService emailService;
    private final PasswordEncoder  passwordEncoder;
    private final TenantInvitationRepository   tenantInvitationRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public void sendInvites(List<String> emails ,String role)
    {

        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Users adminEntity = (Users) details.getUserEntity();

        Long tenantid = TenantContext.getTenantId();

        Tenant tenant = adminEntity.getTenant(); //tenantRepository.findById(tenantid).get();


        for(String email : emails)
        {
            String token = UUID.randomUUID().toString();

            TenantInvitation invitation = new TenantInvitation();
            invitation.setEmail(email);
            invitation.setToken(token);
            invitation.setTenant(tenant);
            invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
            invitation.setInvitedBy(adminEntity);
            tenantInvitationRepository.save(invitation);

            String inviteUrl = "https://" + tenant.getSubdomain() + "/register?token="+ token;

            emailService.sendInvite(email,inviteUrl,tenant.getOrganisationName());
        }
    }

    public TenantInvitation verifyToken(String token)
    {
        TenantInvitation invite = tenantInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired invitation link."));

        if (invite.isExpired()) {
            throw new RuntimeException("This invitation has expired.");
        }

        return invite;
    }

    @Transactional
    public Users completeRegistration(String token, String fullName, String password)
    {
        TenantInvitation invite = verifyToken(token);

        Users newUser = Users.builder()
                .email(invite.getEmail())
                .firstName(fullName)
                .passwordHash(passwordEncoder.encode(password))
                .role(UserRole.MEMBER)
                .tenant(invite.getTenant())
                .invitedBy(invite.getInvitedBy()).build();


        usersRepository.save(newUser);

        tenantInvitationRepository.delete(invite);
        return newUser;

    }
}
