package com.github.istin.dmtools.common.config;

/**
 * Composite configuration interface for all AI-related settings.
 * This interface combines all specific AI provider configurations.
 */
public interface AIConfiguration extends 
        OpenAIConfiguration, 
        GeminiConfiguration, 
        PromptConfiguration,
        AIRetryConfiguration,
        AIModelConfiguration {
} 