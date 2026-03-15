
package com.example.freshfarmjuba.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class RateLimitingConfig {

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    /**
     * Apply rate limiting to the security filter chain
     * This method should be called from your SecurityConfig
     */
    public void configureRateLimiting(HttpSecurity http) throws Exception {
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
    }

    // You can add additional rate limiting configuration here
    // For example, different limits for different endpoints
}