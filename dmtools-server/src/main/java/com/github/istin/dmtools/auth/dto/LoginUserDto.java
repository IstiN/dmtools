package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * User information DTO for login response
 */
@Schema(description = "User information in login response")
public class LoginUserDto {
    
    @Schema(description = "User ID", example = "user123")
    private String id;
    
    @Schema(description = "User email", example = "admin@local.test")
    private String email;
    
    @Schema(description = "User name", example = "admin")
    private String name;
    
    @Schema(description = "Authentication provider", example = "LOCAL", allowableValues = {"LOCAL", "google", "github", "microsoft"})
    private String provider;
    
    @Schema(description = "User role", example = "ADMIN", allowableValues = {"ADMIN", "REGULAR_USER"})
    private String role;
    
    @Schema(description = "Authentication status", example = "true")
    private boolean authenticated;
    
    public LoginUserDto() {}
    
    public LoginUserDto(String id, String email, String name, String provider, String role, boolean authenticated) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.role = role;
        this.authenticated = authenticated;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
