package com.example.base.auth.dto;

import com.example.base.user.dto.UserDto;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenResponse {
    String tokenType;
    String accessToken;
    long expiresIn;
    String refreshToken;
    String jti;
    UserDto user;
}
