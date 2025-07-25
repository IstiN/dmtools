package com.github.istin.dmtools.auth.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Configuration for an integration type loaded from JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationTypeConfig {
    
    private String type;
    private String displayName;
    private String displayNameKey;
    private String description;
    private String descriptionKey;
    private String iconUrl;
    private List<String> categories;
    private List<ConfigParamConfig> configParams;
    private Map<String, String> setupDocumentation;
    private boolean hidden = false; // Default to visible
    private boolean supportsMcp = false; // Default to false

    public IntegrationTypeConfig() {
    }

    public IntegrationTypeConfig(String type, String displayName, String description, String iconUrl,
                               List<String> categories, List<ConfigParamConfig> configParams) {
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.iconUrl = iconUrl;
        this.categories = categories;
        this.configParams = configParams;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayNameKey() {
        return displayNameKey;
    }

    public void setDisplayNameKey(String displayNameKey) {
        this.displayNameKey = displayNameKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public void setDescriptionKey(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<ConfigParamConfig> getConfigParams() {
        return configParams;
    }

    public void setConfigParams(List<ConfigParamConfig> configParams) {
        this.configParams = configParams;
    }

    public Map<String, String> getSetupDocumentation() {
        return setupDocumentation;
    }

    public void setSetupDocumentation(Map<String, String> setupDocumentation) {
        this.setupDocumentation = setupDocumentation;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isSupportsMcp() {
        return supportsMcp;
    }

    public void setSupportsMcp(boolean supportsMcp) {
        this.supportsMcp = supportsMcp;
    }
} 