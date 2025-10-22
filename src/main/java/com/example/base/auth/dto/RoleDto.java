package com.example.base.auth.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class RoleDto {
    Long id;
    Long tenantId;
    String code;
    String name;
    Integer level;
    boolean builtIn;
    Integer status;
    String remark;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<PermissionDto> permissions;
}
