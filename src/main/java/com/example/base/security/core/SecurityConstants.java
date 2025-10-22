package com.example.base.security.core;

public final class SecurityConstants {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TENANT_HEADER = "X-Tenant-Id";
    public static final String ROLES_KEY = "roles";
    public static final String SCOPES_KEY = "scopes";

    private SecurityConstants() {
    }
}
