package com.example.infra.mp.tenant;

import java.util.Optional;

/**
 * 当前请求的租户ID解析。
 * 可自定义实现（如从登录 JWT、ThreadLocal、DB路由等）
 */
public interface TenantResolver {
    Optional<Long> resolveTenantId();
}
