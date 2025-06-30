package com.github.istin.dmtools.auth.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OAuthStateData implements Serializable {
    private String provider;
    private String clientRedirectUri;
    private String clientType;
    private String environment;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String authorizationCode;

    public OAuthStateData() {}

    public OAuthStateData(String provider, String clientRedirectUri, String clientType, String environment) {
        this.provider = provider;
        this.clientRedirectUri = clientRedirectUri;
        this.clientType = clientType;
        this.environment = environment;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusMinutes(5);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getClientRedirectUri() {
        return clientRedirectUri;
    }

    public void setClientRedirectUri(String clientRedirectUri) {
        this.clientRedirectUri = clientRedirectUri;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
} 