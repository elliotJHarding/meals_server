package com.harding.meals.config.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Configuration for JWT encoding and decoding.
 * Uses RSA keys for signing and verification.
 *
 * PRODUCTION NOTE: In production, load keys from secure storage (env vars, key vault)
 * rather than generating them at startup. This implementation generates keys on each
 * application startup, which means existing tokens will be invalidated on restart.
 *
 * For production:
 * 1. Generate keys once and store securely (AWS Secrets Manager, HashiCorp Vault, etc.)
 * 2. Load keys from environment variables or key management service
 * 3. Rotate keys periodically with proper migration strategy
 */
@Configuration
public class JwtKeyConfig {

    private final KeyPair keyPair;

    public JwtKeyConfig() throws NoSuchAlgorithmException {
        // TODO: In production, load from secure storage instead of generating
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        this.keyPair = keyPairGenerator.generateKeyPair();
    }

    /**
     * JWT encoder for signing tokens.
     * Uses the private key from the RSA key pair.
     *
     * @return configured JWT encoder
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JWT decoder for validating tokens.
     * Uses the public key from the RSA key pair.
     *
     * @return configured JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
