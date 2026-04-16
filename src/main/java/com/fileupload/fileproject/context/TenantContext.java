package com.fileupload.fileproject.context;

public class TenantContext {
    private static final ThreadLocal<Long> tenantIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> tenantKeyHolder = new ThreadLocal<>();

    public static void setContext(Long id, String key) {
        tenantIdHolder.set(id);
        tenantKeyHolder.set(key);
    }

    public static Long getTenantId() {
        return tenantIdHolder.get();
    }
    public static String getTenantKey() {
        return tenantKeyHolder.get();
    }

    public static void clear() {
        tenantIdHolder.remove();
        tenantKeyHolder.remove();
    }
}
