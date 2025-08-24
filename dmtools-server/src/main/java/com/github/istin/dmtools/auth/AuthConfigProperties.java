package com.github.istin.dmtools.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfigProperties {

    /**
     * Comma-separated list of enabled authentication providers (e.g., google,microsoft,github).
     * If absent or empty, enables local standalone mode.
     */
    private String enabledProviders;

    /**
     * Comma-separated list of allowed email domains (e.g., example.com,mycompany.org).
     * If absent or empty, authentication allowed for users from any email domain.
     */
    private String permittedEmailDomains;

    /**
     * Local admin username for standalone mode. Default: admin.
     */
    private String adminUsername = "admin";

    /**
     * Local admin password for standalone mode. Default: admin.
     */
    private String adminPassword = "admin";

    public List<String> getEnabledProvidersList() {
        if (enabledProviders == null || enabledProviders.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(enabledProviders.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public List<String> getPermittedEmailDomainsList() {
        if (permittedEmailDomains == null || permittedEmailDomains.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(permittedEmailDomains.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
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
        return getEnabledProvidersList().isEmpty();
    }
}
