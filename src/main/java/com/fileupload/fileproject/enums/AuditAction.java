package com.fileupload.fileproject.enums;

public enum AuditAction {

    FILE_UPLOAD,
    FILE_DOWNLOAD,
    FILE_DELETE,
    FILE_SHARE,
    FILE_UPDATE,


    USER_LOGIN,
    USER_LOGOUT,
    USER_INVITE,
    USER_REMOVE,
    USER_UPDATE,


    TENANT_CREATE,
    TENANT_UPDATE,
    TENANT_SUSPEND,


    SETTINGS_UPDATE,
    QUOTA_EXCEEDED
}
