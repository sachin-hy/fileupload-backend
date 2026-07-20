package com.fileupload.fileproject.responseDto;





public record LoginResponseDto(
        String accessToken,
        UserSummaryDto user,
        TenantSummaryDto tenant
) {}