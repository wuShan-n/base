package com.example.security.config;

import com.example.security.jwt.JwtKeyProvider;
import com.example.security.jwt.JwtProperties;
import com.example.security.jwt.JwtService;
import com.example.security.web.RestAccessDeniedHandler;
import com.example.security.web.RestAuthenticationEntryPoint;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityCommonConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 支持 {bcrypt},{noop},{pbkdf2},{scrypt},{argon2} 等，默认 bcrypt
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler() {
        return new RestAccessDeniedHandler();
    }

    @Bean
    public JwtKeyProvider jwtKeyProvider(JwtProperties props) {
        return new JwtKeyProvider(props);
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtKeyProvider keyProvider, JwtProperties props) {
        RSAPublicKey publicKey = keyProvider.getPublicKey();
        RSAPrivateKey privateKey = keyProvider.getPrivateKey();
        var jwk = new com.nimbusds.jose.jwk.RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(props.getKeyId())
                .build();
        var jwks = new com.nimbusds.jose.jwk.JWKSet(jwk);
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(jwks));
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtKeyProvider keyProvider) {
        return NimbusJwtDecoder.withPublicKey(keyProvider.getPublicKey()).build();
    }

    @Bean
    public JwtService jwtService(JwtEncoder encoder, JwtDecoder decoder, JwtProperties props) {
        return new JwtService(encoder, decoder, props);
    }
}
