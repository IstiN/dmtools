package com.github.istin.dmtools.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfigProperties {

    /**
     * Comma-separated list of provider IDs (e.g., google,microsoft,github).
     * If absent or empty, enables local standalone mode.
     */
    private List<String> enabledProviders;

    /**
     * Comma-separated list of allowed email domains (e.g., example.com,mycompany.org).
     * If absent or empty, authentication allowed for users from any email domain.
     */
    private List<String> permittedEmailDomains;

    /**
     * Local admin username (default: admin) for standalone mode.
     */
    private String adminUsername = "admin";

    /**
     * Local admin password (default: admin) for standalone mode.
     */
    private String adminPassword = "admin";

    public List<String> getEnabledProviders() {
        return enabledProviders;
    }

    public void setEnabledProviders(List<String> enabledProviders) {
        this.enabledProviders = enabledProviders;
    }

    public List<String> getPermittedEmailDomains() {
        return permittedEmailDomains;
    }

    public void setPermittedEmailDomains(List<String> permittedEmailDomains) {
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

    public boolean isLocalStandaloneModeEnabled() {
        return enabledProviders == null || enabledProviders.isEmpty();
    }

    public boolean isEmailDomainRestricted() {
        return permittedEmailDomains != null && !permittedEmailDomains.isEmpty();
    }
}
