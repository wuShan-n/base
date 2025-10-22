package com.example.security.jwt;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;

/**
 * 简化的 JWT 服务：签发 & 解析
 */
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties props;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties props) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.props = props;
    }

    public String issueAccessToken(String subject, String tenant, Collection<String> roles, Map<String, Object> claims) {
        long ttl = props.getAccessTtlSeconds();
        return issueToken(subject, tenant, "access", ttl, roles, claims);
    }

    public String issueRefreshToken(String subject, String tenant, Map<String, Object> claims) {
        long ttl = props.getRefreshTtlSeconds();
        return issueToken(subject, tenant, "refresh", ttl, null, claims);
    }

    private String issueToken(String subject, String tenant, String typ, long ttlSeconds,
                              Collection<String> roles, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);
        String jti = UUID.randomUUID().toString();

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer(props.getIssuer())
                .issuedAt(now)
                .expiresAt(exp)
                .subject(subject)
                .id(jti)
                .claim("typ", typ);

        if (tenant != null) builder.claim("tenant", tenant);
        if (!CollectionUtils.isEmpty(roles)) builder.claim("roles", roles);
        if (claims != null) claims.forEach(builder::claim);

        JwsHeader headers = JwsHeader.with(() -> "RS256")
                .keyId(props.getKeyId())
                .type("JWT")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, builder.build())).getTokenValue();
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }
}
