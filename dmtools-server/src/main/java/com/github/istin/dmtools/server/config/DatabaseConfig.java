package com.github.istin.dmtools.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database configuration for Google Cloud SQL and other environments.
 * Provides health checks and connection management for multi-cloud deployment.
 */
@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    /**
     * Database health indicator that verifies database connectivity
     * and provides detailed health information for monitoring.
     */
    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    try (Statement statement = connection.createStatement();
                         ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                        
                        if (resultSet.next()) {
                            return Health.up()
                                    .withDetail("database", "PostgreSQL")
                                    .withDetail("profile", activeProfile)
                                    .withDetail("connection", "healthy")
                                    .withDetail("url", maskSensitiveUrl(datasourceUrl))
                                    .build();
                        }
                    }
                }
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("profile", activeProfile)
                        .withDetail("connection", "unhealthy")
                        .withDetail("reason", "Query failed")
                        .build();
            } catch (SQLException e) {
                log.error("Database health check failed", e);
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("profile", activeProfile)
                        .withDetail("connection", "failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Google Cloud SQL specific configuration for production environment.
     * Provides additional monitoring and connection verification.
     */
    @Bean
    @Profile("gcp")
    public DatabaseConnectionVerifier cloudSqlConnectionVerifier(DataSource dataSource) {
        return new DatabaseConnectionVerifier(dataSource);
    }

    /**
     * Masks sensitive information in database URL for logging and health checks.
     */
    private String maskSensitiveUrl(String url) {
        if (url == null) return "unknown";
        
        // Mask password if present in URL
        return url.replaceAll("password=[^&]*", "password=***")
                  .replaceAll("user=[^&]*", "user=***");
    }

    /**
     * Database connection verifier for Cloud SQL environments.
     * Provides additional verification and monitoring capabilities.
     */
    public static class DatabaseConnectionVerifier {
        private final DataSource dataSource;

        public DatabaseConnectionVerifier(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public boolean verifyConnection() {
            try (Connection connection = dataSource.getConnection()) {
                return connection.isValid(10);
            } catch (SQLException e) {
                log.error("Database connection verification failed", e);
                return false;
            }
        }

        public String getConnectionInfo() {
            try (Connection connection = dataSource.getConnection()) {
                return String.format("Database: %s, URL: %s, Auto-commit: %s",
                        connection.getMetaData().getDatabaseProductName(),
                        connection.getMetaData().getURL(),
                        connection.getAutoCommit());
            } catch (SQLException e) {
                return "Connection info unavailable: " + e.getMessage();
            }
        }
    }
} 