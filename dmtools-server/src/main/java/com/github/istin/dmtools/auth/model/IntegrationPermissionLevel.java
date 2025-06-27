package com.github.istin.dmtools.auth.model;

/**
 * Enum representing different permission levels a user can have for an integration.
 */
public enum IntegrationPermissionLevel {
    /**
     * Read-only access to the integration
     */
    READ,
    
    /**
     * Can modify the integration
     */
    WRITE,
    
    /**
     * Full control over the integration, including deletion and sharing
     */
    ADMIN
} 