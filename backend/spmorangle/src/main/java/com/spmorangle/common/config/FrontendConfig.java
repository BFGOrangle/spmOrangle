package com.spmorangle.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for frontend application URLs
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendConfig {

    /**
     * Base URL of the frontend application (e.g., "http://localhost:3000" or "https://spmorangle.com")
     * Used for generating full URLs in email notifications
     */
    private String baseUrl;
}
