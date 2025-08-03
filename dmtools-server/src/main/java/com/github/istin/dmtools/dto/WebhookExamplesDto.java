package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for webhook integration examples.
 * Contains rendered examples with templates and available variables.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookExamplesDto {
    
    private String jobConfigurationId;
    private String jobType;
    private String webhookUrl;
    private List<WebhookExampleTemplate> examples;
    private List<String> availableVariables;
    
    /**
     * Individual webhook example template.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookExampleTemplate {
        private String name;
        private String renderedTemplate;
    }
    
    /**
     * Create webhook examples DTO.
     * 
     * @param jobConfigurationId The job configuration ID
     * @param jobType The job type
     * @param webhookUrl The webhook URL
     * @param examples The list of rendered examples
     * @param availableVariables The list of available template variables
     * @return Webhook examples DTO
     */
    public static WebhookExamplesDto create(String jobConfigurationId, String jobType, 
                                          String webhookUrl, List<WebhookExampleTemplate> examples,
                                          List<String> availableVariables) {
        return new WebhookExamplesDto(
            jobConfigurationId,
            jobType,
            webhookUrl,
            examples,
            availableVariables
        );
    }
}