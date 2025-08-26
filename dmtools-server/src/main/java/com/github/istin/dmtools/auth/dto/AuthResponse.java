package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for authentication operations
 */
@Schema(description = "Authentication response")
public class AuthResponse {
    
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "User information")
    private LoginUserDto user;
    
    @Schema(description = "Success message", example = "Login successful")
    private String message;
    
    @Schema(description = "Error message if authentication failed", example = "Invalid credentials")
    private String error;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, LoginUserDto user) {
        this.token = token;
        this.user = user;
    }
    
    public AuthResponse(String error) {
        this.error = error;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LoginUserDto getUser() {
        return user;
    }
    
    public void setUser(LoginUserDto user) {
        this.user = user;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}
