package com.harding.meals.controller;

import com.harding.meals.config.security.GoogleJwtAuthenticationToken;
import com.harding.meals.dto.AppUserDto;
import com.harding.meals.dto.LoginRequest;
import com.harding.meals.dto.LoginResponse;
import com.harding.meals.dto.RefreshTokenRequest;
import com.harding.meals.dto.TokenResponse;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.UserMapper;
import com.harding.meals.service.auth.JwtTokenService;
import com.harding.meals.service.auth.google.GoogleAuthService;
import com.harding.meals.service.auth.google.VerifyGoogleJwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
        SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();

    private final AuthenticationManager authenticationManager;
    private final VerifyGoogleJwtService verifyJwtService;
    private final UserMapper userMapper;
    private final JwtTokenService jwtTokenService;
    private final GoogleAuthService googleAuthService;

    public AuthController(
        VerifyGoogleJwtService verifyJwtService,
        UserMapper userMapper,
        AuthenticationManager authenticationManager,
        JwtTokenService jwtTokenService,
        GoogleAuthService googleAuthService
    ) {
        this.verifyJwtService = verifyJwtService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.googleAuthService = googleAuthService;
    }

    @GetMapping("/whoami")
    public AppUserDto whoAmI(Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();
        return userMapper.toDto(user.getPublicDetails());
    }

    /**
     * Login endpoint that supports both web (session) and mobile (JWT) clients.
     *
     * - Web clients: Receive session cookie + user details
     * - Mobile clients: Receive JWT tokens + user details
     *
     * Detection: Mobile clients should send "User-Agent" header containing "Android" or "iOS"
     * or explicit header "X-Client-Type: mobile"
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @RequestBody LoginRequest loginRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws GeneralSecurityException, IOException {

        log.debug("Login requested");

        if (isNull(loginRequest.getToken())) {
            throw new IllegalArgumentException("No token provided");
        }

        GoogleJwtAuthenticationToken token =
            GoogleJwtAuthenticationToken.unauthenticated(loginRequest.getToken());

        log.debug("Authenticating token with provider");
        Authentication authentication = authenticationManager.authenticate(token);

        AppUser principal = (AppUser) authentication.getPrincipal();

        // Best-effort offline grant from a native serverAuthCode. Identity login must
        // still succeed if this fails (the user can re-consent later via linkCalendar).
        if (nonNull(loginRequest.getAuthCode())) {
            try {
                googleAuthService.authorizeMobile(loginRequest.getAuthCode(), principal);
            } catch (Exception e) {
                log.warn("offline grant via authCode failed for {}: {}", principal.getEmail(), e.getMessage());
            }
        }

        // Create security context for session
        log.debug("Creating security context");
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        log.debug("Saving security context");
        securityContextRepository.saveContext(context, request, response);

        // Determine client type
        boolean isMobileClient = isMobileClient(request);

        if (isMobileClient) {
            // Generate JWT tokens for mobile client
            log.debug("Generating JWT tokens for mobile client");
            JwtTokenService.TokenPair tokenPair =
                jwtTokenService.generateTokenPair(principal, request);

            LoginResponse loginResponse = new LoginResponse()
                .user(userMapper.toDto(principal.getPublicDetails()))
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .expiresIn(tokenPair.expiresIn())
                .tokenType("Bearer");

            return ResponseEntity.ok(loginResponse);
        } else {
            // Return only user details for web client (session handled by cookie)
            log.debug("Returning user details for web client");
            return ResponseEntity.ok(userMapper.toDto(principal.getPublicDetails()));
        }
    }

    /**
     * Refresh token endpoint for mobile clients.
     *
     * Validates the refresh token, generates a new access token,
     * and optionally rotates the refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
        @RequestBody RefreshTokenRequest request,
        HttpServletRequest httpRequest
    ) {
        try {
            log.debug("Token refresh requested");

            JwtTokenService.TokenPair tokenPair =
                jwtTokenService.refreshAccessToken(request.getRefreshToken(), httpRequest);

            TokenResponse response = new TokenResponse()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .expiresIn(tokenPair.expiresIn())
                .tokenType("Bearer");

            return ResponseEntity.ok(response);

        } catch (JwtTokenService.InvalidTokenException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Logout endpoint that clears session and revokes JWT tokens.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AppUser user) {
            // Revoke all JWT tokens for this user
            jwtTokenService.revokeAllUserTokens(user);
        }

        // Clear security context (session)
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }

    /**
     * Determines if the request is from a mobile client.
     */
    private boolean isMobileClient(HttpServletRequest request) {
        // Check explicit client type header
        String clientType = request.getHeader("X-Client-Type");
        if ("mobile".equalsIgnoreCase(clientType)) {
            return true;
        }

        // Check User-Agent for mobile indicators
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            return ua.contains("android") ||
                   ua.contains("iphone") ||
                   ua.contains("ipad") ||
                   ua.contains("mobile");
        }

        return false;
    }

}
