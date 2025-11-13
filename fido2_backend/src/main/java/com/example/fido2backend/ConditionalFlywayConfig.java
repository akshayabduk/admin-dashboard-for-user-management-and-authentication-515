package com.example.fido2backend;

import org.springframework.context.annotation.Configuration;

/**
 * No-op configuration reserved for future conditional customizations.
 * We avoid defining FlywayProperties beans directly to prevent interfering
 * with Spring Boot's Flyway auto-configuration lifecycle which can cause
 * circular dependency between flyway and entityManagerFactory.
 */
@Configuration
public class ConditionalFlywayConfig {
    // Intentionally left blank. To disable Flyway in environments without DB,
    // set environment variable FLYWAY_ENABLED=false (see application.properties).
}
