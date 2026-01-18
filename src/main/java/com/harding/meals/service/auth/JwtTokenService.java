package com.harding.meals.service.auth;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.token.AppJwtToken;
import com.harding.meals.properties.JwtProperties;
import com.harding.meals.repository.AppJwtTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service for generating, validating, and refreshing JWT tokens.
 *
 * This service handles:
 * - Access token generation (short-lived, 15 minutes)
 * - Refresh token generation (long-lived, 7 days)
 * - Token refresh with optional rotation
 * - Token revocation
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;
    private final AppJwtTokenRepository tokenRepository;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            JwtProperties jwtProperties,
            AppJwtTokenRepository tokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Generates both access and refresh tokens for a user.
     *
     * @param user    the authenticated user
     * @param request the HTTP request for tracking client info
     * @return token pair containing access token, refresh token, and expiry
     */
    @Transactional
    public TokenPair generateTokenPair(AppUser user, HttpServletRequest request) {
        log.debug("Generating token pair for user: {}", user.getEmail());

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user, request);

        return new TokenPair(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenValidity());
    }

    /**
     * Generates a short-lived access token.
     * Access tokens are used for API authentication and expire quickly.
     *
     * @param user the user to generate token for
     * @return JWT access token string
     */
    private String generateAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessTokenValidity(), ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .audience(java.util.List.of(jwtProperties.getAudience()))
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("name", user.getPublicDetails().getName())
                .claim("type", "access")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        log.debug("Generated access token for user {} (expires at: {})", user.getEmail(), expiry);

        return token;
    }

    /**
     * Generates a long-lived refresh token and stores it in database.
     * Refresh tokens are used to obtain new access tokens without
     * re-authentication.
     *
     * @param user    the user to generate token for
     * @param request the HTTP request for tracking
     * @return JWT refresh token string
     */
    private String generateRefreshToken(AppUser user, HttpServletRequest request) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefreshTokenValidity(), ChronoUnit.SECONDS);
        String jti = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .audience(java.util.List.of(jwtProperties.getAudience()))
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(expiry)
                .id(jti)
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        // Store refresh token metadata in database for tracking and revocation
        AppJwtToken tokenEntity = new AppJwtToken();
        tokenEntity.setJti(jti);
        tokenEntity.setTokenType(AppJwtToken.TokenType.REFRESH);
        tokenEntity.setUser(user);
        tokenEntity.setExpiresAt(OffsetDateTime.ofInstant(expiry, java.time.ZoneOffset.UTC));
        tokenEntity.setCreatedAt(OffsetDateTime.now());
        tokenEntity.setRevoked(false);

        // Optional: Store device/client info for security tracking
        if (request != null) {
            tokenEntity.setIpAddress(getClientIp(request));
            tokenEntity.setUserAgent(request.getHeader("User-Agent"));
        }

        tokenRepository.save(tokenEntity);
        log.debug("Generated and stored refresh token for user {} (jti: {}, expires at: {})",
                user.getEmail(), jti, expiry);

        return token;
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * Optionally rotates the refresh token for enhanced security.
     *
     * @param refreshToken the refresh token
     * @param request      the HTTP request for tracking
     * @return new token pair
     * @throws InvalidTokenException if token is invalid, expired, or revoked
     */
    @Transactional
    public TokenPair refreshAccessToken(String refreshToken, HttpServletRequest request) {
        log.debug("Attempting to refresh access token");

        // Decode and validate refresh token
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(refreshToken);
        } catch (JwtException e) {
            log.warn("Failed to decode refresh token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid refresh token", e);
        }

        // Verify it's a refresh token
        if (!"refresh".equals(jwt.getClaim("type"))) {
            log.warn("Token type mismatch: expected 'refresh', got '{}'", (String) jwt.getClaim("type"));
            throw new InvalidTokenException("Token is not a refresh token");
        }

        // Check if token is revoked in database
        String jti = jwt.getId();
        AppJwtToken tokenEntity = tokenRepository.findByJti(jti)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in database (jti: {})", jti);
                    return new InvalidTokenException("Refresh token not found");
                });

        if (tokenEntity.isRevoked()) {
            log.warn("Attempted to use revoked refresh token (jti: {}, user: {})",
                    jti, tokenEntity.getUser().getEmail());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (tokenEntity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            log.warn("Refresh token has expired (jti: {}, expired at: {})",
                    jti, tokenEntity.getExpiresAt());
            throw new InvalidTokenException("Refresh token has expired");
        }

        AppUser user = tokenEntity.getUser();
        log.debug("Refresh token validated for user: {}", user.getEmail());

        // Generate new access token
        String newAccessToken = generateAccessToken(user);

        // Token rotation: generate new refresh token
        String newRefreshToken = null;
        if (jwtProperties.isEnableTokenRotation()) {
            log.debug("Token rotation enabled, generating new refresh token");

            // Revoke old refresh token
            tokenEntity.setRevoked(true);
            tokenEntity.setRevokedAt(OffsetDateTime.now());
            tokenRepository.save(tokenEntity);

            // Generate new refresh token
            newRefreshToken = generateRefreshToken(user, request);

            log.debug("Old refresh token revoked and new one generated");
        }

        return new TokenPair(
                newAccessToken,
                newRefreshToken,
                jwtProperties.getAccessTokenValidity());
    }

    /**
     * Revokes a specific refresh token by its JWT ID.
     *
     * @param jti the JWT ID to revoke
     */
    @Transactional
    public void revokeRefreshToken(String jti) {
        log.debug("Revoking refresh token with jti: {}", jti);

        tokenRepository.findByJti(jti).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(OffsetDateTime.now());
            tokenRepository.save(token);
            log.info("Revoked refresh token (jti: {}, user: {})", jti, token.getUser().getEmail());
        });
    }

    /**
     * Revokes all refresh tokens for a user (logout all devices).
     *
     * @param user the user whose tokens should be revoked
     */
    @Transactional
    public void revokeAllUserTokens(AppUser user) {
        log.info("Revoking all tokens for user: {}", user.getEmail());
        tokenRepository.revokeAllForUser(user.getId());
    }

    /**
     * Extracts the client IP address from the request.
     * Handles X-Forwarded-For header for proxied requests.
     *
     * @param request the HTTP request
     * @return client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Holder for access and refresh token pair.
     *
     * @param accessToken  the access token
     * @param refreshToken the refresh token (may be null if not rotating)
     * @param expiresIn    seconds until access token expires
     */
    public record TokenPair(
            String accessToken,
            String refreshToken,
            long expiresIn) {
    }

    /**
     * Exception thrown when token validation fails.
     */
    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }

        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
