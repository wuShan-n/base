package com.example.base.auth.dto;

import com.example.base.auth.enums.PermissionType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class PermissionDto {
    Long id;
    Long tenantId;
    PermissionType type;
    String code;
    String name;
    String resource;
    String httpMethod;
    String action;
    Long parentId;
    Integer orderNo;
    Integer status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
