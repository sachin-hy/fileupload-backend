package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.FileMetadata;
import com.fileupload.fileproject.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    FileMetadata findByStorageKey(String s3Key);

    List<FileMetadata> findByTenant_TenantidAndUploadStatusAndIsDeletedFalse(Long tenantId, UploadStatus uploadStatus);
}
