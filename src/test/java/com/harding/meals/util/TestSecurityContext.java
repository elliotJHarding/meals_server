package com.harding.meals.util;

import com.harding.meals.config.security.GoogleJwtAuthenticationToken;
import com.harding.meals.entity.user.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * Utilities for creating authenticated security contexts in tests.
 */
public class TestSecurityContext {

    /**
     * Set up an authenticated security context for the given user
     */
    public static void setAuthentication(AppUser user) {
        GoogleJwtAuthenticationToken authentication =
            GoogleJwtAuthenticationToken.authenticated(user, null, null);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    /**
     * Clear the security context
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Get the current authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get the current authenticated user
     */
    public static AppUser getCurrentUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUser) {
            return (AppUser) authentication.getPrincipal();
        }
        return null;
    }
}
