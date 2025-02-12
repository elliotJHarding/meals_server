package com.harding.meals.controller;

import com.harding.meals.config.security.GoogleJwtAuthenticationToken;
import com.harding.meals.dto.auth.AppUserDto;
import com.harding.meals.dto.auth.LoginRequest;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.UserMapper;
import com.harding.meals.service.VerifyGoogleJwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.util.Objects.isNull;

@RestController
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();


    private final AuthenticationManager authenticationManager;
    VerifyGoogleJwtService verifyJwtService;
    UserMapper userMapper;

    public AuthController(VerifyGoogleJwtService verifyJwtService, UserMapper userMapper, AuthenticationManager authenticationManager) {
        this.verifyJwtService = verifyJwtService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/auth/whoami")
    AppUserDto whoAmI(Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();
        return userMapper.toDto(user.getPublicDetails());
    }

    @PostMapping("/auth/login")
    AppUserDto login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException, IOException {

        log.debug("Login requested");

        if (isNull(loginRequest.getToken())) {
            throw new IllegalArgumentException("No token provided");
        }

        GoogleJwtAuthenticationToken token = GoogleJwtAuthenticationToken.unauthenticated(loginRequest.getToken());

        log.debug("Authenticating token with provider");
        Authentication authentication = authenticationManager.authenticate(token);

        AppUser principal = (AppUser) authentication.getPrincipal();

        log.debug("Creating security context");
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        log.debug("Saving security context");
        securityContextRepository.saveContext(context, request, response);

        log.debug("Returning user details");
        return userMapper.toDto(principal.getPublicDetails());
    }

}
