package com.harding.meals.controller;

import com.harding.meals.dto.AppUserDto;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.PublicDetails;
import com.harding.meals.mapping.UserMapper;
import com.harding.meals.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.isNull;

/**
 * Session login without Google, for local development and UI test automation
 * (Playwright) only. Active solely under the 'localdev' Spring profile —
 * never enabled in deployed environments.
 *
 * Establishes the session exactly as AuthController.login does (security
 * context saved to the HTTP session).
 */
@RestController
@RequestMapping("/auth")
@Profile("localdev")
public class DevAuthController {

    private static final Logger log = LoggerFactory.getLogger(DevAuthController.class);

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    private final AppUserRepository appUserRepository;
    private final UserMapper userMapper;

    public DevAuthController(AppUserRepository appUserRepository, UserMapper userMapper) {
        this.appUserRepository = appUserRepository;
        this.userMapper = userMapper;
    }

    @PostMapping("/dev-login")
    public AppUserDto devLogin(@RequestParam String email,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        log.warn("DEV LOGIN for {} — localdev profile only", email);

        AppUser user = appUserRepository.findByEmail(email);
        if (isNull(user)) {
            user = new AppUser();
            user.setEmail(email);
            user.setUsername("dev_" + email.replace("@", "_").replace(".", "_"));
            PublicDetails publicDetails = new PublicDetails();
            publicDetails.setName("Dev " + email.substring(0, email.indexOf('@')));
            publicDetails.setEmailVerified(true);
            user.setPublicDetails(publicDetails);
            user = appUserRepository.save(user);
        }

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(user, null, List.of());

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return userMapper.toDto(user.getPublicDetails());
    }
}
