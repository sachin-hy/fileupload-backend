package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.TenantInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TenantInvitationRepository extends JpaRepository<TenantInvitation,Long> {

    @Query("""
    SELECT ti
    FROM TenantInvitation ti
    WHERE ti.token = :token
      AND ti.tenant.tenantid = :tenantId
""")
    Optional<TenantInvitation> findByTokenAndTenantId(@Param("token") String token, @Param("tenantId") Long tenantId);
}
