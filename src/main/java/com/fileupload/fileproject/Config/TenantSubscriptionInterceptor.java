package com.fileupload.fileproject.Config;

import com.fileupload.fileproject.Exception.SubscriptionExpiredException;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.enums.TenantStatus;
import com.fileupload.fileproject.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantSubscriptionInterceptor implements HandlerInterceptor{
    private final TenantService tenantService;

    public TenantSubscriptionInterceptor(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        Long tenantId = TenantContext.getTenantId();

        if(tenantId != null){
            Tenant tenant = tenantService.getTenantByIdCached(tenantId);

            if (tenant.isExpired()) {
                throw new SubscriptionExpiredException("Your subscription expired on date : " + tenant.getSubscriptionEndsAt());
            }

            if(tenant.getStatus() == TenantStatus.SUSPENDED || tenant.getStatus() == TenantStatus.INACTIVE){
                throw new SubscriptionExpiredException("Your Plan got Blocked");
            }
        }

        return true;
    }
}
