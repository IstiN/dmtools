package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.dto.WebhookExamplesDto;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobTypeConfig;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebhookExamplesServiceTest {

    @Mock
    private JobConfigurationLoader jobConfigurationLoader;

    @Mock
    private JobConfigurationRepository jobConfigurationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WebhookExamplesService webhookExamplesService;

    private User testUser;
    private JobConfiguration testJobConfig;
    private JobTypeConfig testJobTypeConfig;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // Setup test job configuration
        testJobConfig = new JobConfiguration();
        testJobConfig.setId("job-config-123");
        testJobConfig.setName("Test Expert Job");
        testJobConfig.setJobType("Expert");
        testJobConfig.setCreatedBy(testUser);
        testJobConfig.setCreatedAt(LocalDateTime.now());

        // Setup test job type configuration with webhook examples
        testJobTypeConfig = new JobTypeConfig();
        testJobTypeConfig.setType("Expert");
        testJobTypeConfig.setDisplayName("AI Expert Analysis");

        JobTypeConfig.WebhookExampleConfig example1 = new JobTypeConfig.WebhookExampleConfig();
        example1.setName("jira automation");
        example1.setTemplate("## Jira Automation\n```json\n{\n  \"url\": \"{{webhook_url}}\",\n  \"headers\": {\n    \"X-API-Key\": \"{{api_key}}\"\n  }\n}\n```");

        JobTypeConfig.WebhookExampleConfig example2 = new JobTypeConfig.WebhookExampleConfig();
        example2.setName("curl command");
        example2.setTemplate("```bash\ncurl -X POST \"{{webhook_url}}\" \\\n  -H \"X-API-Key: {{api_key}}\" \\\n  -d '{\"jobParameters\": {\"inputJql\": \"key = {{issue.key}}\"}}'\\```");

        testJobTypeConfig.setWebhookExamples(Arrays.asList(example1, example2));

        // Set base URL using reflection
        ReflectionTestUtils.setField(webhookExamplesService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void testGetWebhookExamples_Success() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(jobConfigurationLoader.getConfigurationByType("Expert"))
                .thenReturn(testJobTypeConfig);

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertTrue(result.isPresent());
        WebhookExamplesDto dto = result.get();
        
        assertEquals("job-config-123", dto.getJobConfigurationId());
        assertEquals("Expert", dto.getJobType());
        assertEquals("http://localhost:8080/api/v1/job-configurations/job-config-123/webhook", dto.getWebhookUrl());
        
        assertNotNull(dto.getExamples());
        assertEquals(2, dto.getExamples().size());
        
        WebhookExamplesDto.WebhookExampleTemplate template1 = dto.getExamples().get(0);
        assertEquals("jira automation", template1.getName());
        assertTrue(template1.getRenderedTemplate().contains("http://localhost:8080/api/v1/job-configurations/job-config-123/webhook"));
        assertTrue(template1.getRenderedTemplate().contains("{{YOUR_API_KEY}}"));
        
        WebhookExamplesDto.WebhookExampleTemplate template2 = dto.getExamples().get(1);
        assertEquals("curl command", template2.getName());
        assertTrue(template2.getRenderedTemplate().contains("{{YOUR_API_KEY}}"));
        
        assertNotNull(dto.getAvailableVariables());
        assertTrue(dto.getAvailableVariables().contains("{{webhook_url}}"));
        assertTrue(dto.getAvailableVariables().contains("{{api_key}}"));
        assertTrue(dto.getAvailableVariables().contains("{{issue.key}}"));
    }

    @Test
    void testGetWebhookExamples_NoWebhookExamples() {
        // Arrange
        testJobTypeConfig.setWebhookExamples(null);
        
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(jobConfigurationLoader.getConfigurationByType("Expert"))
                .thenReturn(testJobTypeConfig);

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertTrue(result.isPresent());
        WebhookExamplesDto dto = result.get();
        
        assertEquals("job-config-123", dto.getJobConfigurationId());
        assertEquals("Expert", dto.getJobType());
        assertNotNull(dto.getExamples());
        assertTrue(dto.getExamples().isEmpty());
        assertNotNull(dto.getAvailableVariables());
        assertFalse(dto.getAvailableVariables().isEmpty());
    }

    @Test
    void testGetWebhookExamples_EmptyWebhookExamples() {
        // Arrange
        testJobTypeConfig.setWebhookExamples(Collections.emptyList());
        
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(jobConfigurationLoader.getConfigurationByType("Expert"))
                .thenReturn(testJobTypeConfig);

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertTrue(result.isPresent());
        WebhookExamplesDto dto = result.get();
        assertTrue(dto.getExamples().isEmpty());
    }

    @Test
    void testGetWebhookExamples_UserNotFound() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.empty());

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertFalse(result.isPresent());
        verify(jobConfigurationRepository, never()).findByIdAndCreatedBy(anyString(), any());
    }

    @Test
    void testGetWebhookExamples_JobConfigNotFound() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.empty());

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertFalse(result.isPresent());
        verify(jobConfigurationLoader, never()).getConfigurationByType(anyString());
    }

    @Test
    void testGetWebhookExamples_JobTypeConfigNotFound() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(jobConfigurationLoader.getConfigurationByType("Expert"))
                .thenReturn(null);

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testVariableSubstitution() {
        // Arrange
        JobTypeConfig.WebhookExampleConfig example = new JobTypeConfig.WebhookExampleConfig();
        example.setName("variable test");
        example.setTemplate("URL: {{webhook_url}}, Key: {{api_key}}, Job: {{job_name}}, Issue: {{issue.key}}");
        
        testJobTypeConfig.setWebhookExamples(Arrays.asList(example));
        
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(jobConfigurationLoader.getConfigurationByType("Expert"))
                .thenReturn(testJobTypeConfig);

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertTrue(result.isPresent());
        WebhookExamplesDto dto = result.get();
        
        assertEquals(1, dto.getExamples().size());
        String renderedTemplate = dto.getExamples().get(0).getRenderedTemplate();
        
        assertTrue(renderedTemplate.contains("http://localhost:8080/api/v1/job-configurations/job-config-123/webhook"));
        assertTrue(renderedTemplate.contains("{{YOUR_API_KEY}}"));
        assertTrue(renderedTemplate.contains("Test Expert Job"));
        assertTrue(renderedTemplate.contains("DMC-123"));
    }

    @Test
    void testVariableSubstitution_NullTemplate() {
        // Arrange
        JobTypeConfig.WebhookExampleConfig example = new JobTypeConfig.WebhookExampleConfig();
        example.setName("null template test");
        example.setTemplate(null);
        
        testJobTypeConfig.setWebhookExamples(Arrays.asList(example));
        
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(jobConfigurationLoader.getConfigurationByType("Expert"))
                .thenReturn(testJobTypeConfig);

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertTrue(result.isPresent());
        WebhookExamplesDto dto = result.get();
        
        assertEquals(1, dto.getExamples().size());
        String renderedTemplate = dto.getExamples().get(0).getRenderedTemplate();
        assertEquals("", renderedTemplate);
    }

    @Test
    void testVariableSubstitution_UnknownVariables() {
        // Arrange
        JobTypeConfig.WebhookExampleConfig example = new JobTypeConfig.WebhookExampleConfig();
        example.setName("unknown variables test");
        example.setTemplate("Known: {{webhook_url}}, Unknown: {{unknown_variable}}, Mixed: {{another.unknown}}");
        
        testJobTypeConfig.setWebhookExamples(Arrays.asList(example));
        
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(jobConfigurationLoader.getConfigurationByType("Expert"))
                .thenReturn(testJobTypeConfig);

        // Act
        Optional<WebhookExamplesDto> result = webhookExamplesService.getWebhookExamples("job-config-123", "user-123");

        // Assert
        assertTrue(result.isPresent());
        WebhookExamplesDto dto = result.get();
        
        assertEquals(1, dto.getExamples().size());
        String renderedTemplate = dto.getExamples().get(0).getRenderedTemplate();
        
        // Known variables should be substituted
        assertTrue(renderedTemplate.contains("http://localhost:8080/api/v1/job-configurations/job-config-123/webhook"));
        
        // Unknown variables should remain unchanged
        assertTrue(renderedTemplate.contains("{{unknown_variable}}"));
        assertTrue(renderedTemplate.contains("{{another.unknown}}"));
    }
}