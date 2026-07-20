package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.Tenant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;




@Repository
public interface TenantRepository extends JpaRepository< Tenant,Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tenant t WHERE t.tenantid = :tenantId")
    Tenant findAndLockByTenantid(@Param("tenantId") Long tenantId);
}
