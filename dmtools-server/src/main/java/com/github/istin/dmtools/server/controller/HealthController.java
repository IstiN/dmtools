package com.github.istin.dmtools.server.controller;

import com.github.istin.dmtools.server.config.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for database connectivity and environment information.
 * Provides endpoints for monitoring database health and configuration verification.
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthIndicator databaseHealthIndicator;
    private final DataSource dataSource;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Autowired(required = false)
    private DatabaseConfig.DatabaseConnectionVerifier connectionVerifier;

    /**
     * Basic health check endpoint.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("profile", activeProfile);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Simple health check endpoint for compatibility.
     */
    @GetMapping("/simple")
    public String simpleHealth() {
        return "OK";
    }

    /**
     * Root health check endpoint for compatibility.
     */
    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String rootHealth() {
        return "OK";
    }

    /**
     * App Engine health check endpoint.
     */
    @RequestMapping(value = "/_ah/health", method = RequestMethod.GET)
    public String appEngineHealth() {
        return "OK";
    }

    /**
     * Database health check endpoint with detailed information.
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Health health = databaseHealthIndicator.health();
            response.put("status", health.getStatus().getCode());
            response.put("details", health.getDetails());
            response.put("profile", activeProfile);
            
            // Add connection pool information if available
            if (connectionVerifier != null) {
                response.put("connectionInfo", connectionVerifier.getConnectionInfo());
                response.put("connectionValid", connectionVerifier.verifyConnection());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Database health check failed", e);
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("profile", activeProfile);
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Environment configuration information endpoint.
     */
    @GetMapping("/environment")
    public ResponseEntity<Map<String, Object>> environment() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("profile", activeProfile);
        response.put("datasourceUrl", maskSensitiveUrl(datasourceUrl));
        response.put("timestamp", System.currentTimeMillis());
        
        // Add database type detection
        try (Connection connection = dataSource.getConnection()) {
            response.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
            response.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
            response.put("driverName", connection.getMetaData().getDriverName());
            response.put("driverVersion", connection.getMetaData().getDriverVersion());
        } catch (SQLException e) {
            response.put("databaseError", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cloud SQL specific health check (only available in GCP profile).
     */
    @GetMapping("/cloudsql")
    public ResponseEntity<Map<String, Object>> cloudSqlHealth() {
        Map<String, Object> response = new HashMap<>();
        
        if (!"gcp".equals(activeProfile)) {
            response.put("status", "NOT_APPLICABLE");
            response.put("message", "Cloud SQL health check only available in GCP profile");
            response.put("currentProfile", activeProfile);
            return ResponseEntity.ok(response);
        }
        
        if (connectionVerifier == null) {
            response.put("status", "UNAVAILABLE");
            response.put("message", "Cloud SQL connection verifier not available");
            return ResponseEntity.status(503).body(response);
        }
        
        try {
            boolean isValid = connectionVerifier.verifyConnection();
            response.put("status", isValid ? "UP" : "DOWN");
            response.put("connectionInfo", connectionVerifier.getConnectionInfo());
            response.put("profile", activeProfile);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Cloud SQL health check failed", e);
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Masks sensitive information in database URL.
     */
    private String maskSensitiveUrl(String url) {
        if (url == null) return "unknown";
        
        return url.replaceAll("password=[^&]*", "password=***")
                  .replaceAll("user=[^&]*", "user=***");
    }
} 