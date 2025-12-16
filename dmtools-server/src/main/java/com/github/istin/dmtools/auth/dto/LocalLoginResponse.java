package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Local login response")
public class LocalLoginResponse {
    
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "User information")
    private UserInfo user;

    public LocalLoginResponse() {}

    public LocalLoginResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public LocalLoginResponse(String token, String refreshToken, UserInfo user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    @Schema(description = "User information in login response")
    public static class UserInfo {
        @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private String id;
        
        @Schema(description = "User email", example = "admin@local.test")
        private String email;
        
        @Schema(description = "User name", example = "admin")
        private String name;
        
        @Schema(description = "Authentication provider", example = "LOCAL")
        private String provider;
        
        @Schema(description = "User role", example = "ADMIN")
        private String role;
        
        @Schema(description = "Authentication status", example = "true")
        private boolean authenticated;

        public UserInfo() {}

        public UserInfo(String id, String email, String name, String provider, String role, boolean authenticated) {
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
}
