package com.fileupload.fileproject.responseDto;



public record UserDto(
        Long id,
        String fullName,
        String email,
        String role,
        String status
) {}