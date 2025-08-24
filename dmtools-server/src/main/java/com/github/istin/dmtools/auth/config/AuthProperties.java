package com.github.istin.dmtools.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private List<String> enabledProviders = Collections.emptyList();
    private List<String> permittedEmailDomains = Collections.emptyList();
    private String adminUsername = "admin";
    private String adminPassword = "admin";

    public List<String> getEnabledProviders() {
        return enabledProviders;
    }

    public void setEnabledProviders(String enabledProviders) {
        if (enabledProviders != null && !enabledProviders.trim().isEmpty()) {
            this.enabledProviders = Arrays.stream(enabledProviders.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            this.enabledProviders = Collections.emptyList();
        }
    }

    public List<String> getPermittedEmailDomains() {
        return permittedEmailDomains;
    }

    public void setPermittedEmailDomains(String permittedEmailDomains) {
        if (permittedEmailDomains != null && !permittedEmailDomains.trim().isEmpty()) {
            this.permittedEmailDomains = Arrays.stream(permittedEmailDomains.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            this.permittedEmailDomains = Collections.emptyList();
        }
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public boolean isLocalStandaloneMode() {
        return enabledProviders.isEmpty();
    }
}
