package com.fileupload.fileproject.service;


import com.fileupload.fileproject.entity.AuditLog;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.AuditAction;
import com.fileupload.fileproject.repository.AuditLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepo;

    @Transactional
    public void log(Long tenantId,String userEmail, AuditAction action, String ip,String details, String error) {
        AuditLog logEntry = new AuditLog();
        logEntry.setTenantId(tenantId);
        logEntry.setUserEmail(userEmail);
        logEntry.setAction(action);
        logEntry.setIpAddress(ip);
        logEntry.setDetails(details);
        logEntry.setErrorMessage(error);

        auditLogRepo.save(logEntry);
    }


}
