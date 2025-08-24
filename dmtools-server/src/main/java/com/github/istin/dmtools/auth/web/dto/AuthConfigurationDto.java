package com.github.istin.dmtools.auth.web.dto;

import java.util.List;

public class AuthConfigurationDto {

    private boolean localStandaloneMode;
    private List<String> enabledProviders;

    public AuthConfigurationDto(boolean localStandaloneMode, List<String> enabledProviders) {
        this.localStandaloneMode = localStandaloneMode;
        this.enabledProviders = enabledProviders;
    }

    public boolean isLocalStandaloneMode() {
        return localStandaloneMode;
    }

    public void setLocalStandaloneMode(boolean localStandaloneMode) {
        this.localStandaloneMode = localStandaloneMode;
    }

    public List<String> getEnabledProviders() {
        return enabledProviders;
    }

    public void setEnabledProviders(List<String> enabledProviders) {
        this.enabledProviders = enabledProviders;
    }
}
