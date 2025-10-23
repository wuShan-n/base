package com.example.base.auth.dto;

import com.example.base.user.dto.UserDto;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@Builder
public class CurrentUserResponse {
    UserDto user;
    @Builder.Default
    List<RoleDto> roles = Collections.emptyList();
    @Builder.Default
    List<String> permissions = Collections.emptyList();
}
