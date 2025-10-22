package com.example.base.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountCreateRequest {

    @NotBlank
    String identityType;

    @NotBlank
    String identifier;

    @Size(min = 6, max = 128)
    String credential;

    @Builder.Default
    boolean primary = true;
}
