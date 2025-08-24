package com.github.istin.dmtools.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfigProperties {

    private List<String> enabledProviders;

    private List<String> permittedEmailDomains;

    private String adminUsername;

    private String adminPassword;

    public List<String> getEnabledProviders() {
        return Optional.ofNullable(enabledProviders)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public void setEnabledProviders(List<String> enabledProviders) {
        this.enabledProviders = enabledProviders;
    }

    public List<String> getPermittedEmailDomains() {
        return Optional.ofNullable(permittedEmailDomains)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
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

    public boolean isLocalStandaloneMode() {
        return getEnabledProviders().isEmpty();
    }
}
