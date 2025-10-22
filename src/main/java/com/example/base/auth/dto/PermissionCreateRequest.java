package com.example.base.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PermissionCreateRequest {
    @Builder.Default
    String type = "api";
    @NotBlank
    String code;
    @NotBlank
    String name;
    String resource;
    String httpMethod;
    String action;
    Long parentId;
    @Builder.Default
    Integer orderNo = 0;
    @Builder.Default
    Integer status = 1;
}
