package com.harding.meals.config.security;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.repository.AppUserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Converts JWT tokens into Authentication objects with AppUser as principal.
 *
 * This is necessary because Spring Security's default JWT authentication
 * uses the Jwt object as the principal, but our controllers expect AppUser.
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AppUserRepository userRepository;

    public JwtAuthenticationConverter(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract email from JWT claims
        String email = jwt.getClaimAsString("email");

        if (email == null) {
            throw new IllegalArgumentException("JWT does not contain email claim");
        }

        // Load user from database
        AppUser user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        // Create authentication with AppUser as principal
        return new UsernamePasswordAuthenticationToken(
            user,
            jwt,
            user.getAuthorities()
        );
    }
}
