package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.FileMetadata;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    //FileMetadata findByStorageKeyAndTenant(String s3Key, Tenant tenant);

    //List<FileMetadata> findByTenant_TenantidAndUploadStatusAndIsDeletedFalse(Long tenantId, UploadStatus uploadStatus);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f " +
            "WHERE f.tenant.id = :tenantId AND f.uploadStatus = :status")
    Long getSumOfFileSizesByTenantAndStatus(@Param("tenantId") Long tenantId,
                                            @Param("status") UploadStatus status);
    Optional<FileMetadata> findByIdAndTenant(Long fileId, Tenant teanant);

    List<FileMetadata> findByTenant_TenantidAndUploadStatus(Long tenantId, UploadStatus uploadStatus);

   FileMetadata findByStorageKeyAndTenant_Tenantid(String s3Key, Long tenantid);

    FileMetadata findByIdAndTenant_Tenantid(Long fileId, Long currentTenantId);

    //FileMetadata findByIdAndTenant_Tenantid(Long fileId, Long tenantid);
}
