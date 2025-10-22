package com.example.base.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.base.auth.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("auth_permission")
public class AuthPermissionEntity {

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

    private PermissionType type;

    private String code;

    private String name;

    private String resource;

    private String httpMethod;

    private String action;

    private Long parentId;

    private Integer orderNo;

    private Integer status;
}
