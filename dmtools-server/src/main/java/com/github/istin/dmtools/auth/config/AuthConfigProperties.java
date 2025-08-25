package com.github.istin.dmtools.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfigProperties {

    /**
     * Comma-separated list of enabled OAuth2 providers (e.g., "google,microsoft,github").
     * If empty or absent, local standalone mode is enabled.
     */
    private String enabledProviders;

    /**
     * Comma-separated list of permitted email domains (e.g., "example.com,mycompany.org").
     * If empty or absent, authentication is allowed for users from any email domain.
     */
    private String permittedEmailDomains;

    /**
     * Local admin username for standalone mode. Default: "admin".
     */
    private String adminUsername = "admin";

    /**
     * Local admin password for standalone mode. Default: "admin".
     */
    private String adminPassword = "admin";

    public Set<String> getEnabledProvidersAsSet() {
        if (enabledProviders == null || enabledProviders.trim().isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(enabledProviders.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public Set<String> getPermittedEmailDomainsAsSet() {
        if (permittedEmailDomains == null || permittedEmailDomains.trim().isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(permittedEmailDomains.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public String getEnabledProviders() {
        return enabledProviders;
    }

    public void setEnabledProviders(String enabledProviders) {
        this.enabledProviders = enabledProviders;
    }

    public String getPermittedEmailDomains() {
        return permittedEmailDomains;
    }

    public void setPermittedEmailDomains(String permittedEmailDomains) {
        this.permittedEmailDomains = permittedEmailDomains;
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
        return getEnabledProvidersAsSet().isEmpty();
    }
}
