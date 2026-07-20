package com.fileupload.fileproject.responseDto;

import java.time.LocalDate;

public record RecentShareDto(
        Long shareId,
        Long fileId,
        String fileName,
        String sharedByFullName,
        String sharedWithFullName,
        LocalDate sharedAt
) {}
