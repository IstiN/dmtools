package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.Message;
import com.github.istin.dmtools.ai.agent.ToolSelectorAgent;
import com.github.istin.dmtools.ai.js.JSAIClient;
import com.github.istin.dmtools.dto.ChatMessage;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.ToolCallRequest;
import com.github.istin.dmtools.server.service.IntegrationResolutionHelper;
import com.github.istin.dmtools.server.service.McpConfigurationResolverService;
import org.json.JSONObject;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private IntegrationResolutionHelper integrationResolutionHelper;

    @Mock
    private McpConfigurationResolverService mcpConfigurationResolverService;
    
    @Mock
    private ToolSelectorAgent toolSelectorAgent;

    @InjectMocks
    private ChatService chatService;

    /**
     * Helper method to create mock IntegrationDto objects for testing
     */
    private IntegrationDto createMockIntegrationDto(String id, String type) {
        IntegrationDto dto = new IntegrationDto();
        dto.setId(id);
        dto.setType(type);
        dto.setName("Test " + type + " Integration");
        dto.setEnabled(true);
        dto.setCreatedAt(LocalDateTime.now().minusDays(1));
        return dto;
    }

    @Test
    void testSimpleChatMessageWithoutUserId() throws Exception {
        String message = "Hello";
        String model = "test-model";

        ChatResponse response = chatService.simpleChatMessage(message, model, null, null);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getContent().contains("User authentication is required"));
    }

    @Test
    void testSimpleChatMessageWithAutoSelection() throws Exception {
        String message = "Hello";
        String model = "test-model";
        String userId = "user123";
        String firstAIIntegrationId = "auto-selected-integration";
        
        // Mock auto-selection of user's first AI integration
        IntegrationDto firstAI = createMockIntegrationDto(firstAIIntegrationId, "gemini");
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key")
                .put("GEMINI_DEFAULT_MODEL", "gemini-1.5-flash")
                .put("GEMINI_BASE_PATH", "https://generativelanguage.googleapis.com"));
        
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenReturn(integrationConfig);
        when(integrationResolutionHelper.findUserFirstAIIntegration(userId))
            .thenReturn(firstAI);

        ChatResponse response = chatService.simpleChatMessage(message, model, null, userId);

        assertNotNull(response);
        // AI instance is created successfully, but API call fails with test key - this is expected
        assertTrue(response.isSuccess());
        // Note: ChatResponse no longer contains AI integration ID, just success status
    }

    @Test
    void testChatWithHistoryWithoutUserId() throws Exception {
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null),
            new ChatMessage("assistant", "Hi there!", null),
            new ChatMessage("user", "How are you?", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, null);

        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Failed to process chat request: User authentication is required for AI integration selection", response.getContent());
    }

    @Test
    void testChatWithHistoryAndAutoSelection() throws Exception {
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null),
            new ChatMessage("assistant", "Hi there!", null),
            new ChatMessage("user", "How are you?", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, null);
        String userId = "user123";
        String firstAIIntegrationId = "auto-selected-integration";
        
        // Mock auto-selection of user's first AI integration
        IntegrationDto firstAI = createMockIntegrationDto(firstAIIntegrationId, "gemini");
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key")
                .put("GEMINI_DEFAULT_MODEL", "gemini-1.5-flash")
                .put("GEMINI_BASE_PATH", "https://generativelanguage.googleapis.com"));
        
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenReturn(integrationConfig);
        when(integrationResolutionHelper.findUserFirstAIIntegration(userId))
            .thenReturn(firstAI);

        ChatResponse response = chatService.chat(request, userId);

        assertNotNull(response);
        // AI instance is created successfully, but API call fails with test key - this is expected  
        assertTrue(response.isSuccess());
        // Note: ChatResponse no longer contains AI integration ID, just success status
    }

    @Test
    void testChatWithEmptyHistory() {
        ChatRequest request = new ChatRequest(Collections.emptyList(), "test-model", null, null);

        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getContent().contains("Failed to process chat request"));
    }

    @Test
    void testSimpleChatWithExceptionWithoutUserId() throws Exception {
        String message = "Hello";
        String model = "test-model";

        ChatResponse response = chatService.simpleChatMessage(message, model, null, null);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getContent().contains("User authentication is required"));
    }

    @Test
    void testSimpleChatMessageWithAIIntegration() throws Exception {
        String message = "Hello";
        String model = "test-model";
        String userId = "user123";
        String aiIntegrationId = "integration456";
        
        // Mock AI instance resolution with correct nested structure
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key")
                .put("GEMINI_DEFAULT_MODEL", "gemini-1.5-flash")
                .put("GEMINI_BASE_PATH", "https://generativelanguage.googleapis.com"));
        
        when(integrationResolutionHelper.resolveSingleIntegrationId(aiIntegrationId, userId))
            .thenReturn(integrationConfig);

        ChatResponse response = chatService.simpleChatMessage(message, model, aiIntegrationId, userId);

        assertNotNull(response);
        // AI instance is created successfully, but API call fails with test key - this is expected
        assertTrue(response.isSuccess());
        // Note: ChatResponse no longer contains AI integration ID, just success status
        verify(integrationResolutionHelper).resolveSingleIntegrationId(aiIntegrationId, userId);
    }

    @Test
    void testChatWithValidAIIntegration() throws Exception {
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null)
        );
        String aiIntegrationId = "integration456";
        ChatRequest request = new ChatRequest(messages, "test-model", aiIntegrationId, null);
        String userId = "user123";

        // Mock AI instance resolution with correct nested structure
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key")
                .put("GEMINI_DEFAULT_MODEL", "gemini-1.5-flash")
                .put("GEMINI_BASE_PATH", "https://generativelanguage.googleapis.com"));
        
        when(integrationResolutionHelper.resolveSingleIntegrationId(aiIntegrationId, userId))
            .thenReturn(integrationConfig);

        ChatResponse response = chatService.chat(request, userId);

        assertNotNull(response);
        // AI instance is created successfully, but API call fails with test key - this is expected
        assertTrue(response.isSuccess());
        // Note: ChatResponse no longer contains AI integration ID, just success status
        verify(integrationResolutionHelper).resolveSingleIntegrationId(aiIntegrationId, userId);
    }

    @Test
    void testChatWithInvalidAIIntegration() throws Exception {
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null)
        );
        String invalidAiIntegrationId = "invalid-integration";
        ChatRequest request = new ChatRequest(messages, "test-model", invalidAiIntegrationId, null);
        String userId = "user123";

        // Mock AI instance resolution failure
        when(integrationResolutionHelper.resolveSingleIntegrationId(invalidAiIntegrationId, userId))
            .thenThrow(new RuntimeException("Integration not found"));

        ChatResponse response = chatService.chat(request, userId);

        assertNotNull(response);
        assertFalse(response.isSuccess()); // Should return error, not fallback
        assertTrue(response.getContent().contains("Failed to process chat request: Failed to resolve AI integration: Integration not found"));
        verify(integrationResolutionHelper).resolveSingleIntegrationId(invalidAiIntegrationId, userId);
    }

    @Test
    void testChatWithNullAIIntegrationAutoSelection() throws Exception {
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, null);
        String userId = "user123";
        String firstAIIntegrationId = "auto-selected-integration";
        
        // Mock auto-selection of user's first AI integration
        IntegrationDto firstAI = createMockIntegrationDto(firstAIIntegrationId, "gemini");
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key")
                .put("GEMINI_DEFAULT_MODEL", "gemini-1.5-flash")
                .put("GEMINI_BASE_PATH", "https://generativelanguage.googleapis.com"));
        
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenReturn(integrationConfig);
        when(integrationResolutionHelper.findUserFirstAIIntegration(userId))
            .thenReturn(firstAI);

        ChatResponse response = chatService.chat(request, userId);

        assertNotNull(response);
        // AI instance is created successfully, but API call fails with test key - this is expected
        assertTrue(response.isSuccess());
        // Note: ChatResponse no longer contains AI integration ID, just success status
        verify(integrationResolutionHelper).resolveUserFirstAIIntegration(userId);
        verify(integrationResolutionHelper, never()).resolveSingleIntegrationId(any(), any());
    }

    @Test 
    void testChatWithNullAIIntegrationNoUserIntegrations() throws Exception {
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, null);
        String userId = "user123";
        
        // Mock case where user has no AI integrations
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenThrow(new RuntimeException("No AI integrations found for user. Please create an AI integration (Gemini, Claude, OpenAI, etc.) first."));

        ChatResponse response = chatService.chat(request, userId);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getContent().contains("No AI integrations found for user"));
        verify(integrationResolutionHelper).resolveUserFirstAIIntegration(userId);
    }

    @Test
    void testChatWithMcpTools_SuccessfulExecution() throws Exception {
        // Given
        String userId = "user123";
        String mcpConfigId = "mcp-config-123";
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Get ticket information", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, mcpConfigId);
        
        // Mock AI integration resolution
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key")
                .put("GEMINI_DEFAULT_MODEL", "gemini-1.5-flash")
                .put("GEMINI_BASE_PATH", "https://generativelanguage.googleapis.com"));
        
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenReturn(integrationConfig);

        // Mock MCP configuration resolution
        McpConfigurationResolverService.McpConfigurationResult mcpConfigResult = mock(McpConfigurationResolverService.McpConfigurationResult.class);
        when(mcpConfigurationResolverService.resolveMcpConfiguration(mcpConfigId))
            .thenReturn(mcpConfigResult);
        
        // Mock tools list
        Map<String, Object> toolsResult = Map.of(
            "tools", Arrays.asList(
                Map.of("name", "jira-get-ticket", "description", "Get Jira ticket information")
            )
        );
        when(mcpConfigurationResolverService.getToolsListAsMap(mcpConfigResult))
            .thenReturn(toolsResult);

        // Mock ToolSelectorAgent response - empty tools list
        when(toolSelectorAgent.run(any()))
            .thenReturn(Arrays.asList()); // No tools selected

        // When
        ChatResponse response = chatService.chat(request, userId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        
        verify(mcpConfigurationResolverService).resolveMcpConfiguration(mcpConfigId);
        verify(mcpConfigurationResolverService).getToolsListAsMap(mcpConfigResult);
    }

    @Test
    void testChatWithMcpTools_ToolExecution() throws Exception {
        // Given
        String userId = "user123";
        String mcpConfigId = "mcp-config-123";
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Get ticket DMC-100", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, mcpConfigId);
        
        // Mock AI integration resolution
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key")
                .put("GEMINI_DEFAULT_MODEL", "gemini-1.5-flash")
                .put("GEMINI_BASE_PATH", "https://generativelanguage.googleapis.com"));
        
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenReturn(integrationConfig);

        // Mock MCP configuration resolution
        McpConfigurationResolverService.McpConfigurationResult mcpConfigResult = mock(McpConfigurationResolverService.McpConfigurationResult.class);
        when(mcpConfigurationResolverService.resolveMcpConfiguration(mcpConfigId))
            .thenReturn(mcpConfigResult);
        
        // Mock tools list
        Map<String, Object> toolsResult = Map.of(
            "tools", Arrays.asList(
                Map.of("name", "jira-get-ticket", "description", "Get Jira ticket information")
            )
        );
        when(mcpConfigurationResolverService.getToolsListAsMap(mcpConfigResult))
            .thenReturn(toolsResult);

        // Mock ToolSelectorAgent response - one tool selected, then none
        ToolCallRequest toolCall = new ToolCallRequest("jira-get-ticket", 
            "Get ticket information", Map.of("ticketKey", "DMC-100"));
        when(toolSelectorAgent.run(any()))
            .thenReturn(Arrays.asList(toolCall))  // First iteration: select tool
            .thenReturn(Arrays.asList());         // Second iteration: no more tools

        // Mock tool execution
        Object toolResult = Map.of("key", "DMC-100", "summary", "Test ticket");
        when(mcpConfigurationResolverService.executeToolCallRaw(eq(mcpConfigResult), eq("jira-get-ticket"), 
            anyMap()))
            .thenReturn(toolResult);

        // When
        ChatResponse response = chatService.chat(request, userId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        
        verify(mcpConfigurationResolverService).executeToolCallRaw(eq(mcpConfigResult), eq("jira-get-ticket"), 
            anyMap());
        verify(toolSelectorAgent, times(2)).run(any()); // Called twice due to iteration
    }

    @Test
    void testChatWithMcpTools_ConfigurationResolutionFailure() throws Exception {
        // Given
        String userId = "user123";
        String mcpConfigId = "invalid-config";
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Get ticket information", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, mcpConfigId);
        
        // Mock AI integration resolution
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key"));
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenReturn(integrationConfig);

        // Mock MCP configuration resolution failure
        when(mcpConfigurationResolverService.resolveMcpConfiguration(mcpConfigId))
            .thenThrow(new RuntimeException("MCP configuration not found"));

        // When
        ChatResponse response = chatService.chat(request, userId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess()); // Falls back to regular chat without tools
        
        verify(mcpConfigurationResolverService).resolveMcpConfiguration(mcpConfigId);
        verify(mcpConfigurationResolverService, never()).getToolsListAsMap(any(String.class));
    }

    @Test
    void testChatWithMcpTools_EmptyToolsList() throws Exception {
        // Given
        String userId = "user123";
        String mcpConfigId = "mcp-config-123";
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, mcpConfigId);
        
        // Mock AI integration resolution
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key"));
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenReturn(integrationConfig);

        // Mock MCP configuration resolution
        McpConfigurationResolverService.McpConfigurationResult mcpConfigResult = mock(McpConfigurationResolverService.McpConfigurationResult.class);
        when(mcpConfigurationResolverService.resolveMcpConfiguration(mcpConfigId))
            .thenReturn(mcpConfigResult);
        
        // Mock empty tools list
        when(mcpConfigurationResolverService.getToolsListAsMap(mcpConfigResult))
            .thenReturn(Map.of("tools", Arrays.asList()));

        // When
        ChatResponse response = chatService.chat(request, userId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess()); // Falls back to regular chat
        
        verify(mcpConfigurationResolverService).resolveMcpConfiguration(mcpConfigId);
        verify(mcpConfigurationResolverService).getToolsListAsMap(mcpConfigResult);
        verify(toolSelectorAgent, never()).run(any());
    }

    @Test
    void testProcessChatRequest_WithoutMcpConfig() throws Exception {
        // Given
        String userId = "user123";
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, null); // no mcpConfigId
        
        // Mock AI integration resolution
        JSONObject integrationConfig = new JSONObject()
            .put("gemini", new JSONObject()
                .put("GEMINI_API_KEY", "test-key"));
        when(integrationResolutionHelper.resolveUserFirstAIIntegration(userId))
            .thenReturn(integrationConfig);

        // When
        ChatResponse response = chatService.chat(request, userId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        
        // Verify MCP services are not called when no mcpConfigId
        verify(mcpConfigurationResolverService, never()).resolveMcpConfiguration(any(String.class));
        verify(mcpConfigurationResolverService, never()).getToolsListAsMap(any(String.class));
        verify(toolSelectorAgent, never()).run(any());
    }

} 