package com.fileupload.fileproject.entity;

import com.fileupload.fileproject.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class TenantUsage extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    @Column(name = "used_storage_bytes", nullable = false)
    private Long usedStorageBytes = 0L;

    @Column(name = "file_count", nullable = false)
    private Integer fileCount = 0;

    @Column(name = "total_users", nullable = false)
    private Integer totalUsers = 0;

    @Column(name = "total_uploads", nullable = false)
    private Long totalUploads = 0L;

    public double getUsedStorageGB() {
        return usedStorageBytes / (1024.0 * 1024.0 * 1024.0);
    }

    public double getStorageUsagePercentage(long quotaGB) {
        long quotaBytes = quotaGB * 1024L * 1024L * 1024L;
        return (usedStorageBytes * 100.0) / quotaBytes;
    }

    public void incrementFileCount() {
        this.fileCount++;
    }

    public void decrementFileCount() {
        if (this.fileCount > 0) {
            this.fileCount--;
        }
    }

    public void addStorage(long bytes) {
        this.usedStorageBytes += bytes;
    }

    public void removeStorage(long bytes) {
        this.usedStorageBytes = Math.max(0, this.usedStorageBytes - bytes);
    }

}
