package com.example.base.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.base.auth.dto.AccountCreateRequest;
import com.example.base.auth.dto.AccountDto;
import com.example.base.auth.entity.AuthAccountEntity;
import com.example.base.auth.enums.AccountStatus;
import com.example.base.auth.mapper.AuthAccountMapper;
import com.example.base.auth.service.AccountService;
import com.example.base.common.exception.BizException;
import com.example.base.security.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AuthAccountMapper, AuthAccountEntity> implements AccountService {

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AccountDto createAccount(Long userId, AccountCreateRequest request) {
        Assert.notNull(userId, "userId must not be null");
        validateUniqueIdentifier(request.getIdentityType(), request.getIdentifier());
        AuthAccountEntity account = AuthAccountEntity.builder()
                .userId(userId)
                .identityType(request.getIdentityType())
                .identifier(request.getIdentifier())
                .credentialHash(request.getCredential() == null ? null : passwordEncoder.encode(request.getCredential()))
                .passwordVersion(0)
                .isPrimary(request.isPrimary())
                .isVerified(false)
                .failedAttempts(0)
                .status(AccountStatus.ACTIVE)
                .build();
        save(account);
        return toDto(account);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId, Long accountId) {
        AuthAccountEntity account = getById(accountId);
        if (account == null || !account.getUserId().equals(userId)) {
            throw new BizException("ACCOUNT_NOT_FOUND", "Account not found for user");
        }
        removeById(accountId);
    }

    @Override
    public List<AccountDto> listAccountsByUserId(Long userId) {
        List<AuthAccountEntity> accounts = list(new LambdaQueryWrapper<AuthAccountEntity>()
                .eq(AuthAccountEntity::getUserId, userId));
        if (CollUtil.isEmpty(accounts)) {
            return Collections.emptyList();
        }
        return accounts.stream().map(this::toDto).toList();
    }

    @Override
    public Optional<AccountDto> findByIdentity(Long tenantId, String identityType, String identifier) {
        AuthAccountEntity entity = getOne(new LambdaQueryWrapper<AuthAccountEntity>()
                .eq(AuthAccountEntity::getTenantId, tenantId)
                .eq(AuthAccountEntity::getIdentityType, identityType)
                .eq(AuthAccountEntity::getIdentifier, identifier), false);
        return Optional.ofNullable(entity).map(this::toDto);
    }

    private void validateUniqueIdentifier(String identityType, String identifier) {
        Long tenantId = TenantContext.getTenantId();
        boolean exists = lambdaQuery()
                .eq(AuthAccountEntity::getTenantId, tenantId)
                .eq(AuthAccountEntity::getIdentityType, identityType)
                .eq(AuthAccountEntity::getIdentifier, identifier)
                .exists();
        if (exists) {
            throw new BizException("IDENTIFIER_EXISTS", "Identity already registered");
        }
    }

    private AccountDto toDto(AuthAccountEntity entity) {
        LocalDateTime createdAt = entity.getCreatedAt();
        LocalDateTime updatedAt = entity.getUpdatedAt();
        return AccountDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .identityType(entity.getIdentityType())
                .identifier(entity.getIdentifier())
                .passwordVersion(entity.getPasswordVersion())
                .primary(Boolean.TRUE.equals(entity.getIsPrimary()))
                .verified(Boolean.TRUE.equals(entity.getIsVerified()))
                .verifiedAt(entity.getVerifiedAt())
                .failedAttempts(entity.getFailedAttempts())
                .lockUntil(entity.getLockUntil())
                .lastLoginAt(entity.getLastLoginAt())
                .meta(entity.getMeta() == null ? Collections.emptyMap() : entity.getMeta())
                .status(entity.getStatus())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
