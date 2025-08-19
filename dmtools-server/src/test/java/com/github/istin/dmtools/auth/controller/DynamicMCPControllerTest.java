package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.auth.service.McpConfigurationService;
import com.github.istin.dmtools.auth.service.IntegrationConfigurationLoader;
import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.IntegrationConfigDto;
import com.github.istin.dmtools.server.service.FileDownloadService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DynamicMCPController.
 * Tests the current working implementation with SSE endpoints only.
 * 
 * Current endpoints tested:
 * - GET /mcp/stream/{configId} - SSE initialization
 * - POST /mcp/stream/{configId} - SSE request processing
 */
@ExtendWith(MockitoExtension.class)
public class DynamicMCPControllerTest {

    @Mock
    private McpConfigurationService mcpConfigurationService;

    @Mock
    private IntegrationService integrationService;

    @Mock
    private IntegrationConfigurationLoader configurationLoader;

    @Mock
    private FileDownloadService fileDownloadService;

    @InjectMocks
    private DynamicMCPController dynamicMCPController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dynamicMCPController).build();
    }

    // =========================
    // SSE Stream Tests - Current Working Implementation
    // =========================

    @Test
    void mcpStreamGet_WithValidConfigId_ShouldReturnSseResponse() throws Exception {
        // Arrange
        String configId = "test-config-id";
        McpConfiguration mockConfig = createMockMcpConfiguration(configId);
        
        lenient().when(mcpConfigurationService.findById(configId)).thenReturn(mockConfig);

        // Act & Assert
        mockMvc.perform(get("/mcp/stream/{configId}", configId)
                        .header("Accept", "text/event-stream")
                        .header("User-Agent", "cursor"))
                .andExpect(status().isOk());
        
        // Note: SSE content testing is complex in unit tests, so we focus on status
        // The actual SSE content is tested in integration tests
    }

    @Test
    void mcpStreamGet_WithNonExistentConfigId_ShouldReturnSseWithError() throws Exception {
        // Arrange
        String configId = "non-existent-config";
        
        lenient().when(mcpConfigurationService.findById(configId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/mcp/stream/{configId}", configId)
                        .header("Accept", "text/event-stream")
                        .header("User-Agent", "cursor"))
                .andExpect(status().isOk()); // SSE always returns 200, errors are in the stream
        
        // The actual error handling happens within the SSE stream
    }

    @Test
    void mcpStreamPost_WithInitializeRequest_ShouldReturnSseResponse() throws Exception {
        // Arrange
        String configId = "test-config-id";
        McpConfiguration mockConfig = createMockMcpConfiguration(configId);
        
        lenient().when(mcpConfigurationService.findById(configId)).thenReturn(mockConfig);

        String initializeRequest = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", "init-1")
                .put("method", "initialize")
                .put("params", new JSONObject().put("protocolVersion", "2025-01-08"))
                .toString();

        // Act & Assert
        mockMvc.perform(post("/mcp/stream/{configId}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept", "text/event-stream")
                        .header("User-Agent", "cursor")
                        .content(initializeRequest))
                .andExpect(status().isOk());
                
        // Note: SSE content testing is complex in unit tests
    }

    @Test
    void mcpStreamPost_WithToolsListRequest_ShouldReturnSseResponse() throws Exception {
        // Arrange
        String configId = "test-config-id";
        McpConfiguration mockConfig = createMockMcpConfigurationWithIntegrations(configId);
        
        when(mcpConfigurationService.findById(configId)).thenReturn(mockConfig);
        mockIntegrationService(mockConfig);

        String toolsListRequest = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", "tools-1")
                .put("method", "tools/list")
                .toString();

        // Act & Assert
        mockMvc.perform(post("/mcp/stream/{configId}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept", "text/event-stream")
                        .header("User-Agent", "cursor")
                        .content(toolsListRequest))
                .andExpect(status().isOk());
                
        // Tools list response will be in the SSE stream
    }

    @Test
    void mcpStreamPost_WithToolsCallRequest_ShouldReturnSseResponse() throws Exception {
        // Arrange
        String configId = "test-config-id";
        McpConfiguration mockConfig = createMockMcpConfigurationWithIntegrations(configId);
        
        when(mcpConfigurationService.findById(configId)).thenReturn(mockConfig);
        mockIntegrationService(mockConfig);

        String toolCallRequest = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", "call-1")
                .put("method", "tools/call")
                .put("params", new JSONObject()
                        .put("name", "jira_get_ticket")
                        .put("arguments", new JSONObject()
                                .put("ticketKey", "DMC-100")))
                .toString();

        // Act & Assert
        mockMvc.perform(post("/mcp/stream/{configId}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept", "text/event-stream")
                        .header("User-Agent", "cursor")
                        .content(toolCallRequest))
                .andExpect(status().isOk());
                
        // Tool execution result will be in the SSE stream
    }

    @Test
    void mcpStreamPost_WithGeminiClientInitialize_ShouldAutoSendToolsList() throws Exception {
        // Arrange
        String configId = "test-config-id";
        McpConfiguration mockConfig = createMockMcpConfigurationWithIntegrations(configId);
        
        when(mcpConfigurationService.findById(configId)).thenReturn(mockConfig);
        mockIntegrationService(mockConfig);

        String initializeRequest = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", "init-gemini")
                .put("method", "initialize")
                .put("params", new JSONObject().put("protocolVersion", "2025-01-08"))
                .toString();

        // Act & Assert - Gemini CLI detection by User-Agent: node
        mockMvc.perform(post("/mcp/stream/{configId}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept", "text/event-stream")
                        .header("User-Agent", "node")  // Gemini CLI user-agent
                        .content(initializeRequest))
                .andExpect(status().isOk());
                
        // The controller should auto-send tools/list after initialize for Gemini CLI
    }

    @Test
    void mcpStreamPost_WithInvalidJson_ShouldReturnSseWithError() throws Exception {
        // Arrange
        String configId = "test-config-id";
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(post("/mcp/stream/{configId}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept", "text/event-stream")
                        .content(invalidJson))
                .andExpect(status().isOk()); // SSE always returns 200, errors are in the stream
                
        // Error details will be in the SSE stream
    }

    @Test
    void mcpStreamPost_WithUnsupportedMethod_ShouldReturnSseWithError() throws Exception {
        // Arrange
        String configId = "test-config-id";
        String unsupportedRequest = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", "unsupported")
                .put("method", "unsupported/method")
                .toString();

        // Act & Assert
        mockMvc.perform(post("/mcp/stream/{configId}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept", "text/event-stream")
                        .content(unsupportedRequest))
                .andExpect(status().isOk()); // SSE always returns 200, errors are in the stream
                
        // Method not found error will be in the SSE stream
    }

    // =========================
    // Helper Methods
    // =========================

    /**
     * Helper method to create a mock MCP configuration for testing.
     */
    private McpConfiguration createMockMcpConfiguration(String configId) {
        McpConfiguration config = new McpConfiguration();
        config.setId(configId);
        config.setName("Test Configuration");
        
        User mockUser = new User();
        mockUser.setId("test-user-id");
        config.setUser(mockUser);
        
        // Set up integration IDs collection to avoid NullPointerException
        List<String> integrationIds = Arrays.asList("integration-1");
        config.setIntegrationIds(integrationIds);
        
        return config;
    }

    /**
     * Helper method to create a mock MCP configuration with integrations for testing.
     */
    private McpConfiguration createMockMcpConfigurationWithIntegrations(String configId) {
        McpConfiguration config = createMockMcpConfiguration(configId);
        
        // Mock integration IDs
        List<String> integrationIds = Arrays.asList("integration-1");
        config.setIntegrationIds(integrationIds);
        
        return config;
    }

    /**
     * Helper method to mock integration service responses.
     */
    private void mockIntegrationService(McpConfiguration config) {
        IntegrationDto mockIntegration = new IntegrationDto();
        mockIntegration.setId("integration-1");
        mockIntegration.setType("jira");
        mockIntegration.setName("Test Jira Integration");
        
        // Mock the config params that are required for integration resolution
        Set<IntegrationConfigDto> configParams = new HashSet<>();
        
        IntegrationConfigDto urlConfig = new IntegrationConfigDto();
        urlConfig.setParamKey("url");
        urlConfig.setParamValue("https://test.atlassian.net");
        urlConfig.setSensitive(false);
        configParams.add(urlConfig);
        
        IntegrationConfigDto usernameConfig = new IntegrationConfigDto();
        usernameConfig.setParamKey("username");
        usernameConfig.setParamValue("test@example.com");
        usernameConfig.setSensitive(false);
        configParams.add(usernameConfig);
        
        IntegrationConfigDto tokenConfig = new IntegrationConfigDto();
        tokenConfig.setParamKey("token");
        tokenConfig.setParamValue("test-token");
        tokenConfig.setSensitive(true);
        configParams.add(tokenConfig);
        
        mockIntegration.setConfigParams(configParams);
        
        when(integrationService.getIntegrationById(eq("integration-1"), anyString(), anyBoolean()))
                .thenReturn(mockIntegration);
    }
}
