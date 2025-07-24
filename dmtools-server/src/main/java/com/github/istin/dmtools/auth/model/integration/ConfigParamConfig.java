package com.github.istin.dmtools.auth.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Configuration for a single integration parameter.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigParamConfig {
    
    private String key;
    private String displayName;
    private String displayNameKey;
    private String descriptionKey;
    private String instructionsKey;
    private boolean required;
    private boolean sensitive;
    private String inputType;
    private String defaultValue;
    private ValidationConfig validation;
    private List<OptionConfig> options;

    public ConfigParamConfig() {
    }

    public ConfigParamConfig(String key, String displayName, String descriptionKey, String instructionsKey,
                           boolean required, boolean sensitive, String inputType, String defaultValue,
                           ValidationConfig validation) {
        this.key = key;
        this.displayName = displayName;
        this.descriptionKey = descriptionKey;
        this.instructionsKey = instructionsKey;
        this.required = required;
        this.sensitive = sensitive;
        this.inputType = inputType;
        this.defaultValue = defaultValue;
        this.validation = validation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public void setDescriptionKey(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    public String getInstructionsKey() {
        return instructionsKey;
    }

    public void setInstructionsKey(String instructionsKey) {
        this.instructionsKey = instructionsKey;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ValidationConfig getValidation() {
        return validation;
    }

    public void setValidation(ValidationConfig validation) {
        this.validation = validation;
    }

    public List<OptionConfig> getOptions() {
        return options;
    }

    public void setOptions(List<OptionConfig> options) {
        this.options = options;
    }
} 