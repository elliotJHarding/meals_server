package com.harding.meals.config.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.service.auth.AppUserDetailsService;
import com.harding.meals.service.auth.google.VerifyGoogleJwtService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleAuthenticationProvider implements AuthenticationProvider {

    VerifyGoogleJwtService jwtService;
    AppUserDetailsService userDetailsService;

    GoogleAuthenticationProvider(VerifyGoogleJwtService jwtService, AppUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String token = (String) authentication.getCredentials();

        try {
            GoogleIdToken googleIdToken = jwtService.verify(token);

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            AppUser principal = userDetailsService.loadUserByUsername(payload.getSubject());

            if (principal == null) {
                AppUser appUser = new AppUser(googleIdToken);
                userDetailsService.createUser(appUser);
                principal = appUser;
            }

            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            return GoogleJwtAuthenticationToken.authenticated((AppUser) principal, token, grantedAuthorities);

        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(GoogleJwtAuthenticationToken.class);
    }
}
