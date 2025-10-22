package com.example.base.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoleUpdateRequest {
    String name;
    Integer level;
    Integer status;
    String remark;
}
