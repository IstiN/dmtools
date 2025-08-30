package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.Message;


import com.github.istin.dmtools.dto.ChatMessage;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
import com.github.istin.dmtools.dto.IntegrationDto;

import com.github.istin.dmtools.server.service.IntegrationResolutionHelper;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class ChatService {

    private static final Logger logger = LogManager.getLogger(ChatService.class);

    @Autowired
    private IntegrationResolutionHelper integrationResolutionHelper;

    public ChatResponse chat(ChatRequest request) {
        return chat(request, null);
    }

    public ChatResponse chat(ChatRequest request, String userId) {
        try {
            if (request.getMessages() == null || request.getMessages().isEmpty()) {
                logger.warn("Chat request received with no messages.");
                return ChatResponse.error("Failed to process chat request: Message list cannot be empty.");
            }
            logger.info("Processing chat request with {} messages", request.getMessages().size());
            
            // Resolve AI integration if specified
            AIResolutionResult resolutionResult = resolveAIFromRequestWithStatus(request, userId);
            
            // Convert ChatMessage DTOs to AI Message objects
            List<Message> messages = convertToAIMessages(request.getMessages());
            
            // Check if agent tools are enabled
            if (request.getAgentTools() != null && request.getAgentTools().isEnabled()) {
                return chatWithMcpTools(messages, request, resolutionResult.ai, resolutionResult.resolvedIntegrationId);
            } else {
                return chatWithoutTools(messages, request, resolutionResult.ai, resolutionResult.resolvedIntegrationId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            return ChatResponse.error("Failed to process chat request: " + e.getMessage());
        }
    }

    public ChatResponse chatWithFiles(ChatRequest request, List<File> files) {
        return chatWithFiles(request, files, null);
    }

    public ChatResponse chatWithFiles(ChatRequest request, List<File> files, String userId) {
        try {
            logger.info("Processing chat request with {} messages and {} files", 
                       request.getMessages().size(), files != null ? files.size() : 0);
            
            // Resolve AI integration if specified
            AIResolutionResult resolutionResult = resolveAIFromRequestWithStatus(request, userId);
            
            // Convert ChatMessage DTOs to AI Message objects, attaching files to the last user message
            List<Message> messages = convertToAIMessagesWithFiles(request.getMessages(), files);
            
            // Check if agent tools are enabled
            if (request.getAgentTools() != null && request.getAgentTools().isEnabled()) {
                return chatWithMcpTools(messages, request, resolutionResult.ai, resolutionResult.resolvedIntegrationId);
            } else {
                return chatWithoutTools(messages, request, resolutionResult.ai, resolutionResult.resolvedIntegrationId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing chat request with files", e);
            return ChatResponse.error("Failed to process chat request with files: " + e.getMessage());
        }
    }

    public ChatResponse simpleChatMessage(String message, String model) {
        return simpleChatMessage(message, model, null, null);
    }

    public ChatResponse simpleChatMessage(String message, String model, String aiIntegrationId, String userId) {
        try {
            logger.info("Processing simple chat message");
            
            // Resolve AI integration - either specified or automatically detect user's first AI integration
            String resolvedIntegrationId = null;
            AI aiToUse = null;
            
            if (userId == null) {
                logger.error("❌ [ChatService] User ID is required for AI integration selection");
                return ChatResponse.error("User authentication is required for AI integration selection");
            }
            
            try {
                JSONObject integrationConfig;
                String actualIntegrationId;
                
                if (aiIntegrationId != null && !aiIntegrationId.trim().isEmpty()) {
                    // Use specific integration ID provided by user
                    logger.info("🤖 [ChatService] AI integration selection requested for ID: {}", aiIntegrationId);
                    integrationConfig = integrationResolutionHelper.resolveSingleIntegrationId(aiIntegrationId, userId);
                    actualIntegrationId = aiIntegrationId;
                } else {
                    // Automatically use user's first AI integration
                    logger.info("🤖 [ChatService] No AI integration specified, auto-selecting user's first AI integration");
                    integrationConfig = integrationResolutionHelper.resolveUserFirstAIIntegration(userId);
                    
                    // Extract the actual integration ID from the resolved config
                    IntegrationDto firstAI = integrationResolutionHelper.findUserFirstAIIntegration(userId);
                    actualIntegrationId = firstAI != null ? firstAI.getId() : null;
                }
                
                // Create the specific AI instance using ServerManagedIntegrationsModule directly
                ServerManagedIntegrationsModule integrationsModule = new ServerManagedIntegrationsModule(integrationConfig);
                AI integrationAI = integrationsModule.createAI();
                if (integrationAI != null) {
                    logger.info("✅ [ChatService] Successfully created AI instance from integration: {}", actualIntegrationId);
                    aiToUse = integrationAI;
                    resolvedIntegrationId = actualIntegrationId; // Mark as successfully resolved
                } else {
                    logger.error("❌ [ChatService] Failed to create AI instance from integration: {}", actualIntegrationId);
                    return ChatResponse.error("Failed to create AI instance from integration. Please check your integration configuration.");
                }
                
            } catch (Exception e) {
                logger.error("❌ [ChatService] Failed to resolve AI integration: {}", e.getMessage());
                return ChatResponse.error("Failed to resolve AI integration: " + e.getMessage());
            }
            
            String response;
            if (model != null && !model.trim().isEmpty()) {
                response = aiToUse.chat(model, message);
            } else {
                response = aiToUse.chat(message);
            }
            
            logger.info("Successfully processed simple chat message");
            return ChatResponse.success(response, model, resolvedIntegrationId);
            
        } catch (Exception e) {
            logger.error("Error processing simple chat message", e);
            return ChatResponse.error("Failed to process message: " + e.getMessage());
        }
    }

    private ChatResponse chatWithoutTools(List<Message> messages, ChatRequest request, AI aiToUse, String resolvedIntegrationId) throws Exception {
        // Use the AI service to get response without tools
        String response;
        if (request.getModel() != null && !request.getModel().trim().isEmpty()) {
            response = aiToUse.chat(request.getModel(), messages.toArray(new Message[0]));
        } else {
            response = aiToUse.chat(messages.toArray(new Message[0]));
        }
        
        logger.info("Successfully processed chat request without tools");
        return ChatResponse.success(response, request.getModel(), resolvedIntegrationId);
    }

    private ChatResponse chatWithMcpTools(List<Message> messages, ChatRequest request, AI aiToUse, String resolvedIntegrationId) {
        try {
            logger.info("Processing chat request with MCP tools enabled");
            
            // TODO: Uncomment this logic after the MCP generated classes are available
            // Map<String, Object> toolsResult = getToolsListSafely();
            // @SuppressWarnings("unchecked")
            // List<Map<String, Object>> availableTools = (List<Map<String, Object>>) toolsResult.get("tools");
            
            // if (availableTools == null || availableTools.isEmpty()) {
            //     logger.warn("No MCP tools available, falling back to regular chat");
            //     return chatWithoutTools(messages, request, aiToUse, resolvedIntegrationId);
            // }
            
            // List<Map<String, Object>> filteredTools = filterToolsBasedOnConfig(availableTools, request.getAgentTools());
            
            // String lastUserMessage = getLastUserMessage(messages);
            // String formattedTools = formatToolsToString(filteredTools);
            // ToolSelectorAgent.Params params = new ToolSelectorAgent.Params(lastUserMessage, formattedTools);
            // List<ToolCallRequest> toolCalls = toolSelectorAgent.run(params);

            // if (toolCalls != null && !toolCalls.isEmpty()) {
            //     return executeToolCallsAndRespond(toolCalls, messages, request, aiToUse);
            // } else {
            //     return chatWithoutTools(messages, request, aiToUse, resolvedIntegrationId);
            // }
            return chatWithoutTools(messages, request, aiToUse, resolvedIntegrationId); // Placeholder
        } catch (Exception e) {
            logger.error("Error in chat with MCP tools", e);
            // Fallback to regular chat if tool integration fails
            logger.warn("Falling back to regular chat without tools");
            try {
                return chatWithoutTools(messages, request, aiToUse, resolvedIntegrationId);
            } catch (Exception fallbackError) {
                logger.error("Error in fallback chat", fallbackError);
                return ChatResponse.error("Failed to process chat request: " + e.getMessage());
            }
        }
    }



    private List<Message> convertToAIMessages(List<ChatMessage> chatMessages) {
        List<Message> messages = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            messages.add(new Message(chatMessage.getRole(), chatMessage.getContent(), null));
        }
        return messages;
    }

    private List<Message> convertToAIMessagesWithFiles(List<ChatMessage> chatMessages, List<File> files) {
        List<Message> messages = new ArrayList<>();
        
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage chatMessage = chatMessages.get(i);
            
            // Attach files to the last user message in the conversation
            boolean isLastUserMessage = isLastUserMessage(chatMessages, i);
            List<File> messageFiles = (isLastUserMessage && files != null) ? files : null;
            
            messages.add(new Message(chatMessage.getRole(), chatMessage.getContent(), messageFiles));
        }
        
        return messages;
    }

    private boolean isLastUserMessage(List<ChatMessage> messages, int currentIndex) {
        if (!messages.get(currentIndex).getRole().equals("user")) {
            return false;
        }
        
        // Check if there are any user messages after this one
        for (int i = currentIndex + 1; i < messages.size(); i++) {
            if (messages.get(i).getRole().equals("user")) {
                return false;
            }
        }
        
        return true;
    }


    /**
     * Inner class to hold AI resolution result
     */
    private static class AIResolutionResult {
        final AI ai;
        final String resolvedIntegrationId; // null if default AI was used
        
        AIResolutionResult(AI ai, String resolvedIntegrationId) {
            this.ai = ai;
            this.resolvedIntegrationId = resolvedIntegrationId;
        }
    }



    /**
     * Resolves AI instance from ChatRequest, returning both AI and resolution status.
     */
    private AIResolutionResult resolveAIFromRequestWithStatus(ChatRequest request, String userId) {
        if (userId == null) {
            logger.error("❌ [ChatService] User ID is required for AI integration selection");
            throw new RuntimeException("User authentication is required for AI integration selection");
        }
        
        try {
            JSONObject integrationConfig;
            String actualIntegrationId;
            
            if (request.getAi() != null && !request.getAi().trim().isEmpty()) {
                // Use specific integration ID provided by user
                logger.info("🤖 [ChatService] AI integration selection requested for ID: {}", request.getAi());
                integrationConfig = integrationResolutionHelper.resolveSingleIntegrationId(request.getAi(), userId);
                actualIntegrationId = request.getAi();
            } else {
                // Automatically use user's first AI integration
                logger.info("🤖 [ChatService] No AI integration specified, auto-selecting user's first AI integration");
                integrationConfig = integrationResolutionHelper.resolveUserFirstAIIntegration(userId);
                
                // Extract the actual integration ID from the resolved config
                IntegrationDto firstAI = integrationResolutionHelper.findUserFirstAIIntegration(userId);
                actualIntegrationId = firstAI != null ? firstAI.getId() : null;
            }
            
            // Create the specific AI instance using ServerManagedIntegrationsModule directly
            ServerManagedIntegrationsModule integrationsModule = new ServerManagedIntegrationsModule(integrationConfig);
            AI integrationAI = integrationsModule.createAI();
            if (integrationAI != null) {
                logger.info("✅ [ChatService] Successfully created AI instance from integration: {}", actualIntegrationId);
                return new AIResolutionResult(integrationAI, actualIntegrationId); // Successfully resolved
            } else {
                logger.error("❌ [ChatService] Failed to create AI instance from integration: {}", actualIntegrationId);
                throw new RuntimeException("Failed to create AI instance from integration. Please check your integration configuration.");
            }
            
        } catch (Exception e) {
            logger.error("❌ [ChatService] Failed to resolve AI integration: {}", e.getMessage());
            throw new RuntimeException("Failed to resolve AI integration: " + e.getMessage());
        }
    }


} 