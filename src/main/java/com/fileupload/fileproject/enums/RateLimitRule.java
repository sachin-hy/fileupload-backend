package com.fileupload.fileproject.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RateLimitRule {
    FILE_INITIATE      ("/api/private/files/initiate",        5,  5),
    FILE_PRESIGNED_URL ("/api/private/files/presigned-url",   20, 20),
    FILE_COMPLETE      ("/api/private/files/complete",        5,  5),
    FILE_DOWNLOAD      ("/api/private/download/*",            5, 5),
    FILE_LIST          ("/api/private/files",                 5, 5),

    SHARES_ME          ("/api/private/shares/me",             10, 10),
    SHARE              ("/api/private/share",                 5, 5),
    RECENT_ACTIVITY    ("/api/private/recent-activity",       10, 10),
    USERS_COUNT        ("/api/private/users/count",            30, 30),
    FILES_COUNT        ("/api/private/files/count",            30, 30),
    USERS_FILES_COUNT  ("/api/private/users/files/count",      30, 30),
    SHARE_FILE          ("/api/private/shares",                5 , 5),
    SEND_INVITATIONS    ("/api/private/invitations/send",      5 , 5),
    COMPLETE_REGISTRATION("/api/public/invitation/register/*", 5,5),
    LOGIN                ("/api/public/login" ,                5 ,5 ),
    PUBLIC_PLANS    ("/api/public/plans",                      30, 30),
    TENANT_UPGRADE  ("/api/private/tenant/upgrade",            3,  3),
    TENANT_REGISTER ("/api/public/register/tenant",           3, 3),
    TEAM_MEMBERS ("/api/private/members",                     20, 20),
    STORAGE_DETAILS ("/api/private/storageDetails",           20, 20),
    DEFAULT         ("/*" ,                                   10, 10 );

    private final String urlPattern;
    private final int baseCapacity;
    private final int baseRefillPerMinute;
}
