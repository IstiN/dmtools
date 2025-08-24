package com.github.istin.dmtools.auth.model;

import java.util.List;

public class AuthConfiguration {
    private boolean localStandaloneMode;
    private List<String> enabledProviders;

    public AuthConfiguration(boolean localStandaloneMode, List<String> enabledProviders) {
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
