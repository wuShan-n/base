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
@TableName("auth_login_log")
public class AuthLoginLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private Long tenantId;

    @TableField("user_id")
    private Long userId;

    @TableField("account_id")
    private Long accountId;

    @TableField("success")
    private Boolean success;

    @TableField("reason")
    private String reason;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField(value = "login_at", fill = FieldFill.INSERT)
    private LocalDateTime loginAt;

    @TableField("geo")
    private String geo;
}
