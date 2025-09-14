package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Local login request")
public class LocalLoginRequest {
    
    @Schema(description = "Username for local authentication", example = "admin", required = true)
    private String username;
    
    @Schema(description = "Password for local authentication", example = "password", required = true)
    private String password;

    public LocalLoginRequest() {}

    public LocalLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}