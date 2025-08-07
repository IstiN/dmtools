package com.github.istin.dmtools.server;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.dto.*;
import com.github.istin.dmtools.server.model.ExecutionStatus;
import com.github.istin.dmtools.server.model.WebhookKey;
import com.github.istin.dmtools.server.service.JobConfigurationService;
import com.github.istin.dmtools.server.service.WebhookExamplesService;
import com.github.istin.dmtools.server.service.WebhookKeyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobConfigurationControllerWebhookTest {

    @Mock
    private JobConfigurationService jobConfigurationService;

    @Mock
    private UserService userService;

    @Mock
    private WebhookKeyService webhookKeyService;

    @Mock
    private WebhookExamplesService webhookExamplesService;

    @Mock
    private JobExecutionController jobExecutionController;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private JobConfigurationController controller;

    private WebhookKey testWebhookKey;
    private ExecutionParametersDto testExecutionParams;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Setup test user for webhook key
        User testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@example.com");
        
        // Setup test webhook key
        testWebhookKey = new WebhookKey();
        testWebhookKey.setId("webhook-key-123");
        testWebhookKey.setName("Test API Key");
        testWebhookKey.setEnabled(true);
        testWebhookKey.setCreatedBy(testUser);

        // Setup test execution parameters
        testExecutionParams = new ExecutionParametersDto();
        testExecutionParams.setJobType("Expert");
        testExecutionParams.setExecutionMode("SERVER_MANAGED");
        
        // Note: Authentication mocks will be setup per test as needed to avoid unnecessary stubbings
    }

    @Test
    void testExecuteJobConfigurationWebhook_Success() {
        // Arrange
        String jobConfigId = "job-config-123";
        String apiKey = "wk_testkey123456789012345678901234567890";
        String executionId = "exec-456";
        
        WebhookExecuteRequest request = new WebhookExecuteRequest();
        JsonNode jobParams = objectMapper.createObjectNode();
        request.setJobParameters(jobParams);

        // Mock successful job execution response
        JobExecutionResponse jobExecResponse = new JobExecutionResponse();
        jobExecResponse.setExecutionId(executionId);
        jobExecResponse.setStatus(ExecutionStatus.RUNNING);
        jobExecResponse.setJobConfigurationId(jobConfigId);
        jobExecResponse.setMessage("Job started successfully");

        when(webhookKeyService.validateApiKeyForJobConfig(apiKey, jobConfigId))
                .thenReturn(Optional.of(testWebhookKey));
        when(jobConfigurationService.getExecutionParameters(eq(jobConfigId), any(), anyString()))
                .thenReturn(Optional.of(testExecutionParams));
        when(jobExecutionController.executeSavedJobConfiguration(eq(jobConfigId), any(), any()))
                .thenReturn(ResponseEntity.ok(jobExecResponse));

        // Act
        ResponseEntity<WebhookExecutionResponse> response = controller.executeJobConfigurationWebhook(
                jobConfigId, request, apiKey);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDING", response.getBody().getStatus());
        assertEquals("Job execution started successfully", response.getBody().getMessage());
        assertEquals(jobConfigId, response.getBody().getJobConfigurationId());
        assertEquals(executionId, response.getBody().getExecutionId());

        verify(webhookKeyService).validateApiKeyForJobConfig(apiKey, jobConfigId);
        verify(jobConfigurationService).getExecutionParameters(eq(jobConfigId), any(), anyString());
        verify(jobExecutionController).executeSavedJobConfiguration(eq(jobConfigId), any(), any());
    }

    @Test
    void testExecuteJobConfigurationWebhook_InvalidApiKey() {
        // Arrange
        String jobConfigId = "job-config-123";
        String apiKey = "invalid-key";
        
        WebhookExecuteRequest request = new WebhookExecuteRequest();

        when(webhookKeyService.validateApiKeyForJobConfig(apiKey, jobConfigId))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<WebhookExecutionResponse> response = controller.executeJobConfigurationWebhook(
                jobConfigId, request, apiKey);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Invalid or missing API key"));

        verify(webhookKeyService).validateApiKeyForJobConfig(apiKey, jobConfigId);
        verify(jobConfigurationService, never()).getExecutionParameters(anyString(), any(), anyString());
        verify(jobConfigurationService, never()).recordExecution(anyString(), anyString());
    }

    @Test
    void testExecuteJobConfigurationWebhook_JobConfigNotFound() {
        // Arrange
        String jobConfigId = "job-config-123";
        String apiKey = "wk_testkey123456789012345678901234567890";
        
        WebhookExecuteRequest request = new WebhookExecuteRequest();

        when(webhookKeyService.validateApiKeyForJobConfig(apiKey, jobConfigId))
                .thenReturn(Optional.of(testWebhookKey));
        when(jobConfigurationService.getExecutionParameters(eq(jobConfigId), any(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<WebhookExecutionResponse> response = controller.executeJobConfigurationWebhook(
                jobConfigId, request, apiKey);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Job configuration not found"));

        verify(webhookKeyService).validateApiKeyForJobConfig(apiKey, jobConfigId);
        verify(jobConfigurationService).getExecutionParameters(eq(jobConfigId), any(), anyString());
        verify(jobConfigurationService, never()).recordExecution(anyString(), anyString());
    }

    @Test
    void testExecuteJobConfigurationWebhook_NullRequest() {
        // Arrange
        String jobConfigId = "job-config-123";
        String apiKey = "wk_testkey123456789012345678901234567890";
        String executionId = "exec-789";

        // Mock successful job execution response
        JobExecutionResponse jobExecResponse = new JobExecutionResponse();
        jobExecResponse.setExecutionId(executionId);
        jobExecResponse.setStatus(ExecutionStatus.RUNNING);
        jobExecResponse.setJobConfigurationId(jobConfigId);
        jobExecResponse.setMessage("Job started successfully");

        when(webhookKeyService.validateApiKeyForJobConfig(apiKey, jobConfigId))
                .thenReturn(Optional.of(testWebhookKey));
        when(jobConfigurationService.getExecutionParameters(eq(jobConfigId), any(), anyString()))
                .thenReturn(Optional.of(testExecutionParams));
        when(jobExecutionController.executeSavedJobConfiguration(eq(jobConfigId), any(), any()))
                .thenReturn(ResponseEntity.ok(jobExecResponse));

        // Act
        ResponseEntity<WebhookExecutionResponse> response = controller.executeJobConfigurationWebhook(
                jobConfigId, null, apiKey);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDING", response.getBody().getStatus());
        assertEquals("Job execution started successfully", response.getBody().getMessage());
        assertEquals(jobConfigId, response.getBody().getJobConfigurationId());
        assertEquals(executionId, response.getBody().getExecutionId());

        verify(jobConfigurationService).getExecutionParameters(eq(jobConfigId), any(), anyString());
        verify(jobExecutionController).executeSavedJobConfiguration(eq(jobConfigId), any(), any());
    }

    @Test
    void testCreateWebhookKey_Success() {
        // Arrange
        String jobConfigId = "job-config-123";
        CreateWebhookKeyRequest request = new CreateWebhookKeyRequest();
        request.setName("Test API Key");
        request.setDescription("Test description");

        CreateWebhookKeyResponse expectedResponse = CreateWebhookKeyResponse.create(
                "webhook-key-123", "wk_generated_key", "Test API Key", "Test description", 
                jobConfigId, LocalDateTime.now());

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookKeyService.createWebhookKey(jobConfigId, request, "user-123"))
                .thenReturn(Optional.of(expectedResponse));

        // Act
        ResponseEntity<CreateWebhookKeyResponse> response = controller.createWebhookKey(
                jobConfigId, request, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("webhook-key-123", response.getBody().getKeyId());
        assertEquals("Test API Key", response.getBody().getName());
        assertTrue(response.getBody().getApiKey().startsWith("wk_"));

        verify(webhookKeyService).createWebhookKey(jobConfigId, request, "user-123");
    }

    @Test
    void testCreateWebhookKey_JobConfigNotFound() {
        // Arrange
        String jobConfigId = "job-config-123";
        CreateWebhookKeyRequest request = new CreateWebhookKeyRequest();
        request.setName("Test API Key");

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookKeyService.createWebhookKey(jobConfigId, request, "user-123"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<CreateWebhookKeyResponse> response = controller.createWebhookKey(
                jobConfigId, request, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(webhookKeyService).createWebhookKey(jobConfigId, request, "user-123");
    }

    @Test
    void testCreateWebhookKey_InvalidRequest() {
        // Arrange
        String jobConfigId = "job-config-123";
        CreateWebhookKeyRequest request = new CreateWebhookKeyRequest();
        request.setName("Test API Key");

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookKeyService.createWebhookKey(jobConfigId, request, "user-123"))
                .thenThrow(new IllegalArgumentException("Invalid request"));

        // Act
        ResponseEntity<CreateWebhookKeyResponse> response = controller.createWebhookKey(
                jobConfigId, request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(webhookKeyService).createWebhookKey(jobConfigId, request, "user-123");
    }

    @Test
    void testGetWebhookKeys_Success() {
        // Arrange
        String jobConfigId = "job-config-123";
        
        WebhookKeyDto key1 = new WebhookKeyDto();
        key1.setKeyId("key1");
        key1.setName("Key 1");
        key1.setJobConfigurationId(jobConfigId);
        
        WebhookKeyDto key2 = new WebhookKeyDto();
        key2.setKeyId("key2");
        key2.setName("Key 2");
        key2.setJobConfigurationId(jobConfigId);

        List<WebhookKeyDto> expectedKeys = Arrays.asList(key1, key2);

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookKeyService.getWebhookKeys(jobConfigId, "user-123"))
                .thenReturn(expectedKeys);

        // Act
        ResponseEntity<List<WebhookKeyDto>> response = controller.getWebhookKeys(jobConfigId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("key1", response.getBody().get(0).getKeyId());
        assertEquals("key2", response.getBody().get(1).getKeyId());

        verify(webhookKeyService).getWebhookKeys(jobConfigId, "user-123");
    }

    @Test
    void testDeleteWebhookKey_Success() {
        // Arrange
        String jobConfigId = "job-config-123";
        String keyId = "webhook-key-123";

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookKeyService.deleteWebhookKey(jobConfigId, keyId, "user-123"))
                .thenReturn(true);

        // Act
        ResponseEntity<Void> response = controller.deleteWebhookKey(jobConfigId, keyId, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(webhookKeyService).deleteWebhookKey(jobConfigId, keyId, "user-123");
    }

    @Test
    void testDeleteWebhookKey_NotFound() {
        // Arrange
        String jobConfigId = "job-config-123";
        String keyId = "webhook-key-123";

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookKeyService.deleteWebhookKey(jobConfigId, keyId, "user-123"))
                .thenReturn(false);

        // Act
        ResponseEntity<Void> response = controller.deleteWebhookKey(jobConfigId, keyId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(webhookKeyService).deleteWebhookKey(jobConfigId, keyId, "user-123");
    }

    @Test
    void testGetWebhookExamples_Success() {
        // Arrange
        String jobConfigId = "job-config-123";
        
        WebhookExamplesDto.WebhookExampleTemplate template = new WebhookExamplesDto.WebhookExampleTemplate();
        template.setName("curl command");
        template.setRenderedTemplate("curl -X POST ...");

        WebhookExamplesDto expectedExamples = WebhookExamplesDto.create(
                jobConfigId, "Expert", "http://localhost:8080/webhook", 
                Arrays.asList(template), Arrays.asList("{{webhook_url}}", "{{api_key}}"));

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookExamplesService.getWebhookExamples(jobConfigId, "user-123"))
                .thenReturn(Optional.of(expectedExamples));

        // Act
        ResponseEntity<WebhookExamplesDto> response = controller.getWebhookExamples(jobConfigId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(jobConfigId, response.getBody().getJobConfigurationId());
        assertEquals("Expert", response.getBody().getJobType());
        assertEquals(1, response.getBody().getExamples().size());
        assertEquals("curl command", response.getBody().getExamples().get(0).getName());

        verify(webhookExamplesService).getWebhookExamples(jobConfigId, "user-123");
    }

    @Test
    void testGetWebhookExamples_NotFound() {
        // Arrange
        String jobConfigId = "job-config-123";

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookExamplesService.getWebhookExamples(jobConfigId, "user-123"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<WebhookExamplesDto> response = controller.getWebhookExamples(jobConfigId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(webhookExamplesService).getWebhookExamples(jobConfigId, "user-123");
    }

    @Test
    void testGetWebhookExamples_ServiceException() {
        // Arrange
        String jobConfigId = "job-config-123";

        // Setup authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("sub")).thenReturn("user-123");
        
        when(webhookExamplesService.getWebhookExamples(jobConfigId, "user-123"))
                .thenThrow(new IllegalArgumentException("User not found"));

        // Act
        ResponseEntity<WebhookExamplesDto> response = controller.getWebhookExamples(jobConfigId, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(webhookExamplesService).getWebhookExamples(jobConfigId, "user-123");
    }
}