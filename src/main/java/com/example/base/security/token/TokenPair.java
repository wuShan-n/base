package com.example.base.security.token;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class TokenPair {
    String accessToken;
    String refreshToken;
    String jti;
    String clientId;
    Instant accessTokenExpiresAt;
    Instant refreshTokenExpiresAt;
}
