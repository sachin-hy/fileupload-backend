package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.FileShare;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.responseDto.RecentShareDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {

    List<FileShare> findBySharedWithAndTenant_Tenantid(Users currentUser, Long currentTenantId);

    @Query("SELECT new com.fileupload.fileproject.responseDto.RecentShareDto(" +
            "fs.id, " +
            "fs.file.id, " +
            "fs.file.originalFileName, " +
            "fs.sharedBy.firstName, " +
            "fs.sharedWith.firstName, " +
            "fs.createdAt) " +
            "FROM FileShare fs " +
            "WHERE fs.sharedWith.email = :email AND fs.status = 'ACTIVE' AND fs.tenant.tenantid = :tenantid " +
            "ORDER BY fs.createdAt DESC")
    List<RecentShareDto> findRecentActiveSharesByEmailAndTenant_Tenantid(@Param("email") String email,@Param("tenantid") Long tenantId);

    @Query("SELECT COUNT(fs) FROM FileShare fs WHERE fs.sharedWith.email = :email AND fs.tenant.tenantid = :tenantId")
    Integer countSharedFilesByEmailAndTenantId(@Param("email") String email, @Param("tenantId") Long tenantId);

    @Query("SELECT new com.fileupload.fileproject.responseDto.RecentShareDto(" +
            "fs.id, " +
            "fs.file.id, " +
            "fs.file.originalFileName, " +
            "fs.sharedBy.firstName, " +
            "fs.sharedWith.firstName, " +
            "fs.createdAt) " +
            "FROM FileShare fs " +
            "WHERE fs.status = 'ACTIVE' AND fs.tenant.tenantid = :tenantid " +
            "ORDER BY fs.createdAt DESC")
    List<RecentShareDto> findShareOnOrganisation(@Param("tenantid") Long tenantId);
}

