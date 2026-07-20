package com.fileupload.fileproject.entity;

import com.fileupload.fileproject.entity.base.BaseEntity;
import com.fileupload.fileproject.enums.Permission;
import com.fileupload.fileproject.enums.ShareStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "file_shares",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_file_user", columnNames = {"file_id", "shared_with"})
        },
        indexes = {

        @Index(name = "idx_share_tenant", columnList = "tenant_id"),
        @Index(name = "idx_share_status", columnList = "status")
})
public class FileShare extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private FileMetadata file;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_by", nullable = false)
    private Users sharedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_with", nullable = false)
    private Users sharedWith;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 20)
    @Builder.Default
    private Permission permission = Permission.DOWNLOAD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ShareStatus status = ShareStatus.ACTIVE;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;


    public boolean isActive()
    {
        return status == ShareStatus.ACTIVE &&
                (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }

}
