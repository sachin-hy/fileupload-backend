package com.fileupload.fileproject.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlanType {

    FREE("Free", 10, 5),
    BASIC("Basic", 50, 20),
    PREMIUM("Premium", 100, 50),
    ENTERPRISE("Enterprise", 500, 200);

    private final String displayName;
    private final int storageQuotaGB;
    private final int maxUsers;
}
