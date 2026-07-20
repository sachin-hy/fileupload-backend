package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
}
