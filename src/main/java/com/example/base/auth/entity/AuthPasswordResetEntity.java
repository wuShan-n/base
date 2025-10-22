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
@TableName("auth_password_reset")
public class AuthPasswordResetEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private Long tenantId;

    @TableField("account_id")
    private Long accountId;

    @TableField("token_hash")
    private String tokenHash;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("used_at")
    private LocalDateTime usedAt;

    @TableField("request_ip")
    private String requestIp;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
