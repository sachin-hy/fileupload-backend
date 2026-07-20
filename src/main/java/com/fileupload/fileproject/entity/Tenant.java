package com.fileupload.fileproject.entity;

import com.fileupload.fileproject.entity.base.BaseEntity;
import com.fileupload.fileproject.enums.PlanType;
import com.fileupload.fileproject.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
        name = "tenants",
        indexes ={
                @Index(name = "idx_tenant_key",columnList = "tenantKey"),
                @Index(name = "idx_subdomain", columnList = "subdomain"),
                @Index(name = "idx_status", columnList = "status")
        }
      )
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tenantid;


    @Column(name = "tenant_key", nullable = false, unique = true, length = 50)
    private String tenantKey;

    @Column(name = "subdomain", nullable = false, unique = true,length = 100)
    private String subdomain;

    @Column(name = "orgainisation_name", nullable = false, unique = true)
    private String organisationName;



    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "storage_quota_gb", nullable = false)
    @Builder.Default
    private Long storageQuotaGB = 10L;

    @Column(name = "max_users", nullable = false)
    @Builder.Default
    private Integer maxUsers = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type",nullable = false, length = 20)
    @Builder.Default
    private PlanType planType = PlanType.FREE;


    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "subscription_ends_at")
    @Builder.Default
    private LocalDateTime subscriptionEndsAt = null;

    @OneToMany(mappedBy = "tenant" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Users> users = new ArrayList<>();

    @OneToMany(mappedBy = "tenant" , cascade = CascadeType.ALL,orphanRemoval = true)
    private List<FileMetadata> files = new ArrayList<>();

    @OneToOne(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private TenantUsage  usage;

    public boolean isActive()
    {
        return status == TenantStatus.ACTIVE;
    }


    @PrePersist
    @PreUpdate
    public void syncPlanLimits()
    {
        if(this.planType != null)
        {
            this.storageQuotaGB = this.planType.getStorageQuotaGB();
            this.maxUsers = this.planType.getMaxUsers();
        }
    }

    public boolean isExpired()
    {
        if(this.subscriptionEndsAt == null)
        {
            return false;
        }

        return LocalDateTime.now().isAfter(this.subscriptionEndsAt);
    }


    public long getStorageQuotaBytes()
    {
        return storageQuotaGB  * 1024L * 1024L * 1024L;
    }



}
