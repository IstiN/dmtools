package com.github.istin.dmtools.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthInitiateRequest {
    private String provider;
    
    @JsonProperty("client_redirect_uri")
    private String clientRedirectUri;
    
    @JsonProperty("client_type")
    private String clientType;
    
    private String environment;

    public OAuthInitiateRequest() {}

    public OAuthInitiateRequest(String provider, String clientRedirectUri, String clientType, String environment) {
        this.provider = provider;
        this.clientRedirectUri = clientRedirectUri;
        this.clientType = clientType;
        this.environment = environment;
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
} 