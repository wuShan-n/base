package com.example.security.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 安全上下文便捷方法
 */
public final class SecurityUtils {
    private SecurityUtils(){}

    public static Optional<String> currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();
        return Optional.ofNullable(auth.getName());
    }

    public static Optional<Long> currentUserIdAsLong() {
        return currentUserId().flatMap(id -> {
            try { return Optional.of(Long.parseLong(id)); }
            catch (NumberFormatException e) { return Optional.empty(); }
        });
    }

    public static Optional<String> currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token) {
            Jwt jwt = token.getToken();
            String sub = jwt.getSubject();
            if (sub != null) return Optional.of(sub);
        }
        if (auth != null && auth.getName() != null) {
            return Optional.of(auth.getName());
        }
        return Optional.empty();
    }

    public static List<String> currentAuthorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return List.of();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities == null) return List.of();
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
    }
}
