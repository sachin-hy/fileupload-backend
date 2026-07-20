package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.TenantUsage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TenantUsageRepository extends JpaRepository<TenantUsage, Long> {
    TenantUsage findByTenant_Tenantid(Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TenantUsage t WHERE t.tenant.tenantid = :tenantId")
    Optional<TenantUsage> findAndLockByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT tu.usedStorageBytes,tu.tenant.storageQuotaGB from TenantUsage tu  WHERE tu.tenant.tenantid = :tenantId")
    Object fetchStorageMetrics(@Param("tenantId") Long tenantId);
}
