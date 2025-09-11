package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.auth.service.McpConfigurationService;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.mcp.generated.MCPSchemaGenerator;
import com.github.istin.dmtools.mcp.generated.MCPToolExecutor;
import com.github.istin.dmtools.auth.repository.McpConfigurationRepository;
import com.github.istin.dmtools.server.service.FileDownloadService;
import com.github.istin.dmtools.server.service.IntegrationResolutionHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpConfigurationResolverServiceTest {

    @Mock
    private McpConfigurationRepository mcpConfigurationRepository;
    
    @Mock
    private McpConfigurationService mcpConfigurationService;
    
    @Mock
    private IntegrationService integrationService;
    
    @Mock
    private IntegrationResolutionHelper integrationResolutionHelper;
    
    @Mock
    private FileDownloadService fileDownloadService;

    @InjectMocks
    private McpConfigurationResolverService mcpConfigurationResolverService;

    private McpConfiguration testConfiguration;
    private String configId;

    @BeforeEach
    void setUp() {
        configId = "test-config-123";
        
        User testUser = new User();
        testUser.setId("user123");
        
        testConfiguration = new McpConfiguration();
        testConfiguration.setId(configId);
        testConfiguration.setName("Test Configuration");
        testConfiguration.setUser(testUser);
        testConfiguration.setIntegrationIds(Arrays.asList("jira-integration", "confluence-integration"));
    }

    @Test
    void testResolveMcpConfiguration_Success() throws Exception {
        // Given
        when(mcpConfigurationService.findById(configId)).thenReturn(testConfiguration);
        
        // Mock integrations
        IntegrationDto jiraIntegration = new IntegrationDto();
        jiraIntegration.setId("jira-integration");
        jiraIntegration.setType("jira");
        
        IntegrationDto confluenceIntegration = new IntegrationDto();
        confluenceIntegration.setId("confluence-integration");
        confluenceIntegration.setType("confluence");
        
        when(integrationService.getIntegrationById("jira-integration", "user123", false))
            .thenReturn(jiraIntegration);
        when(integrationService.getIntegrationById("confluence-integration", "user123", false))
            .thenReturn(confluenceIntegration);

        // When
        McpConfigurationResolverService.McpConfigurationResult result = 
            mcpConfigurationResolverService.resolveMcpConfiguration(configId);

        // Then
        assertNotNull(result);
        assertEquals(testConfiguration, result.getMcpConfig());
        assertEquals("user123", result.getUserId());
        assertEquals(Arrays.asList("jira-integration", "confluence-integration"), result.getIntegrationIds());
        assertNotNull(result.getIntegrationTypes());
        assertEquals(2, result.getIntegrationTypes().size());
        assertTrue(result.getIntegrationTypes().contains("jira"));
        assertTrue(result.getIntegrationTypes().contains("confluence"));
        
        verify(mcpConfigurationService).findById(configId);
        verify(integrationService).getIntegrationById("jira-integration", "user123", false);
        verify(integrationService).getIntegrationById("confluence-integration", "user123", false);
    }

    @Test
    void testResolveMcpConfiguration_NotFound() throws Exception {
        // Given
        when(mcpConfigurationService.findById(configId)).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, 
            () -> mcpConfigurationResolverService.resolveMcpConfiguration(configId));
            
        verify(mcpConfigurationService).findById(configId);
    }

    @Test
    void testGetToolsListAsMap_WithConfigId() throws Exception {
        // Given
        when(mcpConfigurationService.findById(configId)).thenReturn(testConfiguration);
        
        // Mock integrations
        IntegrationDto jiraIntegration = new IntegrationDto();
        jiraIntegration.setId("jira-integration");
        jiraIntegration.setType("jira");
        
        IntegrationDto confluenceIntegration = new IntegrationDto();
        confluenceIntegration.setId("confluence-integration");
        confluenceIntegration.setType("confluence");
        
        when(integrationService.getIntegrationById("jira-integration", "user123", false))
            .thenReturn(jiraIntegration);
        when(integrationService.getIntegrationById("confluence-integration", "user123", false))
            .thenReturn(confluenceIntegration);
        
        try (MockedStatic<MCPSchemaGenerator> mockedSchemaGenerator = Mockito.mockStatic(MCPSchemaGenerator.class)) {
            Map<String, Object> expectedTools = Map.of(
                "tools", Arrays.asList(
                    Map.of("name", "jira-get-ticket", "description", "Get Jira ticket information")
                )
            );
            mockedSchemaGenerator.when(() -> MCPSchemaGenerator.generateToolsListResponse(eq(Set.of("jira", "confluence"))))
                .thenReturn(expectedTools);

            // When
            Map<String, Object> result = mcpConfigurationResolverService.getToolsListAsMap(configId);

            // Then
            assertEquals(expectedTools, result);
            mockedSchemaGenerator.verify(() -> MCPSchemaGenerator.generateToolsListResponse(eq(Set.of("jira", "confluence"))));
        }
    }

    @Test
    void testGetToolsListAsMap_WithConfigResult() throws Exception {
        // Given
        McpConfigurationResolverService.McpConfigurationResult configResult = 
            new McpConfigurationResolverService.McpConfigurationResult(
                testConfiguration, 
                "user123", 
                Arrays.asList("jira-integration"), 
                Set.of("jira")
            );
        
        try (MockedStatic<MCPSchemaGenerator> mockedSchemaGenerator = Mockito.mockStatic(MCPSchemaGenerator.class)) {
            Map<String, Object> expectedTools = Map.of(
                "tools", Arrays.asList(
                    Map.of("name", "jira-get-ticket", "description", "Get Jira ticket information")
                )
            );
            mockedSchemaGenerator.when(() -> MCPSchemaGenerator.generateToolsListResponse(Set.of("jira")))
                .thenReturn(expectedTools);

            // When  
            Map<String, Object> result = mcpConfigurationResolverService.getToolsListAsMap(configResult);

            // Then
            assertEquals(expectedTools, result);
            mockedSchemaGenerator.verify(() -> MCPSchemaGenerator.generateToolsListResponse(Set.of("jira")));
        }
    }

    @Test
    void testExecuteToolCallRaw_WithConfigResult() throws Exception {
        // Given
        String toolName = "jira-get-ticket";
        Map<String, Object> arguments = Map.of("ticketKey", "DMC-100");
        Map<String, Object> resolvedIntegrationsMap = Map.of("jira", Map.of("url", "https://test.atlassian.net"));
        
        // Mock the JSONObject for resolved integrations
        JSONObject resolvedIntegrationsJson = new JSONObject();
        resolvedIntegrationsJson.put("jira", Map.of("url", "https://test.atlassian.net"));
        
        when(integrationResolutionHelper.resolveIntegrationIds(eq(Arrays.asList("jira-integration")), eq("user123")))
            .thenReturn(resolvedIntegrationsJson);
        
        McpConfigurationResolverService.McpConfigurationResult configResult = 
            new McpConfigurationResolverService.McpConfigurationResult(
                testConfiguration, 
                "user123", 
                Arrays.asList("jira-integration"), 
                Set.of("jira")
            );
        
        Object expectedResult = Map.of("key", "DMC-100", "summary", "Test ticket");
        
        try (MockedStatic<MCPToolExecutor> mockedExecutor = Mockito.mockStatic(MCPToolExecutor.class)) {
            mockedExecutor.when(() -> MCPToolExecutor.executeTool(eq(toolName), any(Map.class), any(Map.class)))
                .thenReturn(expectedResult);

            // When
            Object result = mcpConfigurationResolverService.executeToolCallRaw(configResult, toolName, arguments);

            // Then
            assertEquals(expectedResult, result);
            mockedExecutor.verify(() -> MCPToolExecutor.executeTool(eq(toolName), any(Map.class), any(Map.class)));
        }
    }

    @Test
    void testExecuteToolCallRaw_WithConfigId() throws Exception {
        // Given
        String toolName = "jira-get-ticket";
        Map<String, Object> arguments = Map.of("ticketKey", "DMC-100");
        
        when(mcpConfigurationService.findById(configId)).thenReturn(testConfiguration);
        
        // Mock integrations
        IntegrationDto jiraIntegration = new IntegrationDto();
        jiraIntegration.setId("jira-integration");
        jiraIntegration.setType("jira");
        
        IntegrationDto confluenceIntegration = new IntegrationDto();
        confluenceIntegration.setId("confluence-integration");
        confluenceIntegration.setType("confluence");
        
        when(integrationService.getIntegrationById("jira-integration", "user123", false))
            .thenReturn(jiraIntegration);
        when(integrationService.getIntegrationById("confluence-integration", "user123", false))
            .thenReturn(confluenceIntegration);
            
        // Mock the JSONObject for resolved integrations
        JSONObject resolvedIntegrationsJson = new JSONObject();
        resolvedIntegrationsJson.put("jira", Map.of("url", "https://test.atlassian.net"));
        
        when(integrationResolutionHelper.resolveIntegrationIds(
                eq(Arrays.asList("jira-integration", "confluence-integration")), eq("user123")))
            .thenReturn(resolvedIntegrationsJson);
        
        Object expectedResult = Map.of("key", "DMC-100", "summary", "Test ticket");
        
        try (MockedStatic<MCPToolExecutor> mockedExecutor = Mockito.mockStatic(MCPToolExecutor.class)) {
            mockedExecutor.when(() -> MCPToolExecutor.executeTool(eq(toolName), any(Map.class), any(Map.class)))
                .thenReturn(expectedResult);

            // When
            Object result = mcpConfigurationResolverService.executeToolCallRaw(configId, toolName, arguments);

            // Then
            assertEquals(expectedResult, result);
            verify(mcpConfigurationService).findById(configId);
            mockedExecutor.verify(() -> MCPToolExecutor.executeTool(eq(toolName), any(Map.class), any(Map.class)));
        }
    }
}
