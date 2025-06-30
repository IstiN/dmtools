package com.github.istin.dmtools.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class PlaceholderAuthentication implements Authentication {
    
    private final String authorizationCode;
    private final String provider;
    private boolean authenticated = false;
    
    public PlaceholderAuthentication(String authorizationCode, String provider) {
        this.authorizationCode = authorizationCode;
        this.provider = provider;
    }
    
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    
    public String getProvider() {
        return provider;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return authorizationCode;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return "placeholder_" + provider;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return "placeholder_" + provider;
    }
} 