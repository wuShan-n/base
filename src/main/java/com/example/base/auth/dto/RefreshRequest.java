package com.example.base.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RefreshRequest {
    @NotBlank
    String refreshToken;
    @Builder.Default
    String clientId = "web";
}
