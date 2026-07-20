package com.fileupload.fileproject.service;


import com.fileupload.fileproject.Exception.ResourceConflictException;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.TenantInvitation;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.AuditAction;
import com.fileupload.fileproject.enums.UserRole;
import com.fileupload.fileproject.enums.UserStatus;
import com.fileupload.fileproject.repository.TenantInvitationRepository;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.util.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final AuditLogService auditLogService;


    @Transactional
    public void sendInvites(List<String> emails ,String role,String ip)  {

        Long tenantid = TenantContext.getTenantId();

        if(usersRepository.existsByEmailInAndTenantId(emails,tenantid)){
            throw new ResourceConflictException("One or more team members are already registered within this workspace tenant.");
        }

        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tenantId = TenantContext.getTenantId();

        Users adminEntity = usersRepository.findByEmailAndTenant_Tenantid(adminEmail,tenantId).orElseThrow(() ->  new UsernameNotFoundException("User Not available"));



        Tenant tenant = adminEntity.getTenant(); //tenantRepository.findById(tenantid).get();

        long currentUsersCount = usersRepository.countByTenant_Tenantid(tenant.getTenantid());
        int maxUsers = tenant.getMaxUsers();

        int totalUsers = emails.size();

        if(currentUsersCount + totalUsers > maxUsers)
        {
            throw new RuntimeException("Users Limit exceed Plan Limit");
        }


        try {
            for (String email : emails) {
                String token = UUID.randomUUID().toString();

                TenantInvitation invitation = new TenantInvitation();

                invitation.setEmail(email);
                invitation.setToken(token);
                invitation.setTenant(tenant);
                invitation.setRole(UserRole.valueOf(role));
                invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
                invitation.setInvitedBy(adminEntity);
                tenantInvitationRepository.save(invitation);

                String inviteUrl = "https://" + tenant.getSubdomain() + "/register?token=" + token;

                emailService.sendInvite(email, inviteUrl, tenant.getOrganisationName());

               }

            auditLogService.log(tenant.getTenantid(), adminEntity.getEmail(), AuditAction.INVITATION_SENT, ip, "Invited: " + emails.toString(), null);

        }catch(Exception e)
        {
            auditLogService.log(tenant.getTenantid(), adminEntity.getEmail(), AuditAction.INVITATION_FAILED, ip,null, e.getMessage());
            throw e;
        }
    }




    @Transactional
    public String completeRegistration(String token, String firstName, String lastName, String password,String ip)
    {
        TenantInvitation invite = tenantInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired invitation link."));

        if (invite.isExpired()) {
            throw new RuntimeException("This invitation link has expired.");
        }


        // check email is already registerd or not
        if (usersRepository.existsByEmail(invite.getEmail())) {
            throw new RuntimeException("Email is already registered.");
        }

        Long tenantId = invite.getTenant().getTenantid();

        Tenant tenant = tenantRepository.findAndLockByTenantid(tenantId);//.orElseThrow(() -> new RuntimeException("Tenant not found."));

        int maxUsers = tenant.getMaxUsers();

        long currentUsersCount = usersRepository.countByTenant_Tenantid(tenant.getTenantid());

        if(currentUsersCount  >= maxUsers)
        {
            throw new RuntimeException("Users Limit exceed Plan Limit");
        }

        Users newUser = Users.builder()
                .tenant(tenant)
                .firstName(firstName)
                .lastName(lastName)
                .email(invite.getEmail())
                .passwordHash(passwordEncoder.encode(password))
                .role(invite.getRole())
                .status(UserStatus.ACTIVE)
                .invitedBy(invite.getInvitedBy())
                .build();
     try{

          usersRepository.save(newUser);

          invite.setAccepted(true);
          tenantInvitationRepository.save(invite);
          auditLogService.log(invite.getTenant().getTenantid(), newUser.getEmail(), AuditAction.USER_REGISTERED, ip, "user registration completed", null);

          return tenant.getOrganisationName();
    } catch (Exception e) {

        auditLogService.log(invite.getTenant().getTenantid(), null, AuditAction.REGISTRATION_FAILED, ip,null, e.getMessage());
        throw e;
    }

    }
}
