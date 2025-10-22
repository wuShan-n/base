package com.example.base.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("auth_refresh_token")
public class AuthRefreshTokenEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private Long tenantId;

    @TableField("user_id")
    private Long userId;

    @TableField("client_id")
    private String clientId;

    @TableField("token_hash")
    private String tokenHash;

    @TableField("issued_at")
    private LocalDateTime issuedAt;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("revoked_at")
    private LocalDateTime revokedAt;

    @TableField("revoked_reason")
    private String revokedReason;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
