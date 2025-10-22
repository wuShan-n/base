package com.example.base.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginRequest {
    @NotBlank
    String identityType;
    @NotBlank
    String identifier;
    @NotBlank
    String credential;
    @Builder.Default
    String clientId = "web";
}
