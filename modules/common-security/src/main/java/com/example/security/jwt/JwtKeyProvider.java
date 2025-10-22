package com.example.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwtKeyProvider {

    private final JwtProperties properties;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public JwtKeyProvider(JwtProperties properties) {
        this.properties = properties;
        initKeys();
    }

    public RSAPrivateKey getPrivateKey() { return privateKey; }
    public RSAPublicKey getPublicKey() { return publicKey; }

    public JWKSet jwkSet() {
        RSAKey jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(properties.getKeyId())
                .build();
        return new JWKSet(jwk);
    }

    private void initKeys() {
        try {
            if (hasText(properties.getPrivateKeyPem()) && hasText(properties.getPublicKeyPem())) {
                this.privateKey = (RSAPrivateKey) parsePrivateKeyFromPem(properties.getPrivateKeyPem());
                this.publicKey = (RSAPublicKey) parsePublicKeyFromPem(properties.getPublicKeyPem());
            } else {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(2048);
                KeyPair kp = kpg.generateKeyPair();
                this.privateKey = (RSAPrivateKey) kp.getPrivate();
                this.publicKey = (RSAPublicKey) kp.getPublic();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Init RSA keys failed", e);
        }
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static PrivateKey parsePrivateKeyFromPem(String pem) throws Exception {
        String content = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\s+", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private static PublicKey parsePublicKeyFromPem(String pem) throws Exception {
        String content = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\s+", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }
}
