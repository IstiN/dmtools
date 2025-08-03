package com.github.istin.dmtools.server.model;

import com.github.istin.dmtools.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookKeyTest {

    private WebhookKey webhookKey;
    private JobConfiguration jobConfig;
    private User user;

    @BeforeEach
    void setUp() {
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
        webhookKey.setKeyHash("abcd1234567890");
        webhookKey.setKeyPrefix("wk_abc123");
        webhookKey.setEnabled(true);
        webhookKey.setUsageCount(0);
    }

    @Test
    void testDefaultConstructor() {
        // Act
        WebhookKey key = new WebhookKey();

        // Assert
        assertNotNull(key);
        assertNull(key.getId());
        assertNull(key.getName());
        assertNull(key.getDescription());
        assertNull(key.getJobConfiguration());
        assertNull(key.getCreatedBy());
        assertNull(key.getKeyHash());
        assertNull(key.getKeyPrefix());
        assertTrue(key.isEnabled()); // Default value is true
        assertNull(key.getCreatedAt());
        assertNull(key.getLastUsedAt());
        assertEquals(0, key.getUsageCount()); // Default value is 0
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        WebhookKey key = new WebhookKey(
                "key-123", "Test Key", "Description", jobConfig, user,
                "hash123", "wk_prefix", true, now, now.plusHours(1), 5
        );

        // Assert
        assertEquals("key-123", key.getId());
        assertEquals("Test Key", key.getName());
        assertEquals("Description", key.getDescription());
        assertEquals(jobConfig, key.getJobConfiguration());
        assertEquals(user, key.getCreatedBy());
        assertEquals("hash123", key.getKeyHash());
        assertEquals("wk_prefix", key.getKeyPrefix());
        assertTrue(key.isEnabled());
        assertEquals(now, key.getCreatedAt());
        assertEquals(now.plusHours(1), key.getLastUsedAt());
        assertEquals(5, key.getUsageCount());
    }

    @Test
    void testRecordUsage() {
        // Arrange
        LocalDateTime beforeUsage = LocalDateTime.now().minusMinutes(1);
        webhookKey.setUsageCount(5);
        webhookKey.setLastUsedAt(beforeUsage);

        // Act
        webhookKey.recordUsage();

        // Assert
        assertEquals(6, webhookKey.getUsageCount());
        assertNotNull(webhookKey.getLastUsedAt());
        assertTrue(webhookKey.getLastUsedAt().isAfter(beforeUsage));
    }

    @Test
    void testRecordUsage_FromZero() {
        // Arrange
        webhookKey.setUsageCount(0);
        webhookKey.setLastUsedAt(null);

        // Act
        webhookKey.recordUsage();

        // Assert
        assertEquals(1, webhookKey.getUsageCount());
        assertNotNull(webhookKey.getLastUsedAt());
    }

    @Test
    void testIsActive_Enabled() {
        // Arrange
        webhookKey.setEnabled(true);

        // Act & Assert
        assertTrue(webhookKey.isActive());
    }

    @Test
    void testIsActive_Disabled() {
        // Arrange
        webhookKey.setEnabled(false);

        // Act & Assert
        assertFalse(webhookKey.isActive());
    }

    @Test
    void testDisable() {
        // Arrange
        webhookKey.setEnabled(true);

        // Act
        webhookKey.disable();

        // Assert
        assertFalse(webhookKey.isEnabled());
        assertFalse(webhookKey.isActive());
    }

    @Test
    void testOnCreate() {
        // Arrange
        WebhookKey newKey = new WebhookKey();
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

        // Act
        newKey.onCreate(); // This would normally be called by JPA @PrePersist

        // Assert
        assertNotNull(newKey.getCreatedAt());
        assertTrue(newKey.getCreatedAt().isAfter(beforeCreate));
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        LocalDateTime testTime = LocalDateTime.now();

        // Act
        webhookKey.setId("new-id");
        webhookKey.setName("New Name");
        webhookKey.setDescription("New Description");
        webhookKey.setKeyHash("newhash");
        webhookKey.setKeyPrefix("wk_new");
        webhookKey.setEnabled(false);
        webhookKey.setCreatedAt(testTime);
        webhookKey.setLastUsedAt(testTime.plusHours(2));
        webhookKey.setUsageCount(99);

        // Assert
        assertEquals("new-id", webhookKey.getId());
        assertEquals("New Name", webhookKey.getName());
        assertEquals("New Description", webhookKey.getDescription());
        assertEquals("newhash", webhookKey.getKeyHash());
        assertEquals("wk_new", webhookKey.getKeyPrefix());
        assertFalse(webhookKey.isEnabled());
        assertEquals(testTime, webhookKey.getCreatedAt());
        assertEquals(testTime.plusHours(2), webhookKey.getLastUsedAt());
        assertEquals(99, webhookKey.getUsageCount());
    }
}