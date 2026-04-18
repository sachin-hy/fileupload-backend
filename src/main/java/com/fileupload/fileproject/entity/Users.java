package com.fileupload.fileproject.entity;


import com.fileupload.fileproject.entity.base.BaseEntity;
import com.fileupload.fileproject.enums.UserRole;
import com.fileupload.fileproject.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_status" , columnList = "status"),
                @Index(name = "idx_role", columnList = "role")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tenant_email", columnNames = {"tenant_id", "email"})
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
@Getter
@Builder
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id",nullable = false)
    private Tenant tenant;

    @Column(name = "email", nullable = false)
    private String email;


    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;


    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.MEMBER;


    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.INVITED;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;


    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private Users invitedBy;

//    @Column(name = "invitation_token")
//    private String invitationToken;

//    @Column(name = "invitation_expires_at")
//    private LocalDateTime invitationExpiresAt;

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL)
    @Builder.Default
    private List<FileMetadata> uploadedFiles = new ArrayList<>();

    @OneToMany(mappedBy = "sharedWith")
    @Builder.Default
    private List<FileShare> sharedFiles = new ArrayList<>();


    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive(){
        return status == UserStatus.ACTIVE;
    }

    public boolean hasAdminPrivileges() {
        return role != null && role.hasAdminPrivileges();
    }

    public void updateLastLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
    }

}
