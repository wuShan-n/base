package com.example.base.security.core;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class SecurityUser implements UserDetails {

    private final Long userId;
    private final Long tenantId;
    private final Long accountId;
    private final String username;
    @Builder.Default
    private final List<String> roles = Collections.emptyList();
    @Builder.Default
    private final Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
    private final String jti;
    private final String clientId;
    private final Instant accessTokenExpiresAt;
    private final int tokenVersion;
    private final int passwordVersion;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
