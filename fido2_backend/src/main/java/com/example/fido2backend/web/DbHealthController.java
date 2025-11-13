package com.example.fido2backend.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/**
 * Minimal database connectivity check.
 * Attempts to get a connection from the configured DataSource.
 * Returns ok: true if successful, with driver info when available.
 */
@RestController
@Tag(name = "Health", description = "Database connectivity health check")
public class DbHealthController {

    private final DataSource dataSource;

    public DbHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // PUBLIC_INTERFACE
    @GetMapping("/health/db")
    @Operation(summary = "DB health", description = "Checks if the application can obtain a connection to the database using the configured DataSource.")
    public Map<String, Object> dbHealth() {
        try (Connection c = dataSource.getConnection()) {
            String driver = c.getMetaData().getDriverName() + " " + c.getMetaData().getDriverVersion();
            String url = c.getMetaData().getURL();
            return Map.of(
                    "ok", true,
                    "driver", driver,
                    "url", url
            );
        } catch (Exception e) {
            return Map.of(
                    "ok", false,
                    "error", e.getClass().getSimpleName() + ": " + e.getMessage()
            );
        }
    }
}
