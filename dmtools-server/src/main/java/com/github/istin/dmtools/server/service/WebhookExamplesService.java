package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.dto.WebhookExamplesDto;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobTypeConfig;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for generating webhook integration examples.
 * Processes job configuration JSON and renders templates with variable substitution.
 */
@Service
@Slf4j
public class WebhookExamplesService {
    
    @Autowired
    private JobConfigurationLoader jobConfigurationLoader;
    
    @Autowired
    private JobConfigurationRepository jobConfigurationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${dmtools.server.base-url:http://localhost:8080}")
    private String baseUrl;
    
    // Template variable pattern for substitution
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    /**
     * Get webhook examples for a job configuration.
     * 
     * @param jobConfigId The job configuration ID
     * @param userId The user ID requesting the examples
     * @return Optional containing webhook examples DTO
     */
    public Optional<WebhookExamplesDto> getWebhookExamples(String jobConfigId, String userId) {
        try {
            // Find user
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                log.warn("User not found: {}", userId);
                return Optional.empty();
            }
            User user = optionalUser.get();
            
            // Find job configuration and verify ownership
            Optional<JobConfiguration> optionalJobConfig = jobConfigurationRepository
                    .findByIdAndCreatedBy(jobConfigId, user);
            if (optionalJobConfig.isEmpty()) {
                log.warn("Job configuration not found or not accessible: {} for user: {}", jobConfigId, userId);
                return Optional.empty();
            }
            
            JobConfiguration jobConfig = optionalJobConfig.get();
            String jobType = jobConfig.getJobType();
            
            // Get job type configuration
            JobTypeConfig jobTypeConfig = jobConfigurationLoader.getConfigurationByType(jobType);
            if (jobTypeConfig == null) {
                log.warn("Job type configuration not found for type: {}", jobType);
                return Optional.empty();
            }
            
            // Get webhook examples from job type configuration
            List<JobTypeConfig.WebhookExampleConfig> webhookExamples = jobTypeConfig.getWebhookExamples();
            if (webhookExamples == null || webhookExamples.isEmpty()) {
                log.info("No webhook examples configured for job type: {}", jobType);
                return Optional.of(WebhookExamplesDto.create(
                    jobConfigId,
                    jobType,
                    buildWebhookUrl(jobConfigId),
                    Collections.emptyList(),
                    getAvailableVariables()
                ));
            }
            
            // Render webhook examples with variable substitution
            List<WebhookExamplesDto.WebhookExampleTemplate> renderedExamples = webhookExamples.stream()
                    .map(example -> renderWebhookExample(example, jobConfigId, jobConfig))
                    .collect(Collectors.toList());
            
            return Optional.of(WebhookExamplesDto.create(
                jobConfigId,
                jobType,
                buildWebhookUrl(jobConfigId),
                renderedExamples,
                getAvailableVariables()
            ));
            
        } catch (Exception e) {
            log.error("Error generating webhook examples for job configuration: {} by user: {}", jobConfigId, userId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Render a webhook example template with variable substitution.
     * 
     * @param example The webhook example configuration
     * @param jobConfigId The job configuration ID
     * @param jobConfig The job configuration entity
     * @return Rendered webhook example template
     */
    private WebhookExamplesDto.WebhookExampleTemplate renderWebhookExample(
            JobTypeConfig.WebhookExampleConfig example, String jobConfigId, JobConfiguration jobConfig) {
        
        String template = example.getTemplate();
        if (template == null) {
            template = "";
        }
        
        // Create variable substitution map
        Map<String, String> variables = new HashMap<>();
        variables.put("webhook_url", buildWebhookUrl(jobConfigId));
        variables.put("api_key", "{{YOUR_API_KEY}}"); // Placeholder for user's actual API key
        variables.put("job_config_id", jobConfigId);
        variables.put("job_type", jobConfig.getJobType());
        variables.put("job_name", jobConfig.getName());
        
        // Add example values for common parameters
        variables.put("issue.key", "DMC-123");
        variables.put("userInputs.field", "example_value");
        variables.put("project.key", "DMC");
        variables.put("issue.summary", "Example Issue Summary");
        
        // Perform variable substitution
        String renderedTemplate = performVariableSubstitution(template, variables);
        
        return new WebhookExamplesDto.WebhookExampleTemplate(example.getName(), renderedTemplate);
    }
    
    /**
     * Perform variable substitution in template.
     * 
     * @param template The template string
     * @param variables The variable substitution map
     * @return Template with variables substituted
     */
    private String performVariableSubstitution(String template, Map<String, String> variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String replacement = variables.getOrDefault(variableName, "{{" + variableName + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Build the webhook URL for a job configuration.
     * 
     * @param jobConfigId The job configuration ID
     * @return Webhook URL
     */
    private String buildWebhookUrl(String jobConfigId) {
        return baseUrl + "/api/v1/job-configurations/" + jobConfigId + "/webhook";
    }
    
    /**
     * Get list of available template variables.
     * 
     * @return List of available variable names
     */
    private List<String> getAvailableVariables() {
        return Arrays.asList(
            "{{webhook_url}}",
            "{{api_key}}",
            "{{job_config_id}}",
            "{{job_type}}",
            "{{job_name}}",
            "{{issue.key}}",
            "{{issue.summary}}",
            "{{project.key}}",
            "{{userInputs.field}}"
        );
    }
}