package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.WebhookKey;
import com.github.istin.dmtools.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookKeyDtoTest {

    private WebhookKey webhookKey;
    private JobConfiguration jobConfig;
    private User user;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        user = new User();
        user.setId("user-123");

        jobConfig = new JobConfiguration();
        jobConfig.setId("job-config-123");

        webhookKey = new WebhookKey();
        webhookKey.setId("webhook-key-123");
        webhookKey.setName("Test API Key");
        webhookKey.setDescription("Test description");
        webhookKey.setJobConfiguration(jobConfig);
        webhookKey.setCreatedBy(user);
        webhookKey.setKeyPrefix("wk_abc123");
        webhookKey.setEnabled(true);
        webhookKey.setUsageCount(42);
        webhookKey.setCreatedAt(testTime);
        webhookKey.setLastUsedAt(testTime.plusHours(1));
    }

    @Test
    void testFromEntity() {
        // Act
        WebhookKeyDto dto = WebhookKeyDto.fromEntity(webhookKey);

        // Assert
        assertNotNull(dto);
        assertEquals("webhook-key-123", dto.getKeyId());
        assertEquals("Test API Key", dto.getName());
        assertEquals("Test description", dto.getDescription());
        assertEquals("job-config-123", dto.getJobConfigurationId());
        assertEquals("wk_abc123", dto.getKeyPrefix());
        assertTrue(dto.isEnabled());
        assertEquals(42, dto.getUsageCount());
        assertEquals(testTime, dto.getCreatedAt());
        assertEquals(testTime.plusHours(1), dto.getLastUsedAt());
    }

    @Test
    void testFromEntity_WithNullValues() {
        // Arrange
        webhookKey.setDescription(null);
        webhookKey.setLastUsedAt(null);

        // Act
        WebhookKeyDto dto = WebhookKeyDto.fromEntity(webhookKey);

        // Assert
        assertNotNull(dto);
        assertEquals("webhook-key-123", dto.getKeyId());
        assertEquals("Test API Key", dto.getName());
        assertNull(dto.getDescription());
        assertEquals("job-config-123", dto.getJobConfigurationId());
        assertEquals("wk_abc123", dto.getKeyPrefix());
        assertTrue(dto.isEnabled());
        assertEquals(42, dto.getUsageCount());
        assertEquals(testTime, dto.getCreatedAt());
        assertNull(dto.getLastUsedAt());
    }

    @Test
    void testDefaultConstructor() {
        // Act
        WebhookKeyDto dto = new WebhookKeyDto();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getKeyId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getJobConfigurationId());
        assertNull(dto.getKeyPrefix());
        assertFalse(dto.isEnabled()); // boolean defaults to false
        assertEquals(0, dto.getUsageCount()); // long defaults to 0
        assertNull(dto.getCreatedAt());
        assertNull(dto.getLastUsedAt());
    }

    @Test
    void testAllArgsConstructor() {
        // Act
        WebhookKeyDto dto = new WebhookKeyDto(
                "key-456", "Another Key", "Another description", "job-456", 
                "wk_def456", false, 10, testTime, testTime.plusDays(1));

        // Assert
        assertEquals("key-456", dto.getKeyId());
        assertEquals("Another Key", dto.getName());
        assertEquals("Another description", dto.getDescription());
        assertEquals("job-456", dto.getJobConfigurationId());
        assertEquals("wk_def456", dto.getKeyPrefix());
        assertFalse(dto.isEnabled());
        assertEquals(10, dto.getUsageCount());
        assertEquals(testTime, dto.getCreatedAt());
        assertEquals(testTime.plusDays(1), dto.getLastUsedAt());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        WebhookKeyDto dto = new WebhookKeyDto();

        // Act
        dto.setKeyId("test-key");
        dto.setName("Test Name");
        dto.setDescription("Test Desc");
        dto.setJobConfigurationId("test-job");
        dto.setKeyPrefix("wk_test");
        dto.setEnabled(true);
        dto.setUsageCount(99);
        dto.setCreatedAt(testTime);
        dto.setLastUsedAt(testTime.plusMinutes(30));

        // Assert
        assertEquals("test-key", dto.getKeyId());
        assertEquals("Test Name", dto.getName());
        assertEquals("Test Desc", dto.getDescription());
        assertEquals("test-job", dto.getJobConfigurationId());
        assertEquals("wk_test", dto.getKeyPrefix());
        assertTrue(dto.isEnabled());
        assertEquals(99, dto.getUsageCount());
        assertEquals(testTime, dto.getCreatedAt());
        assertEquals(testTime.plusMinutes(30), dto.getLastUsedAt());
    }
}