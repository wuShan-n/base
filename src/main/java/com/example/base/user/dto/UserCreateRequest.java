package com.example.base.user.dto;

import com.example.base.auth.dto.AccountCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserCreateRequest {
    @NotBlank
    String nickname;
    String locale;
    Integer gender;
    String avatarUrl;
    String timezone;
    @NotEmpty
    @Singular
    List<@Valid AccountCreateRequest> accounts;
    String remark;
}
