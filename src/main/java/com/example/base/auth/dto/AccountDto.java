package com.example.base.auth.dto;

import com.example.base.auth.enums.AccountStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder(toBuilder = true)
public class AccountDto {
    Long id;
    Long tenantId;
    Long userId;
    String identityType;
    String identifier;
    Integer passwordVersion;
    boolean primary;
    boolean verified;
    LocalDateTime verifiedAt;
    Integer failedAttempts;
    LocalDateTime lockUntil;
    LocalDateTime lastLoginAt;
    Map<String, Object> meta;
    AccountStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
