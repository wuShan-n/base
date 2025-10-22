package com.example.base.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserUpdateRequest {
    String nickname;
    Integer gender;
    String avatarUrl;
    String locale;
    String timezone;
    Integer status;
    @Size(max = 255)
    String remark;
}
