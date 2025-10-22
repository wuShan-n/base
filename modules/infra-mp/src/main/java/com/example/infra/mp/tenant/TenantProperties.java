package com.example.infra.mp.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "app.tenant")
public class TenantProperties {
    /**
     * 是否启用多租户拦截（默认 false）
     */
    private boolean enabled = false;
    /**
     * 租户字段名（默认 tenant_id）
     */
    private String column = "tenant_id";
    /**
     * 默认租户ID（解析不到时使用，建议 0 表示公共）
     */
    private long defaultTenantId = 0L;
    /**
     * 需要忽略多租户的表（如基础字典、系统配置等）
     */
    private Set<String> ignoreTables = new HashSet<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }
    public long getDefaultTenantId() { return defaultTenantId; }
    public void setDefaultTenantId(long defaultTenantId) { this.defaultTenantId = defaultTenantId; }
    public Set<String> getIgnoreTables() { return ignoreTables; }
    public void setIgnoreTables(Set<String> ignoreTables) { this.ignoreTables = ignoreTables; }
}
