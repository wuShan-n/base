package com.example.base.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoleCreateRequest {
    @NotBlank
    String code;
    @NotBlank
    String name;
    @Builder.Default
    Integer level = 100;
    @Builder.Default
    Integer status = 1;
    String remark;
}
