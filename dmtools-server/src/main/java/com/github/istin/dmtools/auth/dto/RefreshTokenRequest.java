package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refresh token request")
public class RefreshTokenRequest {
    
    @Schema(description = "Refresh token to exchange for new access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

