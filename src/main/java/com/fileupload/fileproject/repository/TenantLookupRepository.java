package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.TenantLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantLookupRepository extends JpaRepository<TenantLookup,Long> {
    TenantLookup findByEmailAndSubdomain(String email, String subdomain);
}
