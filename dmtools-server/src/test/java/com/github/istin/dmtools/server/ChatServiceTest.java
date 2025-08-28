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
import com.github.istin.dmtools.server.service.IntegrationResolutionHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AI ai;

    @Mock
    private ToolSelectorAgent toolSelectorAgent;

    @Mock
    private IntegrationResolutionHelper integrationResolutionHelper;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService();
        // Use reflection to inject mocks instead of @InjectMocks to avoid constructor issues
        ReflectionTestUtils.setField(chatService, "ai", ai);
        ReflectionTestUtils.setField(chatService, "toolSelectorAgent", toolSelectorAgent);
        ReflectionTestUtils.setField(chatService, "integrationResolutionHelper", integrationResolutionHelper);
    }

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

        ChatResponse response = chatService.simpleChatMessage(message, model);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("User authentication is required for AI integration selection", response.getContent());
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
        assertEquals(firstAIIntegrationId, response.getAi());
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
        assertEquals(firstAIIntegrationId, response.getAi());
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

        ChatResponse response = chatService.simpleChatMessage(message, model);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("User authentication is required for AI integration selection", response.getContent());
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
        assertEquals(aiIntegrationId, response.getAi());
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
        assertEquals(aiIntegrationId, response.getAi());
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
        assertEquals(firstAIIntegrationId, response.getAi());
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

} 