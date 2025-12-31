package com.github.istin.dmtools.atlassian.jira;

import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient.TextType;
import com.github.istin.dmtools.common.utils.CacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JiraClient field resolution functionality.
 * These tests verify the current behavior before implementing performance optimizations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JiraClientFieldResolutionTest {

    private TestableJiraClient jiraClient;
    
    @Mock
    private CacheManager mockCacheManager;
    
    @Mock
    private Logger mockLogger;

    /**
     * Testable JiraClient subclass that allows us to test protected/private methods
     */
    private static class TestableJiraClient extends JiraClient<Ticket> {
        private final Map<String, String> mockResponses = new ConcurrentHashMap<>();
        private int getFieldsCallCount = 0;
        
        public TestableJiraClient(CacheManager cacheManager, Logger logger) throws IOException {
            super("https://test.atlassian.net", "test-token", logger);
        }
        
        // Override getFields to track calls and return mock responses
        @Override
        public String getFields(String project) throws IOException {
            getFieldsCallCount++;
            return mockResponses.getOrDefault(project, createDefaultFieldsResponse());
        }
        
        public void setMockFieldsResponse(String project, String response) {
            mockResponses.put(project, response);
        }
        
        public int getGetFieldsCallCount() {
            return getFieldsCallCount;
        }
        
        @SuppressWarnings("unused")
        public void resetCallCount() {
            getFieldsCallCount = 0;
        }
        
        private String createDefaultFieldsResponse() {
            return """
                [
                  {
                    "id": "summary",
                    "name": "Summary",
                    "custom": false,
                    "orderable": true,
                    "navigable": true,
                    "searchable": true,
                    "clauseNames": ["summary"],
                    "schema": {
                      "type": "string",
                      "system": "summary"
                    }
                  },
                  {
                    "id": "description",
                    "name": "Description",
                    "custom": false,
                    "orderable": true,
                    "navigable": true,
                    "searchable": true,
                    "clauseNames": ["description"],
                    "schema": {
                      "type": "string",
                      "system": "description"
                    }
                  },
                  {
                    "id": "customfield_10004",
                    "name": "Epic Link",
                    "custom": true,
                    "orderable": true,
                    "navigable": true,
                    "searchable": true,
                    "clauseNames": ["cf[10004]", "Epic Link"],
                    "schema": {
                      "type": "string",
                      "custom": "com.atlassian.jira.plugin.system.customfieldtypes:textfield",
                      "customId": 10004
                    }
                  },
                  {
                    "id": "customfield_10001",
                    "name": "Story Points",
                    "custom": true,
                    "orderable": true,
                    "navigable": true,
                    "searchable": true,
                    "clauseNames": ["cf[10001]", "Story Points"],
                    "schema": {
                      "type": "number",
                      "custom": "com.atlassian.jira.plugin.system.customfieldtypes:float",
                      "customId": 10001
                    }
                  }
                ]
                """;
        }
        
        // Required abstract method implementations
        @Override
        public String getTextFieldsOnly(ITicket ticket) {
            try {
                return ticket.toText();
            } catch (IOException e) {
                return "Error getting text: " + e.getMessage();
            }
        }
        
        @Override
        public String[] getDefaultQueryFields() {
            return new String[]{"summary", "description", "status"};
        }
        
        @Override
        public String[] getExtendedQueryFields() {
            return new String[]{"summary", "description", "status", "priority", "assignee"};
        }
        
        @Override
        public TextType getTextType() {
            return TextType.HTML;
        }
        
        @Override
        public List<? extends ITicket> getTestCases(ITicket ticket) throws IOException {
            return new ArrayList<>();
        }
        
        // convertToTicket is not abstract, so we don't need to override it
        
        // Expose protected methods for testing
        public String testGetFieldCustomCode(String project, String fieldName) throws IOException {
            return getFieldCustomCode(project, fieldName);
        }
        
        @SuppressWarnings("unchecked")
        public Map<String, String> testGetFieldMappingForProject(String projectKey) throws IOException {
            try {
                Method method = JiraClient.class.getDeclaredMethod("getFieldMappingForProject", String.class);
                method.setAccessible(true);
                return (Map<String, String>) method.invoke(this, projectKey);
            } catch (Exception e) {
                throw new IOException("Failed to call getFieldMappingForProject", e);
            }
        }
        
        public String testResolveFieldNameToCustomFieldId(String projectKey, String fieldName) throws IOException {
            try {
                Method method = JiraClient.class.getDeclaredMethod("resolveFieldNameToCustomFieldId", String.class, String.class);
                method.setAccessible(true);
                return (String) method.invoke(this, projectKey, fieldName);
            } catch (Exception e) {
                throw new IOException("Failed to call resolveFieldNameToCustomFieldId", e);
            }
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        jiraClient = new TestableJiraClient(mockCacheManager, mockLogger);
        
        // Setup cache manager to simulate current caching behavior
        when(mockCacheManager.getOrComputeWithTTL(anyString(), any(), anyLong()))
            .thenAnswer(invocation -> {
                // Simulate cache miss - always compute
                return invocation.getArgument(1, java.util.function.Supplier.class).get();
            });
    }

    @Test
    void testGetFieldCustomCode_CloudJira_FoundField() throws IOException {
        // Arrange
        String project = "TEST";
        String fieldName = "Epic Link";

        jiraClient.setMockFieldsResponse(project, """
            [
              {
                "id": "customfield_10004",
                "name": "Epic Link",
                "custom": true,
                "orderable": true,
                "navigable": true,
                "searchable": true,
                "clauseNames": ["cf[10004]", "Epic Link"],
                "schema": {
                  "type": "string",
                  "custom": "com.atlassian.jira.plugin.system.customfieldtypes:textfield",
                  "customId": 10004
                }
              }
            ]
            """);

        // Act
        String result = jiraClient.testGetFieldCustomCode(project, fieldName);

        // Assert
        assertEquals("customfield_10004", result);
        assertEquals(1, jiraClient.getGetFieldsCallCount());
    }

    @Test
    void testGetFieldCustomCode_CloudJira_FieldNotFound() throws IOException {
        // Arrange
        String project = "TEST";
        String fieldName = "NonExistentField";

        jiraClient.setMockFieldsResponse(project, """
            [
              {
                "id": "customfield_10004",
                "name": "Epic Link",
                "custom": true,
                "orderable": true,
                "navigable": true,
                "searchable": true,
                "clauseNames": ["cf[10004]", "Epic Link"],
                "schema": {
                  "type": "string",
                  "custom": "com.atlassian.jira.plugin.system.customfieldtypes:textfield",
                  "customId": 10004
                }
              }
            ]
            """);

        // Act
        String result = jiraClient.testGetFieldCustomCode(project, fieldName);

        // Assert
        assertNull(result);
        assertEquals(1, jiraClient.getGetFieldsCallCount());
    }

    @Test
    void testGetFieldCustomCode_RepeatedCallsForSameField() throws IOException {
        // Arrange
        String project = "TEST";
        String fieldName = "Epic Link";

        jiraClient.setMockFieldsResponse(project, """
            [
              {
                "id": "customfield_10004",
                "name": "Epic Link",
                "custom": true,
                "orderable": true,
                "navigable": true,
                "searchable": true,
                "clauseNames": ["cf[10004]", "Epic Link"],
                "schema": {
                  "type": "string",
                  "custom": "com.atlassian.jira.plugin.system.customfieldtypes:textfield",
                  "customId": 10004
                }
              }
            ]
            """);

        // Act - Call multiple times for same field
        String result1 = jiraClient.testGetFieldCustomCode(project, fieldName);
        String result2 = jiraClient.testGetFieldCustomCode(project, fieldName);
        String result3 = jiraClient.testGetFieldCustomCode(project, fieldName);

        // Assert - Should return same result but verify API call count
        assertEquals("customfield_10004", result1);
        assertEquals("customfield_10004", result2);
        assertEquals("customfield_10004", result3);
        
        // This test documents current behavior - multiple API calls for same field
        // After optimization, this should be reduced via caching
        assertEquals(3, jiraClient.getGetFieldsCallCount());
    }

    @Test
    void testGetFieldMappingForProject_CacheBehavior() throws IOException {
        // Arrange
        String project = "TEST";

        // Act - Note: TestableJiraClient might not use the cache manager directly
        Map<String, String> result1 = jiraClient.testGetFieldMappingForProject(project);
        Map<String, String> result2 = jiraClient.testGetFieldMappingForProject(project);

        // Assert - Check that the expected fields are present (there may be more)
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.containsKey("epic link"));
        assertTrue(result1.containsKey("story points"));
        assertEquals("customfield_10004", result1.get("epic link"));
        assertEquals("customfield_10001", result1.get("story points"));
        
        // Results should be consistent
        assertEquals(result1, result2);
    }

    @Test
    void testResolveFieldNameToCustomFieldId_AlreadyCustomFieldId() throws IOException {
        // Arrange
        String project = "TEST";
        String customFieldId = "customfield_10004";

        // Act
        String result = jiraClient.testResolveFieldNameToCustomFieldId(project, customFieldId);

        // Assert
        assertEquals(customFieldId, result);
        assertEquals(0, jiraClient.getGetFieldsCallCount()); // Should not make API calls for custom field IDs
    }

    @Test
    void testResolveFieldNameToCustomFieldId_UserFriendlyName() throws IOException {
        // Arrange
        String project = "TEST";
        String fieldName = "Epic Link";
        
        Map<String, String> mockMapping = new ConcurrentHashMap<>();
        mockMapping.put("epic link", "customfield_10004");
        
        when(mockCacheManager.getOrComputeWithTTL(eq("fieldMapping_" + project), any(), anyLong()))
            .thenReturn(mockMapping);

        // Act
        String result = jiraClient.testResolveFieldNameToCustomFieldId(project, fieldName);

        // Assert
        assertEquals("customfield_10004", result);
    }

    @Test
    void testResolveFieldNameToCustomFieldId_NotFoundInMapping() throws IOException {
        // Arrange
        String project = "TEST";
        String fieldName = "UnknownField";
        
        Map<String, String> emptyMapping = new ConcurrentHashMap<>();
        
        when(mockCacheManager.getOrComputeWithTTL(eq("fieldMapping_" + project), any(), anyLong()))
            .thenReturn(emptyMapping);

        // Act
        String result = jiraClient.testResolveFieldNameToCustomFieldId(project, fieldName);

        // Assert
        assertNull(result);
    }

    @Test
    void testFieldResolution_Performance_MultipleFieldsForSameProject() throws IOException {
        // Arrange
        String project = "DMC";
        String[] fields = {"Epic Link", "Story Points", "Priority", "Components", "FixVersions"};

        jiraClient.setMockFieldsResponse(project, """
            [
              {
                "id": "customfield_10004",
                "name": "Epic Link",
                "custom": true,
                "orderable": true,
                "navigable": true,
                "searchable": true,
                "clauseNames": ["cf[10004]", "Epic Link"],
                "schema": {
                  "type": "string",
                  "custom": "com.atlassian.jira.plugin.system.customfieldtypes:textfield",
                  "customId": 10004
                }
              },
              {
                "id": "customfield_10001",
                "name": "Story Points",
                "custom": true,
                "orderable": true,
                "navigable": true,
                "searchable": true,
                "clauseNames": ["cf[10001]", "Story Points"],
                "schema": {
                  "type": "number",
                  "custom": "com.atlassian.jira.plugin.system.customfieldtypes:float",
                  "customId": 10001
                }
              }
            ]
            """);

        long startTime = System.currentTimeMillis();

        // Act - Resolve multiple fields
        for (String field : fields) {
            jiraClient.testGetFieldCustomCode(project, field);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert - Document current performance characteristics
        System.out.println("Field resolution for " + fields.length + " fields took: " + duration + "ms");
        assertEquals(fields.length, jiraClient.getGetFieldsCallCount()); // Should be optimized to 1 call after changes
        
        // Performance expectation: after optimization, should complete in < 100ms for 5 fields
        // Current behavior: each field resolution makes separate API call
    }

    @Test
    void testFieldResolution_MultipleProjects() throws IOException {
        // Arrange
        String[] projects = {"DMC", "TEST", "PROJ"};
        String fieldName = "Epic Link";

        // Act - Resolve same field for different projects
        for (String project : projects) {
            jiraClient.testGetFieldCustomCode(project, fieldName);
        }

        // Assert - Should make one call per project (expected current behavior)
        assertEquals(projects.length, jiraClient.getGetFieldsCallCount());
    }

    @Test
    void testFieldResolution_CaseInsensitive() throws IOException {
        // Arrange
        String project = "TEST";
        
        Map<String, String> mockMapping = new ConcurrentHashMap<>();
        mockMapping.put("epic link", "customfield_10004"); // lowercase in cache
        
        when(mockCacheManager.getOrComputeWithTTL(eq("fieldMapping_" + project), any(), anyLong()))
            .thenReturn(mockMapping);

        // Act - Test different case variations
        String result1 = jiraClient.testResolveFieldNameToCustomFieldId(project, "Epic Link");
        String result2 = jiraClient.testResolveFieldNameToCustomFieldId(project, "EPIC LINK");
        String result3 = jiraClient.testResolveFieldNameToCustomFieldId(project, "epic link");

        // Assert - Should handle case-insensitive lookup
        assertEquals("customfield_10004", result1);
        assertEquals("customfield_10004", result2);
        assertEquals("customfield_10004", result3);
    }

    @Test
    void testFieldResolution_ErrorHandling() throws IOException {
        // Arrange
        String project = "ERROR_PROJECT";
        String fieldName = "TestField";
        
        // Mock getFields to throw exception
        TestableJiraClient errorClient = new TestableJiraClient(mockCacheManager, mockLogger) {
            @Override
            public String getFields(String project) throws IOException {
                throw new IOException("API Error");
            }
        };

        // Act & Assert - Should propagate the IOException (current behavior)
        assertThrows(IOException.class, () -> {
            errorClient.testGetFieldCustomCode(project, fieldName);
        });
    }
}
