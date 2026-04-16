package com.fileupload.fileproject.entity;

import com.fileupload.fileproject.entity.base.TenantAwareEntity;
import com.fileupload.fileproject.enums.UploadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;



@Entity
@Table(
        name = "file_metadata",
        indexes = {
                @Index(name = "idx_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, insertable = false, updatable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Users uploadedBy;


    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;


    @Column(name = "original_file_name", nullable = false, length = 500)
    private String originalFileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 100)
    private String contentType;


    @Column(name = "storage_key", nullable = false, length = 1000)
    private String storageKey;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "upload_id")
    private String uploadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 20)
    @Builder.Default
    private UploadStatus uploadStatus = UploadStatus.INITIATED;


    @Column(nullable = false)
    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Integer downloadCount = 0;






    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }



    public boolean isCompleted() {
        return uploadStatus == UploadStatus.COMPLETED;
    }
}