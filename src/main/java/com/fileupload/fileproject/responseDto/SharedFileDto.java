package com.fileupload.fileproject.responseDto;

import java.time.LocalDate;

public record SharedFileDto(
        Long id,
        String fileName,
        String type,
        String sharedByFullName,
        Long size
) {
}