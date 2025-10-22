package com.example.base.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AssignRolesRequest {
    @NotEmpty
    @Singular
    List<Long> roleIds;
}
