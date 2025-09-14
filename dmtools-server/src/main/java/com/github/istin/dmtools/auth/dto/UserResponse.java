package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for user information
 */
@Schema(description = "User information response")
public class UserResponse {
    
    @Schema(description = "Whether the user is authenticated", example = "true")
    private boolean authenticated;
    
    @Schema(description = "User ID", example = "user123")
    private String id;
    
    @Schema(description = "User email", example = "user@example.com")
    private String email;
    
    @Schema(description = "User display name", example = "John Doe")
    private String name;
    
    @Schema(description = "User given name", example = "John")
    private String givenName;
    
    @Schema(description = "User family name", example = "Doe")
    private String familyName;
    
    @Schema(description = "User profile picture URL", example = "https://example.com/avatar.jpg")
    private String pictureUrl;
    
    @Schema(description = "Authentication provider", example = "google", allowableValues = {"google", "github", "microsoft", "local"})
    private String provider;
    
    @Schema(description = "User role", example = "ADMIN", allowableValues = {"ADMIN", "REGULAR_USER"})
    private String role;
    
    @Schema(description = "Message for non-authenticated users", example = "Authentication in progress")
    private String message;
    
    public UserResponse() {}
    
    public UserResponse(boolean authenticated) {
        this.authenticated = authenticated;
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
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
