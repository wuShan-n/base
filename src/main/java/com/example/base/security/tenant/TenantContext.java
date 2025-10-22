package com.example.base.security.tenant;

public final class TenantContext {
    private static final ThreadLocal<Long> TENANT = ThreadLocal.withInitial(() -> 0L);

    private TenantContext() {
    }

    public static Long getTenantId() {
        return TENANT.get();
    }

    public static void setTenantId(Long tenantId) {
        TENANT.set(tenantId == null ? 0L : tenantId);
    }

    public static void clear() {
        TENANT.remove();
    }
}
