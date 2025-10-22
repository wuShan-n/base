package com.example.base.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.base.auth.dto.LoginRequest;
import com.example.base.auth.dto.LogoutRequest;
import com.example.base.auth.dto.RefreshRequest;
import com.example.base.auth.dto.TokenResponse;
import com.example.base.auth.entity.*;
import com.example.base.auth.enums.AccountStatus;
import com.example.base.auth.mapper.*;
import com.example.base.auth.service.AuthService;
import com.example.base.common.exception.BizException;
import com.example.base.common.util.DigestUtil;
import com.example.base.security.config.JwtProperties;
import com.example.base.security.core.SecurityUtils;
import com.example.base.security.token.JwtTokenService;
import com.example.base.security.token.TokenPair;
import com.example.base.user.dto.UserCreateRequest;
import com.example.base.user.dto.UserDto;
import com.example.base.user.entity.UserEntity;
import com.example.base.user.mapper.UserMapper;
import com.example.base.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_MINUTES = 15;

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthAccountMapper accountMapper;
    private final AuthRefreshTokenMapper refreshTokenMapper;
    private final AuthAccessDenylistMapper accessDenylistMapper;
    private final AuthLoginLogMapper loginLogMapper;
    private final AuthRoleMapper roleMapper;
    private final AuthUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public UserDto register(UserCreateRequest request) {
        return userService.createUser(request);
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        AuthAccountEntity account = findAccount(request.getIdentityType(), request.getIdentifier());
        UserEntity user = userMapper.selectById(account.getUserId());
        if (user == null) {
            throw new BizException("USER_NOT_FOUND", "User not found");
        }
        validateAccountStatus(account);
        validateUserStatus(user);

        if (!passwordEncoder.matches(request.getCredential(), Optional.ofNullable(account.getCredentialHash()).orElse(""))) {
            handleFailedLogin(account, user, request.getClientId(), "INVALID_CREDENTIALS");
            throw new BizException("INVALID_CREDENTIALS", "Invalid username or password");
        }
        resetAccountLockState(account);

        updateLoginTimestamp(account, user);
        List<String> roleCodes = fetchUserRoleCodes(user.getId());
        TokenPair tokenPair = jwtTokenService.createTokenPair(user, account, roleCodes, request.getClientId());
        persistRefreshToken(user.getId(), tokenPair);
        enforceRefreshTokenQuota(user.getId());

        recordLoginLog(user.getId(), account.getId(), true, null);

        UserDto dto = userService.getUser(user.getId());
        return TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(tokenPair.getAccessToken())
                .expiresIn(jwtProperties.getAccessTokenTtl().toSeconds())
                .refreshToken(tokenPair.getRefreshToken())
                .jti(tokenPair.getJti())
                .user(dto)
                .build();
    }

    @Override
    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String tokenHash = DigestUtil.sha256Hex(request.getRefreshToken());
        AuthRefreshTokenEntity entity = refreshTokenMapper.selectOne(new LambdaQueryWrapper<AuthRefreshTokenEntity>()
                .eq(AuthRefreshTokenEntity::getTokenHash, tokenHash));
        if (entity == null) {
            throw new BizException("INVALID_REFRESH_TOKEN", "Refresh token invalid");
        }
        if (entity.getRevokedAt() != null) {
            throw new BizException("REFRESH_TOKEN_REVOKED", "Refresh token revoked");
        }
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException("REFRESH_TOKEN_EXPIRED", "Refresh token expired");
        }
        UserEntity user = userMapper.selectById(entity.getUserId());
        if (user == null) {
            throw new BizException("USER_NOT_FOUND", "User not found");
        }
        validateUserStatus(user);

        AuthAccountEntity account = findPrimaryAccountByUser(user.getId());
        String clientId = Optional.ofNullable(request.getClientId()).orElse(entity.getClientId());
        List<String> roleCodes = fetchUserRoleCodes(user.getId());
        TokenPair newToken = jwtTokenService.createTokenPair(user, account, roleCodes, clientId);

        entity.setRevokedAt(LocalDateTime.now());
        entity.setRevokedReason("ROTATED");
        refreshTokenMapper.updateById(entity);
        persistRefreshToken(user.getId(), newToken);
        enforceRefreshTokenQuota(user.getId());

        UserDto dto = userService.getUser(user.getId());
        return TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(newToken.getAccessToken())
                .expiresIn(jwtProperties.getAccessTokenTtl().toSeconds())
                .refreshToken(newToken.getRefreshToken())
                .jti(newToken.getJti())
                .user(dto)
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        SecurityUtils.getCurrentUser().ifPresent(principal -> {
            boolean allSessions = request != null && request.isAllSessions();
            if (allSessions) {
                bumpUserTokenVersion(principal.getUserId());
                revokeAllRefreshTokens(principal.getUserId());
            } else {
                revokeRefreshTokensByClient(principal.getUserId(), principal.getClientId());
            }
            denyAccessToken(principal.getJti(), principal.getAccessTokenExpiresAt());
        });
    }

    @Override
    public UserDto currentUser() {
        return SecurityUtils.getCurrentUserId()
                .map(userService::getUser)
                .orElseThrow(() -> new BizException("UNAUTHORIZED", "Not authenticated"));
    }

    private AuthAccountEntity findAccount(String identityType, String identifier) {
        AuthAccountEntity account = accountMapper.selectOne(new LambdaQueryWrapper<AuthAccountEntity>()
                .eq(AuthAccountEntity::getIdentityType, identityType)
                .eq(AuthAccountEntity::getIdentifier, identifier));
        if (account == null) {
            throw new BizException("INVALID_CREDENTIALS", "Invalid username or password");
        }
        return account;
    }

    private void validateAccountStatus(AuthAccountEntity account) {
        if (account.getStatus() == AccountStatus.DISABLED) {
            throw new BizException("ACCOUNT_DISABLED", "Account disabled");
        }
        if (account.getLockUntil() != null && account.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BizException("ACCOUNT_LOCKED", "Account locked");
        }
    }

    private void validateUserStatus(UserEntity user) {
        switch (user.getStatus()) {
            case DISABLED -> throw new BizException("USER_DISABLED", "User disabled");
            case LOCKED -> throw new BizException("USER_LOCKED", "User locked");
            default -> {
            }
        }
    }

    private void handleFailedLogin(AuthAccountEntity account, UserEntity user, String clientId, String reason) {
        int attempts = Optional.ofNullable(account.getFailedAttempts()).orElse(0) + 1;
        account.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            account.setLockUntil(LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_MINUTES));
        }
        accountMapper.updateById(account);
        recordLoginLog(user.getId(), account.getId(), false, reason);
        log.warn("Login failed for account {} from client {}. Attempts={}", account.getIdentifier(), clientId, attempts);
    }

    private void resetAccountLockState(AuthAccountEntity account) {
        account.setFailedAttempts(0);
        account.setLockUntil(null);
        accountMapper.updateById(account);
    }

    private void updateLoginTimestamp(AuthAccountEntity account, UserEntity user) {
        LocalDateTime now = LocalDateTime.now();
        account.setLastLoginAt(now);
        accountMapper.updateById(account);
        userMapper.updateById(user.toBuilder().lastLoginAt(now).build());
    }

    private List<String> fetchUserRoleCodes(Long userId) {
        List<AuthUserRoleEntity> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<AuthUserRoleEntity>()
                .eq(AuthUserRoleEntity::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> roleIds = userRoles.stream().map(AuthUserRoleEntity::getRoleId).collect(Collectors.toSet());
        List<AuthRoleEntity> roles = roleMapper.selectBatchIds(roleIds);
        if (CollUtil.isEmpty(roles)) {
            return Collections.emptyList();
        }
        return roles.stream().map(AuthRoleEntity::getCode).collect(Collectors.toList());
    }

    private AuthAccountEntity findPrimaryAccountByUser(Long userId) {
        AuthAccountEntity account = accountMapper.selectOne(new LambdaQueryWrapper<AuthAccountEntity>()
                .eq(AuthAccountEntity::getUserId, userId)
                .orderByDesc(AuthAccountEntity::getIsPrimary)
                .last("limit 1"));
        if (account == null) {
            throw new BizException("ACCOUNT_NOT_FOUND", "Account not found");
        }
        return account;
    }

    private void persistRefreshToken(Long userId, TokenPair tokenPair) {
        LocalDateTime issuedAt = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        LocalDateTime expiresAt = LocalDateTime.ofInstant(tokenPair.getRefreshTokenExpiresAt(), ZoneId.systemDefault());
        AuthRefreshTokenEntity entity = AuthRefreshTokenEntity.builder()
                .userId(userId)
                .clientId(Optional.ofNullable(tokenPair.getClientId()).orElse("web"))
                .tokenHash(DigestUtil.sha256Hex(tokenPair.getRefreshToken()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
        refreshTokenMapper.insert(entity);
    }

    private void enforceRefreshTokenQuota(Long userId) {
        Integer limit = jwtProperties.getMaxRefreshTokenPerUser();
        if (limit == null || limit <= 0) {
            return;
        }
        List<AuthRefreshTokenEntity> tokens = refreshTokenMapper.selectList(new LambdaQueryWrapper<AuthRefreshTokenEntity>()
                .eq(AuthRefreshTokenEntity::getUserId, userId)
                .orderByDesc(AuthRefreshTokenEntity::getCreatedAt));
        if (tokens.size() <= limit) {
            return;
        }
        tokens.stream().skip(limit).forEach(token -> refreshTokenMapper.deleteById(token.getId()));
    }

    private void recordLoginLog(Long userId, Long accountId, boolean success, String reason) {
        HttpServletRequest request = currentHttpRequest();
        AuthLoginLogEntity logEntity = AuthLoginLogEntity.builder()
                .userId(userId)
                .accountId(accountId)
                .success(success)
                .reason(reason)
                .ipAddress(request != null ? request.getRemoteAddr() : null)
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .loginAt(LocalDateTime.now())
                .build();
        loginLogMapper.insert(logEntity);
    }

    private void revokeAllRefreshTokens(Long userId) {
        List<AuthRefreshTokenEntity> tokens = refreshTokenMapper.selectList(new LambdaQueryWrapper<AuthRefreshTokenEntity>()
                .eq(AuthRefreshTokenEntity::getUserId, userId)
                .isNull(AuthRefreshTokenEntity::getRevokedAt));
        LocalDateTime now = LocalDateTime.now();
        tokens.forEach(token -> {
            token.setRevokedAt(now);
            token.setRevokedReason("LOGOUT_ALL");
            refreshTokenMapper.updateById(token);
        });
    }

    private void revokeRefreshTokensByClient(Long userId, String clientId) {
        List<AuthRefreshTokenEntity> tokens = refreshTokenMapper.selectList(new LambdaQueryWrapper<AuthRefreshTokenEntity>()
                .eq(AuthRefreshTokenEntity::getUserId, userId)
                .eq(AuthRefreshTokenEntity::getClientId, clientId)
                .isNull(AuthRefreshTokenEntity::getRevokedAt));
        LocalDateTime now = LocalDateTime.now();
        tokens.forEach(token -> {
            token.setRevokedAt(now);
            token.setRevokedReason("LOGOUT");
            refreshTokenMapper.updateById(token);
        });
    }

    private void bumpUserTokenVersion(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user != null) {
            int currentVersion = Optional.ofNullable(user.getTokenVersion()).orElse(0);
            userMapper.updateById(user.toBuilder().tokenVersion(currentVersion + 1).build());
        }
    }

    private void denyAccessToken(String jti, Instant expiresAt) {
        AuthAccessDenylistEntity entity = AuthAccessDenylistEntity.builder()
                .jtiHash(DigestUtil.sha256Hex(jti))
                .expiresAt(LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()))
                .reason("LOGOUT")
                .build();
        accessDenylistMapper.insert(entity);
    }

    private HttpServletRequest currentHttpRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }
}
