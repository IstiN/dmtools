package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.dto.CreateWebhookKeyRequest;
import com.github.istin.dmtools.dto.CreateWebhookKeyResponse;
import com.github.istin.dmtools.dto.WebhookKeyDto;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.WebhookKey;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
import com.github.istin.dmtools.server.repository.WebhookKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebhookKeyServiceTest {

    @Mock
    private WebhookKeyRepository webhookKeyRepository;

    @Mock
    private JobConfigurationRepository jobConfigurationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WebhookKeyService webhookKeyService;

    private User testUser;
    private JobConfiguration testJobConfig;
    private WebhookKey testWebhookKey;
    private CreateWebhookKeyRequest createRequest;

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
        testJobConfig.setName("Test Job Config");
        testJobConfig.setJobType("Expert");
        testJobConfig.setCreatedBy(testUser);
        testJobConfig.setCreatedAt(LocalDateTime.now());

        // Setup test webhook key
        testWebhookKey = new WebhookKey();
        testWebhookKey.setId("webhook-key-123");
        testWebhookKey.setName("Test API Key");
        testWebhookKey.setDescription("Test description");
        testWebhookKey.setJobConfiguration(testJobConfig);
        testWebhookKey.setCreatedBy(testUser);
        testWebhookKey.setKeyHash("abcd1234567890abcd1234567890abcd1234567890abcd1234567890abcd1234");
        testWebhookKey.setKeyPrefix("wk_abcd123");
        testWebhookKey.setEnabled(true);
        testWebhookKey.setCreatedAt(LocalDateTime.now());
        testWebhookKey.setUsageCount(0);

        // Setup create request
        createRequest = new CreateWebhookKeyRequest();
        createRequest.setName("Test API Key");
        createRequest.setDescription("Test description");
    }

    @Test
    void testCreateWebhookKey_Success() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(webhookKeyRepository.existsByJobConfigurationAndNameIgnoreCase(testJobConfig, "Test API Key"))
                .thenReturn(false);
        when(webhookKeyRepository.save(any(WebhookKey.class))).thenReturn(testWebhookKey);

        // Act
        Optional<CreateWebhookKeyResponse> result = webhookKeyService.createWebhookKey(
                "job-config-123", createRequest, "user-123");

        // Assert
        assertTrue(result.isPresent());
        CreateWebhookKeyResponse response = result.get();
        assertEquals("webhook-key-123", response.getKeyId());
        assertEquals("Test API Key", response.getName());
        assertEquals("Test description", response.getDescription());
        assertEquals("job-config-123", response.getJobConfigurationId());
        assertNotNull(response.getApiKey());
        assertTrue(response.getApiKey().startsWith("wk_"));

        // Verify interactions
        ArgumentCaptor<WebhookKey> webhookKeyCaptor = ArgumentCaptor.forClass(WebhookKey.class);
        verify(webhookKeyRepository).save(webhookKeyCaptor.capture());
        WebhookKey savedKey = webhookKeyCaptor.getValue();
        assertEquals("Test API Key", savedKey.getName());
        assertEquals("Test description", savedKey.getDescription());
        assertEquals(testJobConfig, savedKey.getJobConfiguration());
        assertEquals(testUser, savedKey.getCreatedBy());
        assertTrue(savedKey.isEnabled());
        assertNotNull(savedKey.getKeyHash());
        assertNotNull(savedKey.getKeyPrefix());
    }

    @Test
    void testCreateWebhookKey_UserNotFound() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            webhookKeyService.createWebhookKey("job-config-123", createRequest, "user-123");
        });

        verify(jobConfigurationRepository, never()).findByIdAndCreatedBy(anyString(), any());
        verify(webhookKeyRepository, never()).save(any());
    }

    @Test
    void testCreateWebhookKey_JobConfigNotFound() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.empty());

        // Act
        Optional<CreateWebhookKeyResponse> result = webhookKeyService.createWebhookKey(
                "job-config-123", createRequest, "user-123");

        // Assert
        assertFalse(result.isPresent());
        verify(webhookKeyRepository, never()).save(any());
    }

    @Test
    void testCreateWebhookKey_DuplicateName() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(webhookKeyRepository.existsByJobConfigurationAndNameIgnoreCase(testJobConfig, "Test API Key"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            webhookKeyService.createWebhookKey("job-config-123", createRequest, "user-123");
        });

        verify(webhookKeyRepository, never()).save(any());
    }

    @Test
    void testGetWebhookKeys_Success() {
        // Arrange
        WebhookKey key1 = new WebhookKey();
        key1.setId("key1");
        key1.setName("Key 1");
        key1.setJobConfiguration(testJobConfig);
        key1.setCreatedBy(testUser);
        key1.setKeyPrefix("wk_abc123");
        key1.setEnabled(true);
        key1.setCreatedAt(LocalDateTime.now());
        key1.setUsageCount(5);

        WebhookKey key2 = new WebhookKey();
        key2.setId("key2");
        key2.setName("Key 2");
        key2.setJobConfiguration(testJobConfig);
        key2.setCreatedBy(testUser);
        key2.setKeyPrefix("wk_def456");
        key2.setEnabled(false);
        key2.setCreatedAt(LocalDateTime.now());
        key2.setUsageCount(10);

        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(webhookKeyRepository.findByJobConfigurationIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Arrays.asList(key1, key2));

        // Act
        List<WebhookKeyDto> result = webhookKeyService.getWebhookKeys("job-config-123", "user-123");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        WebhookKeyDto dto1 = result.get(0);
        assertEquals("key1", dto1.getKeyId());
        assertEquals("Key 1", dto1.getName());
        assertEquals("job-config-123", dto1.getJobConfigurationId());
        assertEquals("wk_abc123", dto1.getKeyPrefix());
        assertTrue(dto1.isEnabled());
        assertEquals(5, dto1.getUsageCount());

        WebhookKeyDto dto2 = result.get(1);
        assertEquals("key2", dto2.getKeyId());
        assertEquals("Key 2", dto2.getName());
        assertFalse(dto2.isEnabled());
        assertEquals(10, dto2.getUsageCount());
    }

    @Test
    void testDeleteWebhookKey_Success() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(webhookKeyRepository.findByIdAndJobConfiguration("webhook-key-123", testJobConfig))
                .thenReturn(Optional.of(testWebhookKey));

        // Act
        boolean result = webhookKeyService.deleteWebhookKey("job-config-123", "webhook-key-123", "user-123");

        // Assert
        assertTrue(result);
        verify(webhookKeyRepository).delete(testWebhookKey);
    }

    @Test
    void testDeleteWebhookKey_KeyNotFound() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(webhookKeyRepository.findByIdAndJobConfiguration("webhook-key-123", testJobConfig))
                .thenReturn(Optional.empty());

        // Act
        boolean result = webhookKeyService.deleteWebhookKey("job-config-123", "webhook-key-123", "user-123");

        // Assert
        assertFalse(result);
        verify(webhookKeyRepository, never()).delete(any());
    }

    @Test
    void testDeleteWebhookKey_UnauthorizedUser() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId("another-user");
        testWebhookKey.setCreatedBy(anotherUser);

        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jobConfigurationRepository.findByIdAndCreatedBy("job-config-123", testUser))
                .thenReturn(Optional.of(testJobConfig));
        when(webhookKeyRepository.findByIdAndJobConfiguration("webhook-key-123", testJobConfig))
                .thenReturn(Optional.of(testWebhookKey));

        // Act
        boolean result = webhookKeyService.deleteWebhookKey("job-config-123", "webhook-key-123", "user-123");

        // Assert
        assertFalse(result);
        verify(webhookKeyRepository, never()).delete(any());
    }

    @Test
    void testValidateApiKeyForJobConfig_Success() {
        // Arrange
        String apiKey = "wk_testkey123456789012345678901234567890";
        when(webhookKeyRepository.findByKeyHashAndJobConfigurationId(anyString(), eq("job-config-123")))
                .thenReturn(Optional.of(testWebhookKey));
        when(webhookKeyRepository.save(testWebhookKey)).thenReturn(testWebhookKey);

        // Act
        Optional<WebhookKey> result = webhookKeyService.validateApiKeyForJobConfig(apiKey, "job-config-123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testWebhookKey, result.get());
        
        // Verify usage was recorded
        verify(webhookKeyRepository).save(testWebhookKey);
        // Note: We can't verify usage count increment due to method call on the object
    }

    @Test
    void testValidateApiKeyForJobConfig_InvalidKey() {
        // Arrange
        String apiKey = "invalid-key";
        when(webhookKeyRepository.findByKeyHashAndJobConfigurationId(anyString(), eq("job-config-123")))
                .thenReturn(Optional.empty());

        // Act
        Optional<WebhookKey> result = webhookKeyService.validateApiKeyForJobConfig(apiKey, "job-config-123");

        // Assert
        assertFalse(result.isPresent());
        verify(webhookKeyRepository, never()).save(any());
    }

    @Test
    void testValidateApiKeyForJobConfig_DisabledKey() {
        // Arrange
        testWebhookKey.setEnabled(false);
        String apiKey = "wk_testkey123456789012345678901234567890";
        when(webhookKeyRepository.findByKeyHashAndJobConfigurationId(anyString(), eq("job-config-123")))
                .thenReturn(Optional.of(testWebhookKey));

        // Act
        Optional<WebhookKey> result = webhookKeyService.validateApiKeyForJobConfig(apiKey, "job-config-123");

        // Assert
        assertFalse(result.isPresent());
        verify(webhookKeyRepository, never()).save(any());
    }

    @Test
    void testValidateApiKeyForJobConfig_NullOrEmptyKey() {
        // Test null key
        Optional<WebhookKey> result1 = webhookKeyService.validateApiKeyForJobConfig(null, "job-config-123");
        assertFalse(result1.isPresent());

        // Test empty key
        Optional<WebhookKey> result2 = webhookKeyService.validateApiKeyForJobConfig("", "job-config-123");
        assertFalse(result2.isPresent());

        // Test whitespace key
        Optional<WebhookKey> result3 = webhookKeyService.validateApiKeyForJobConfig("   ", "job-config-123");
        assertFalse(result3.isPresent());

        verify(webhookKeyRepository, never()).findByKeyHashAndJobConfigurationId(anyString(), anyString());
    }
}