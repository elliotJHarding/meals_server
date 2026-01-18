package com.harding.meals.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    GoogleAuthenticationProvider authenticationProvider;

    @Autowired
    JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Security filter chain for public endpoints (no authentication required).
     * Highest priority (@Order(1)) - handles login, refresh, etc.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/auth/login",
                "/auth/refresh",
                "/units",
                "/error",
                "/actuator/health/**"
            )
            .cors(withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Security filter chain for session-authenticated requests (web + mobile hybrid).
     * Lowest priority (@Order(3)) - handles all other requests.
     *
     * This filter chain supports:
     * - Web clients: Session-based authentication with cookies
     * - Mobile clients: JWT tokens via Bearer header (oauth2ResourceServer)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain sessionSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .logout(logout -> logout.logoutUrl("/auth/logout"))
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            )
            .csrf(AbstractHttpConfigurer::disable)
            .securityContext(securityContext ->
                securityContext.requireExplicitSave(true));

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.authenticationProvider(authenticationProvider);

        return authenticationManagerBuilder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://192.168.1.159:5173",
                "http://grubplanner.co.uk",
                "https://grubplanner.co.uk"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Client-Type"
        ));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
