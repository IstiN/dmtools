package com.github.istin.dmtools.auth.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Configuration for validation rules of integration parameters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationConfig {
    
    private String pattern;
    private Integer minLength;
    private Integer maxLength;
    private List<String> allowedValues;

    public ValidationConfig() {
    }

    public ValidationConfig(String pattern, Integer minLength, Integer maxLength, List<String> allowedValues) {
        this.pattern = pattern;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.allowedValues = allowedValues;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }
} 