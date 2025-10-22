package com.example.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    /**
     * 允许的来源（完整域名或通配），为空则不配置 CORS
     */
    private List<String> allowedOrigins = new ArrayList<>();
    /**
     * 允许的方法
     */
    private List<String> allowedMethods = List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS");
    /**
     * 允许的请求头
     */
    private List<String> allowedHeaders = List.of("*");
    /**
     * 暴露的响应头
     */
    private List<String> exposedHeaders = List.of("X-Trace-Id");
    /**
     * 是否允许携带凭据
     */
    private boolean allowCredentials = true;
    /**
     * 预检缓存秒数
     */
    private long maxAge = 3600;

    public List<String> getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    public List<String> getAllowedMethods() { return allowedMethods; }
    public void setAllowedMethods(List<String> allowedMethods) { this.allowedMethods = allowedMethods; }
    public List<String> getAllowedHeaders() { return allowedHeaders; }
    public void setAllowedHeaders(List<String> allowedHeaders) { this.allowedHeaders = allowedHeaders; }
    public List<String> getExposedHeaders() { return exposedHeaders; }
    public void setExposedHeaders(List<String> exposedHeaders) { this.exposedHeaders = exposedHeaders; }
    public boolean isAllowCredentials() { return allowCredentials; }
    public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
    public long getMaxAge() { return maxAge; }
    public void setMaxAge(long maxAge) { this.maxAge = maxAge; }
}
