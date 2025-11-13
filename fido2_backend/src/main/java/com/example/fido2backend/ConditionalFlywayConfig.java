package com.example.fido2backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditionally disables Flyway at runtime if no SPRING_DATASOURCE_URL is provided.
 * This allows the app to boot and bind to the port even if the DB is unavailable in preview/dev.
 */
@Configuration
@EnableConfigurationProperties(FlywayProperties.class)
public class ConditionalFlywayConfig {

    // PUBLIC_INTERFACE
    @Bean
    /** Returns FlywayProperties possibly toggled off when database is not configured. */
    public FlywayProperties conditionalFlywayProps(
            FlywayProperties props,
            @Value("${SPRING_DATASOURCE_URL:}") String dsUrlEnv) {
        // If no datasource URL is supplied via env, disable Flyway to avoid startup failure.
        if (dsUrlEnv == null || dsUrlEnv.isBlank()) {
            props.setEnabled(false);
        }
        return props;
    }
}
