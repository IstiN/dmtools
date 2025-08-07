package com.github.istin.dmtools.server.config;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.di.AIComponent;
import com.github.istin.dmtools.di.DaggerAIComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    private final AIComponent aiComponent = DaggerAIComponent.create();

    @Bean
    public AI ai() {
        return aiComponent.ai();
    }

    @Bean
    public IPromptTemplateReader promptTemplateReader() {
        return aiComponent.promptTemplateReader();
    }
} 