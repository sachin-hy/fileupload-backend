package com.fileupload.fileproject.responseDto;

import com.fileupload.fileproject.enums.PlanType;

import java.time.LocalDateTime;

public record TenantSummaryDto(
        Long id,
        String organisationName,
        String subdomain,
        LocalDateTime subscriptionEndsAt,
        PlanType planType
) {}
