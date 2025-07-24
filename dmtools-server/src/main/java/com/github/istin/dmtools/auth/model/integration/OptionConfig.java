package com.github.istin.dmtools.auth.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Configuration for a select option in integration parameters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionConfig {
    
    private String value;
    private String label;
    private String labelKey;

    public OptionConfig() {
    }

    public OptionConfig(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }
} 