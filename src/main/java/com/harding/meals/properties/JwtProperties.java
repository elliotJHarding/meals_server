package com.harding.meals.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JWT token generation and validation.
 *
 * Properties are loaded from application.properties with prefix "app.jwt"
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Access token validity in seconds (default: 15 minutes)
     */
    private long accessTokenValidity = 900;

    /**
     * Refresh token validity in seconds (default: 7 days)
     */
    private long refreshTokenValidity = 604800;

    /**
     * Enable refresh token rotation (recommended for security)
     * When enabled, a new refresh token is issued on each refresh
     * and the old one is revoked.
     */
    private boolean enableTokenRotation = true;

    /**
     * JWT issuer claim
     */
    private String issuer = "com.harding.meals";

    /**
     * JWT audience claim
     */
    private String audience = "meals-mobile-app";

    // Getters and setters

    public long getAccessTokenValidity() {
        return accessTokenValidity;
    }

    public void setAccessTokenValidity(long accessTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
    }

    public long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    public void setRefreshTokenValidity(long refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public boolean isEnableTokenRotation() {
        return enableTokenRotation;
    }

    public void setEnableTokenRotation(boolean enableTokenRotation) {
        this.enableTokenRotation = enableTokenRotation;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
