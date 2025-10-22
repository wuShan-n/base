package com.example.base.security.filter;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.base.auth.entity.AuthAccessDenylistEntity;
import com.example.base.auth.entity.AuthAccountEntity;
import com.example.base.auth.enums.AccountStatus;
import com.example.base.auth.mapper.AuthAccessDenylistMapper;
import com.example.base.auth.mapper.AuthAccountMapper;
import com.example.base.common.enums.UserStatus;
import com.example.base.security.core.SecurityConstants;
import com.example.base.security.core.SecurityUser;
import com.example.base.security.core.SecurityUtils;
import com.example.base.security.exception.JwtAuthenticationException;
import com.example.base.security.tenant.TenantContext;
import com.example.base.security.token.JwtTokenService;
import com.example.base.user.entity.UserEntity;
import com.example.base.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AuthAccessDenylistMapper accessDenylistMapper;
    private final UserMapper userMapper;
    private final AuthAccountMapper accountMapper;
    private final com.example.base.security.handler.RestAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Claims claims = jwtTokenService.parseAccessToken(token);
            SecurityUser securityUser = buildSecurityUser(claims);
            Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    securityUser,
                    null,
                    securityUser.getAuthorities());
            ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) authentication)
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            SecurityUtils.clear();
            authenticationEntryPoint.commence(request, response, ex);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityUtils.clear();
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException("INVALID_TOKEN", ex));
        }
    }

    private SecurityUser buildSecurityUser(Claims claims) {
        String subject = claims.getSubject();
        if (subject == null) {
            throw new BadCredentialsException("TOKEN_SUBJECT_MISSING");
        }
        Long userId = parseLong(subject);
        Long tenantId = getLongClaim(claims, "tenant");
        Long accountId = getLongClaim(claims, "aid");
        String jti = claims.getId();
        if (jti == null) {
            throw new BadCredentialsException("TOKEN_ID_MISSING");
        }
        if (isDenylisted(jti)) {
            throw new BadCredentialsException("TOKEN_REVOKED");
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadCredentialsException("USER_NOT_FOUND");
        }
        validateUserStatus(user);
        AuthAccountEntity account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BadCredentialsException("ACCOUNT_NOT_FOUND");
        }
        validateAccountStatus(account);
        validateVersions(claims, user, account);
        ensureTenantConsistency(tenantId);

        List<String> roles = extractRoles(claims);
        Collection<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        Instant expiresAt = claims.getExpiration() != null
                ? claims.getExpiration().toInstant()
                : Instant.now().plusSeconds(30);
        String clientId = claims.get("cid", String.class);

        return SecurityUser.builder()
                .userId(userId)
                .tenantId(tenantId)
                .accountId(accountId)
                .username(account.getIdentifier())
                .roles(roles)
                .authorities(authorities)
                .jti(jti)
                .clientId(clientId)
                .accessTokenExpiresAt(expiresAt)
                .tokenVersion(Optional.ofNullable(user.getTokenVersion()).orElse(0))
                .passwordVersion(Optional.ofNullable(account.getPasswordVersion()).orElse(0))
                .build();
    }

    private void validateUserStatus(UserEntity user) {
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BadCredentialsException("USER_DISABLED");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BadCredentialsException("USER_LOCKED");
        }
    }

    private void validateAccountStatus(AuthAccountEntity account) {
        if (account.getStatus() == AccountStatus.DISABLED) {
            throw new BadCredentialsException("ACCOUNT_DISABLED");
        }
        if (account.getLockUntil() != null && account.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BadCredentialsException("ACCOUNT_LOCKED");
        }
    }

    private void validateVersions(Claims claims, UserEntity user, AuthAccountEntity account) {
        int tokenVersionClaim = getIntClaim(claims, "tver");
        int passwordVersionClaim = getIntClaim(claims, "pver");
        int userTokenVersion = Optional.ofNullable(user.getTokenVersion()).orElse(0);
        int accountPasswordVersion = Optional.ofNullable(account.getPasswordVersion()).orElse(0);
        if (userTokenVersion != tokenVersionClaim) {
            throw new BadCredentialsException("TOKEN_VERSION_MISMATCH");
        }
        if (accountPasswordVersion != passwordVersionClaim) {
            throw new BadCredentialsException("PASSWORD_VERSION_MISMATCH");
        }
    }

    private void ensureTenantConsistency(Long tenantId) {
        Long current = TenantContext.getTenantId();
        if (!Objects.equals(current, tenantId)) {
            throw new BadCredentialsException("TENANT_MISMATCH");
        }
        TenantContext.setTenantId(tenantId);
    }

    private boolean isDenylisted(String jti) {
        String hash = SecureUtil.sha256(jti);
        AuthAccessDenylistEntity entity = accessDenylistMapper.selectOne(new LambdaQueryWrapper<AuthAccessDenylistEntity>()
                .eq(AuthAccessDenylistEntity::getJtiHash, hash));
        return entity != null && entity.getExpiresAt() != null && entity.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private List<String> extractRoles(Claims claims) {
        Object rolesClaim = claims.get(SecurityConstants.ROLES_KEY);
        if (rolesClaim instanceof List<?> rawList) {
            return rawList.stream()
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }

    private Long getLongClaim(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str) {
            return Long.parseLong(str);
        }
        throw new BadCredentialsException("INVALID_CLAIM_" + key);
    }

    private int getIntClaim(Claims claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str) {
            return Integer.parseInt(str);
        }
        throw new BadCredentialsException("INVALID_CLAIM_" + key);
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new BadCredentialsException("INVALID_SUBJECT", ex);
        }
    }
}
