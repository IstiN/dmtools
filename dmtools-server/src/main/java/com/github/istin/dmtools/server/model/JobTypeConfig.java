package com.github.istin.dmtools.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Configuration for a job type loaded from JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobTypeConfig {
    
    private String type;
    private String displayName;
    private String displayNameKey;
    private String description;
    private String descriptionKey;
    private String iconUrl;
    private List<String> categories;
    private List<String> executionModes;
    private List<String> requiredIntegrations;
    private List<String> optionalIntegrations;
    private List<ConfigParamConfig> configParams;
    private Map<String, String> setupDocumentation;
    private boolean hidden = false; // Default to visible
    private List<WebhookExampleConfig> webhookExamples;

    public JobTypeConfig() {
    }

    public JobTypeConfig(String type, String displayName, String description, String iconUrl,
                        List<String> categories, List<String> executionModes, 
                        List<String> requiredIntegrations, List<String> optionalIntegrations,
                        List<ConfigParamConfig> configParams) {
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.iconUrl = iconUrl;
        this.categories = categories;
        this.executionModes = executionModes;
        this.requiredIntegrations = requiredIntegrations;
        this.optionalIntegrations = optionalIntegrations;
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

    public List<String> getExecutionModes() {
        return executionModes;
    }

    public void setExecutionModes(List<String> executionModes) {
        this.executionModes = executionModes;
    }

    public List<String> getRequiredIntegrations() {
        return requiredIntegrations;
    }

    public void setRequiredIntegrations(List<String> requiredIntegrations) {
        this.requiredIntegrations = requiredIntegrations;
    }

    public List<String> getOptionalIntegrations() {
        return optionalIntegrations;
    }

    public void setOptionalIntegrations(List<String> optionalIntegrations) {
        this.optionalIntegrations = optionalIntegrations;
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

    public List<WebhookExampleConfig> getWebhookExamples() {
        return webhookExamples;
    }

    public void setWebhookExamples(List<WebhookExampleConfig> webhookExamples) {
        this.webhookExamples = webhookExamples;
    }

    /**
     * Configuration for a job parameter loaded from JSON.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConfigParamConfig {
        private String key;
        private String displayName;
        private String displayNameKey;
        private String description;
        private String descriptionKey;
        private String instructions;
        private String instructionsKey;
        private boolean required;
        private boolean sensitive;
        private String inputType;
        private String defaultValue;
        private ValidationConfig validation;
        private List<OptionConfig> options;
        private List<String> examples;

        public ConfigParamConfig() {
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

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
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

        public List<String> getExamples() {
            return examples;
        }

        public void setExamples(List<String> examples) {
            this.examples = examples;
        }
    }

    /**
     * Validation configuration for job parameters.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidationConfig {
        private String pattern;
        private Integer minLength;
        private Integer maxLength;
        private Integer min;
        private Integer max;
        private List<String> enumValues;
        private String type;

        public ValidationConfig() {
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

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
        }

        public List<String> getEnumValues() {
            return enumValues;
        }

        public void setEnumValues(List<String> enumValues) {
            this.enumValues = enumValues;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * Option configuration for select parameters.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OptionConfig {
        private String value;
        private String label;
        private String labelKey;

        public OptionConfig() {
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

    /**
     * Configuration for webhook examples.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookExampleConfig {
        private String name;
        private String template;

        public WebhookExampleConfig() {
        }

        public WebhookExampleConfig(String name, String template) {
            this.name = name;
            this.template = template;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }
    }
} 