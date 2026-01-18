package com.harding.meals.entity.user.token;

import com.harding.meals.entity.user.AppUser;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "app_jwt_token",
    indexes = {
        @Index(name = "idx_jwt_token_user", columnList = "user_id"),
        @Index(name = "idx_jwt_token_expires", columnList = "expires_at"),
        @Index(name = "idx_jwt_token_jti", columnList = "jti", unique = true)
    }
)
public class AppJwtToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * JWT ID (jti claim) for tracking and revocation
     */
    @Column(nullable = false, unique = true, length = 255)
    private String jti;

    /**
     * Token type: REFRESH or ACCESS (for tracking purposes)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenType tokenType;

    /**
     * Associated user
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    /**
     * Expiration time
     */
    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    /**
     * Creation time
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    /**
     * Revocation tracking
     */
    @Column(nullable = false)
    private boolean revoked = false;

    @Column
    private OffsetDateTime revokedAt;

    /**
     * Device binding (optional - Android device ID)
     */
    @Column(length = 255)
    private String deviceId;

    /**
     * IP address for security tracking
     */
    @Column(length = 45)
    private String ipAddress;

    /**
     * User agent for tracking
     */
    @Column(length = 500)
    private String userAgent;

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    // Constructors
    public AppJwtToken() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
