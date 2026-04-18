package com.fileupload.fileproject.entity;


import com.fileupload.fileproject.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_invitations",
        indexes= {
                @Index(name  = "idx_token",columnList = "token"),
                @Index(name = "idx_invite_email", columnList = "email")
        })
@Getter
@Setter
public class TenantInvitation extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;


    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private LocalDateTime expiresAt;
    private boolean accepted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_user_id")
    private Users invitedBy;


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }


}
