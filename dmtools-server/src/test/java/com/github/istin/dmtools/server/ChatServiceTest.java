package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.Message;
import com.github.istin.dmtools.ai.agent.ToolSelectorAgent;
import com.github.istin.dmtools.dto.ChatMessage;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
import com.github.istin.dmtools.server.service.IntegrationResolutionHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void testSimpleChatMessage() throws Exception {
        String message = "Hello";
        String model = "test-model";
        when(ai.chat(model, message)).thenReturn("Hi there!");

        ChatResponse response = chatService.simpleChatMessage(message, model);

        assertNotNull(response);
        assertEquals("Hi there!", response.getContent());
        assertTrue(response.isSuccess());
    }

    @Test
    void testChatWithHistory() throws Exception {
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null),
            new ChatMessage("assistant", "Hi there!", null),
            new ChatMessage("user", "How are you?", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, null);
        when(ai.chat(org.mockito.ArgumentMatchers.eq("test-model"), org.mockito.ArgumentMatchers.any(Message[].class))).thenReturn("I am fine, thank you!");

        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertEquals("I am fine, thank you!", response.getContent());
        assertTrue(response.isSuccess());
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
    void testSimpleChatWithException() throws Exception {
        String message = "Hello";
        String model = "test-model";
        when(ai.chat(model, message)).thenThrow(new RuntimeException("Network error"));

        ChatResponse response = chatService.simpleChatMessage(message, model);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getContent().contains("Failed to process message"));
    }

    @Test
    void testSimpleChatMessageWithAIIntegration() throws Exception {
        String message = "Hello";
        String model = "test-model";
        String userId = "user123";
        String aiIntegrationId = "integration456";
        
        // Mock AI instance resolution
        when(integrationResolutionHelper.resolveSingleIntegrationId(aiIntegrationId, userId))
            .thenReturn(new JSONObject().put("api_key", "test-key"));
        when(ai.chat(model, message)).thenReturn("Hi from integration AI!");

        ChatResponse response = chatService.simpleChatMessage(message, model, aiIntegrationId, userId);

        assertNotNull(response);
        assertEquals("Hi from integration AI!", response.getContent());
        assertEquals(aiIntegrationId, response.getAi());
        assertTrue(response.isSuccess());
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

        // Mock AI instance resolution
        when(integrationResolutionHelper.resolveSingleIntegrationId(aiIntegrationId, userId))
            .thenReturn(new JSONObject().put("api_key", "test-key"));
        when(ai.chat(org.mockito.ArgumentMatchers.eq("test-model"), org.mockito.ArgumentMatchers.any(Message[].class)))
            .thenReturn("Response from integration AI!");

        ChatResponse response = chatService.chat(request, userId);

        assertNotNull(response);
        assertEquals("Response from integration AI!", response.getContent());
        assertEquals(aiIntegrationId, response.getAi());
        assertTrue(response.isSuccess());
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
        when(ai.chat(org.mockito.ArgumentMatchers.eq("test-model"), org.mockito.ArgumentMatchers.any(Message[].class)))
            .thenReturn("Response from default AI!");

        ChatResponse response = chatService.chat(request, userId);

        assertNotNull(response);
        assertEquals("Response from default AI!", response.getContent());
        assertNull(response.getAi()); // Should fall back to default AI
        assertTrue(response.isSuccess());
        verify(integrationResolutionHelper).resolveSingleIntegrationId(invalidAiIntegrationId, userId);
    }

    @Test
    void testChatWithNullAIIntegration() throws Exception {
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "Hello", null)
        );
        ChatRequest request = new ChatRequest(messages, "test-model", null, null);
        String userId = "user123";

        when(ai.chat(org.mockito.ArgumentMatchers.eq("test-model"), org.mockito.ArgumentMatchers.any(Message[].class)))
            .thenReturn("Response from default AI!");

        ChatResponse response = chatService.chat(request, userId);

        assertNotNull(response);
        assertEquals("Response from default AI!", response.getContent());
        assertNull(response.getAi()); // Should use default AI
        assertTrue(response.isSuccess());
        verify(integrationResolutionHelper, never()).resolveSingleIntegrationId(any(), any());
    }

} 