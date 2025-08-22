package com.github.istin.dmtools.auth.dto;

import java.util.List;

public class AuthConfigurationDto {
    private List<String> enabledProviders;
    private boolean localStandaloneModeEnabled;

    public AuthConfigurationDto(List<String> enabledProviders, boolean localStandaloneModeEnabled) {
        this.enabledProviders = enabledProviders;
        this.localStandaloneModeEnabled = localStandaloneModeEnabled;
    }

    public List<String> getEnabledProviders() {
        return enabledProviders;
    }

    public void setEnabledProviders(List<String> enabledProviders) {
        this.enabledProviders = enabledProviders;
    }

    public boolean isLocalStandaloneModeEnabled() {
        return localStandaloneModeEnabled;
    }

    public void setLocalStandaloneModeEnabled(boolean localStandaloneModeEnabled) {
        this.localStandaloneModeEnabled = localStandaloneModeEnabled;
    }
}
