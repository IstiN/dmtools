package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated user information")
public class AuthUserResponse {
    
    @Schema(description = "Whether user is authenticated", example = "true")
    private boolean authenticated;
    
    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;
    
    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User full name", example = "John Doe")
    private String name;
    
    @Schema(description = "User given name", example = "John")
    private String givenName;
    
    @Schema(description = "User family name", example = "Doe")
    private String familyName;
    
    @Schema(description = "User profile picture URL", example = "https://example.com/avatar.jpg")
    private String pictureUrl;
    
    @Schema(description = "Authentication provider", example = "google")
    private String provider;
    
    @Schema(description = "User role", example = "REGULAR_USER")
    private String role;

    // Default constructor
    public AuthUserResponse() {}

    // Constructor with authentication status only
    public AuthUserResponse(boolean authenticated) {
        this.authenticated = authenticated;
    }

    // Constructor with authentication status and message (for failures)
    public AuthUserResponse(boolean authenticated, String message) {
        this.authenticated = authenticated;
        // Could add message field if needed
    }

    // Getters and setters
    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

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

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
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
}
