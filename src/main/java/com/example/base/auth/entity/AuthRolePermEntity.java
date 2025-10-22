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
@TableName("auth_role_perm")
public class AuthRolePermEntity {

    @TableId(value = "role_id", type = IdType.INPUT)
    private Long roleId;

    @TableField("perm_id")
    private Long permId;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private Long tenantId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
