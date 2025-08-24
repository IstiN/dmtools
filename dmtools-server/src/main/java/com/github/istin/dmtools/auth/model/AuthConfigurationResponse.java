package com.github.istin.dmtools.auth.model;

import java.util.List;
import java.util.Objects;

public class AuthConfigurationResponse {
    private boolean localStandaloneMode;
    private List<String> enabledProviders;

    public AuthConfigurationResponse(boolean localStandaloneMode, List<String> enabledProviders) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthConfigurationResponse that = (AuthConfigurationResponse) o;
        return localStandaloneMode == that.localStandaloneMode && Objects.equals(enabledProviders, that.enabledProviders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localStandaloneMode, enabledProviders);
    }

    @Override
    public String toString() {
        return "AuthConfigurationResponse{" +
               "localStandaloneMode=" + localStandaloneMode +
               ", enabledProviders=" + enabledProviders +
               '}'
        ;
    }
}
