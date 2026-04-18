package com.fileupload.fileproject.responseDto;

import java.time.LocalDate;

public record SharedFileDto(
        Long shareId,
        Long fileId,
        String fileName,
        String sharedByName,
        LocalDate sharedAt
) {

}
