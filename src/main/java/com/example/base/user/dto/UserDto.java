package com.example.base.user.dto;

import com.example.base.auth.dto.AccountDto;
import com.example.base.auth.dto.RoleDto;
import com.example.base.common.enums.Gender;
import com.example.base.common.enums.UserStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class UserDto {
    Long id;
    Long tenantId;
    String nickname;
    String avatarUrl;
    Gender gender;
    String timezone;
    String locale;
    UserStatus status;
    Integer tokenVersion;
    LocalDateTime lastLoginAt;
    String remark;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<AccountDto> accounts;
    List<RoleDto> roles;
}
