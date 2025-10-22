package com.example.base.security.token;

import cn.hutool.core.util.IdUtil;
import com.example.base.auth.entity.AuthAccountEntity;
import com.example.base.security.config.JwtProperties;
import com.example.base.user.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties properties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenPair createTokenPair(UserEntity user, AuthAccountEntity account, List<String> roles, String clientId) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(properties.getAccessTokenTtl());
        Instant refreshExpiresAt = now.plus(properties.getRefreshTokenTtl());
        String jti = IdUtil.fastUUID();
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenant", user.getTenantId());
        claims.put("roles", roles);
        claims.put("pver", account.getPasswordVersion());
        claims.put("tver", user.getTokenVersion());
        claims.put("aid", account.getId());
        claims.put("cid", clientId);
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setId(jti)
                .setIssuer(properties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(accessExpiresAt))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        String refreshToken = IdUtil.fastUUID() + IdUtil.fastUUID();
        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .jti(jti)
                .clientId(clientId)
                .accessTokenExpiresAt(accessExpiresAt)
                .refreshTokenExpiresAt(refreshExpiresAt)
                .build();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
