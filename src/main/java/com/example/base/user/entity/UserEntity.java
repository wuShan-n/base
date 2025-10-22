package com.example.base.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.base.common.enums.Gender;
import com.example.base.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_user")
public class UserEntity {

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

    private String nickname;

    private String avatarUrl;

    private Gender gender;

    private String timezone;

    private String locale;

    private UserStatus status;

    private Integer tokenVersion;

    private LocalDateTime lastLoginAt;

    private String remark;
}
