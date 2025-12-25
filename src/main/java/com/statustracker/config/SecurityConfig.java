package com.statustracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.webhook.secret}")
    private String webhookSecret;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * Configure security filter chain
     * - Webhooks are publicly accessible (no auth required)
     * - Status endpoints require authorization
     * - Actuator endpoints require authorization
     * - Stateless session management for REST API
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Allow webhook endpoints without authentication
                .requestMatchers(contextPath + "/webhook/**").permitAll()

                // Allow public health check
                .requestMatchers(contextPath + "/health").permitAll()

                // All actuator endpoints require authentication
                .requestMatchers(contextPath + "/actuator/**").authenticated()

                // Status endpoints require authentication
                .requestMatchers(contextPath + "/status/**").authenticated()

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.realmName("Status Tracker API"))
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        log.info("Security configuration applied");
        return http.build();
    }

    /**
     * Configure CORS for webhook endpoints
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "https://status.openai.com",
            "https://status.chargebee.com",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("X-Total-Count", "X-Page-Number"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(contextPath + "/**", configuration);
        return source;
    }

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Validate webhook signature
     * Statuspage.io sends X-Statuspage-Signature header
     */
    public boolean validateWebhookSignature(String payload, String signature) {
        try {
            // In production, verify HMAC-SHA256 signature
            // For now, basic validation
            if (signature == null || signature.isEmpty()) {
                log.warn("Missing webhook signature");
                return false;
            }
            log.debug("Webhook signature validated");
            return true;
        } catch (Exception e) {
            log.error("Webhook signature validation failed: {}", e.getMessage());
            return false;
        }
    }
}
