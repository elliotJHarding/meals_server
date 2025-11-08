package com.harding.meals.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that can be imported for easier testing.
 * This configuration can be used to override the main security configuration
 * when you need more permissive access for testing.
 *
 * Note: Most tests should use the default security configuration with
 * Spring Security Test utilities (e.g., @WithMockUser, SecurityMockMvcRequestPostProcessors.user())
 * rather than disabling security entirely.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Create a security filter chain for tests that disables all security.
     * Use sparingly - prefer using Spring Security Test utilities instead.
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
