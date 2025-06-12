package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.Message;
import com.github.istin.dmtools.ai.agent.ToolSelectorAgent;
import com.github.istin.dmtools.dto.ChatMessage;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AI ai;

    @Mock
    private ToolSelectorAgent toolSelectorAgent;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService();
        // Use reflection to inject mocks instead of @InjectMocks to avoid constructor issues
        ReflectionTestUtils.setField(chatService, "ai", ai);
        ReflectionTestUtils.setField(chatService, "toolSelectorAgent", toolSelectorAgent);
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
        ChatRequest request = new ChatRequest(messages, "test-model", null);
        when(ai.chat(org.mockito.ArgumentMatchers.eq("test-model"), org.mockito.ArgumentMatchers.any(Message[].class))).thenReturn("I am fine, thank you!");

        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertEquals("I am fine, thank you!", response.getContent());
        assertTrue(response.isSuccess());
    }

    @Test
    void testChatWithEmptyHistory() {
        ChatRequest request = new ChatRequest(Collections.emptyList(), "test-model", null);

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

} 