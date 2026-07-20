package com.fileupload.fileproject.service;


import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.enums.AuditAction;
import com.fileupload.fileproject.enums.PlanType;
import com.fileupload.fileproject.enums.TenantStatus;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.requestDto.PlanUpdateRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PlanTypeUpdateService {

    private final TenantRepository tenantRepo;

    private final AuditLogService auditLogService;

    private final TenantService tenantService;




    public List<Map<String, Object>> getFrontendPlans() {

        List<Map<String, Object>> formattedPlans = new ArrayList<>();
        for (PlanType plan : PlanType.values()) {
            Map<String, Object> planMap = new HashMap<>();

            planMap.put("enumName", plan.name());
            planMap.put("name", plan.getDisplayName() + " Plan");
            planMap.put("quota", plan.getStorageQuotaGB() + " GB");
            planMap.put("users", "Up to " + plan.getMaxUsers() + " users");

            switch (plan) {
                case FREE -> {
                    planMap.put("price", "$0");
                    planMap.put("desc", "Good for testing the project.");
                }
                case BASIC -> {
                    planMap.put("price", "$29");
                    planMap.put("desc", "For small business environments.");
                }
                case PREMIUM -> {
                    planMap.put("price", "$99");
                    planMap.put("desc", "For bigger teams needing optimized limits.");
                }
                case ENTERPRISE -> {
                    planMap.put("price", "$249");
                    planMap.put("desc", "Full power scale for massive organizations.");
                }
            }

            formattedPlans.add(planMap);
        }

        return formattedPlans;
    }



    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public void updatePlan(PlanUpdateRequestDto dto, String ip,Long tenantId) {

         String planType = dto.planType();

         Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Tenant space not found with ID: " + tenantId));

        PlanType oldPlan = tenant.getPlanType();

        try {

            PlanType newPlan;
            try {
                newPlan = PlanType.valueOf(planType.toUpperCase());
            } catch (IllegalArgumentException ex) {

                throw new IllegalArgumentException("The requested plan tier '" + planType + "' does not exist in our system.");
            }


            if (newPlan == oldPlan) {
                throw new IllegalArgumentException(
                        String.format("You are already subscribed to the %s plan tier.", oldPlan.name())
                );
            }

            if (newPlan.getLevel() < oldPlan.getLevel()) {
                throw new IllegalArgumentException(
                        String.format("Cannot downgrade directly from %s to %s via dashboard.", oldPlan.name(), newPlan.name())
                );
            }


            tenant.setPlanType(newPlan);
            tenant.setStorageQuotaGB(newPlan.getStorageQuotaGB());
            tenant.setMaxUsers(newPlan.getMaxUsers());
            tenant.setStatus(TenantStatus.ACTIVE);

            tenant.setSubscriptionEndsAt(LocalDateTime.now().plusMonths(1));

            tenantRepo.save(tenant);

            String details = "Plan successfully upgraded from " + oldPlan.name() + " to " + newPlan.name();
            auditLogService.log(tenantId, null, AuditAction.PLAN_UPDATED, ip, details, null);

        } catch (IllegalArgumentException e) {
            auditLogService.log(tenantId, null, AuditAction.PLAN_UPDATE_FAILED, ip, null, e.getLocalizedMessage());
            throw new IllegalArgumentException(e.getMessage());

        } catch (Exception e) {
            auditLogService.log(tenantId, null, AuditAction.PLAN_UPDATE_FAILED, ip, null, e.getMessage());
            throw e;
        }
    }


    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public void renewPlan(){

        Long tenantId = TenantContext.getTenantId();

        Tenant tenant = tenantRepo.findById(tenantId).orElseThrow(() -> new EntityNotFoundException("Tenant workspace not found"));

        PlanType currentPlan = tenant.getPlanType();

        try{
             if(currentPlan == PlanType.FREE)
             {
                 throw new IllegalArgumentException("The Free Plan tier does not require renew");
             }

             LocalDateTime currentExpiry = tenant.getSubscriptionEndsAt();

             LocalDateTime newExpiry;

             if(currentExpiry != null && currentExpiry.isAfter(LocalDateTime.now())){

                 newExpiry = currentExpiry.plusMonths(1);

             }else{
                 newExpiry = LocalDateTime.now().plusMonths(1);
             }

             tenant.setSubscriptionEndsAt(newExpiry);
             tenant.setStatus(TenantStatus.ACTIVE);

             tenantRepo.save(tenant);

             String details = String.format("Subscription for plan %s successfull renewed.");

             auditLogService.log(tenantId, null, AuditAction.PLAN_RENEWED, null, details, null);

        }catch(Exception e){

            auditLogService.log(tenantId, null, AuditAction.PLAN_RENEW_FAILED, null, null, e.getMessage());
            throw e;

        }

    }


}
