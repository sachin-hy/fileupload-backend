package com.fileupload.fileproject.responseDto;

public record UserSummaryDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {}
