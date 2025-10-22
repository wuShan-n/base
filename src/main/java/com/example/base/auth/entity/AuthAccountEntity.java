package com.example.base.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.base.auth.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "auth_account", autoResultMap = true)
public class AuthAccountEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @Version
    private Long version;

    private Long userId;

    private String identityType;

    private String identifier;

    private String credentialHash;

    private Integer passwordVersion;

    private Boolean isPrimary;

    private Boolean isVerified;

    private LocalDateTime verifiedAt;

    private Integer failedAttempts;

    private LocalDateTime lockUntil;

    private LocalDateTime lastLoginAt;

    @TableField(value = "meta_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> meta;

    private AccountStatus status;
}
