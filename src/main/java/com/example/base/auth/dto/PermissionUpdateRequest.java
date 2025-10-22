package com.example.base.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PermissionUpdateRequest {
    String name;
    String resource;
    String httpMethod;
    String action;
    Long parentId;
    Integer orderNo;
    Integer status;
}
