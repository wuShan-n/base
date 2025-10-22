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
@TableName("auth_access_denylist")
public class AuthAccessDenylistEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private Long tenantId;

    @TableField("jti_hash")
    private String jtiHash;

    @TableField("reason")
    private String reason;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
