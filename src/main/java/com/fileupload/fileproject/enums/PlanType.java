package com.fileupload.fileproject.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlanType {

    FREE("Free", 10L, 5, 0),
    BASIC("Basic", 50L, 20, 1),
    PREMIUM("Premium", 100L, 50, 2),
    ENTERPRISE("Enterprise", 500L, 200, 3);

    private final String displayName;
    private final Long storageQuotaGB;
    private final int maxUsers;
    private final int level;
}
