package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for authentication configuration
 */
@Schema(description = "Authentication configuration")
public class AuthConfigResponse {
    
    @Schema(description = "Authentication mode", example = "oauth2", allowableValues = {"standalone", "oauth2"})
    private String authenticationMode;
    
    @Schema(description = "List of enabled OAuth2 providers", example = "[\"google\", \"github\"]")
    private List<String> enabledProviders;
    
    public AuthConfigResponse() {}
    
    public AuthConfigResponse(String authenticationMode, List<String> enabledProviders) {
        this.authenticationMode = authenticationMode;
        this.enabledProviders = enabledProviders;
    }
    
    public String getAuthenticationMode() {
        return authenticationMode;
    }
    
    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }
    
    public List<String> getEnabledProviders() {
        return enabledProviders;
    }
    
    public void setEnabledProviders(List<String> enabledProviders) {
        this.enabledProviders = enabledProviders;
    }
}
