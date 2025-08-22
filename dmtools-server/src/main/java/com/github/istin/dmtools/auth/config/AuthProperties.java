package com.github.istin.dmtools.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * Comma-separated list of enabled external authentication providers (e.g., google,microsoft,github).
     * If absent or empty, enables local standalone mode.
     */
    private List<String> enabledProviders = Collections.emptyList();

    /**
     * Comma-separated list of allowed email domains (e.g., example.com,mycompany.org).
     * If absent or empty, authentication allowed for users from any email domain.
     */
    private List<String> permittedEmailDomains = Collections.emptyList();

    /**
     * Local admin username for standalone mode. Default: admin.
     */
    private String adminUsername = "admin";

    /**
     * Local admin password for standalone mode. Default: admin.
     */
    private String adminPassword = "admin";

    public List<String> getEnabledProviders() {
        return enabledProviders;
    }

    public void setEnabledProviders(List<String> enabledProviders) {
        this.enabledProviders = Optional.ofNullable(enabledProviders).orElse(Collections.emptyList());
    }

    public List<String> getPermittedEmailDomains() {
        return permittedEmailDomains;
    }

    public void setPermittedEmailDomains(List<String> permittedEmailDomains) {
        this.permittedEmailDomains = Optional.ofNullable(permittedEmailDomains).orElse(Collections.emptyList());
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
        return enabledProviders.isEmpty();
    }

    public boolean isEmailDomainRestrictionEnabled() {
        return !permittedEmailDomains.isEmpty();
    }
}
