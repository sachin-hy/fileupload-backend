package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.FileShare;
import com.fileupload.fileproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {
    List<FileShare> findBySharedWithAndTenant_Tenantid(Users currentUser, Long currentTenantId);
}
