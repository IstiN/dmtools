package com.github.istin.dmtools.auth.model;

/**
 * Enum representing different roles a user can have within a workspace.
 */
public enum WorkspaceRole {
    /**
     * Admin role - can manage workspace users and settings
     */
    ADMIN,
    
    /**
     * User role - can view and use workspace but cannot manage users
     */
    USER
} 