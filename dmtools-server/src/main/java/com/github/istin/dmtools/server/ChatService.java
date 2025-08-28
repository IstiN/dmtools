package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.Message;
import com.github.istin.dmtools.ai.agent.ToolSelectorAgent;
import com.github.istin.dmtools.ai.dial.DialAIClient;
import com.github.istin.dmtools.ai.js.JSAIClient;
import com.github.istin.dmtools.auth.controller.DynamicMCPController;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.dto.ChatMessage;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.ToolCallRequest;
import com.github.istin.dmtools.server.service.IntegrationResolutionHelper;
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
    private AI ai;

    @Autowired
    private DynamicMCPController mcpController;

    @Autowired
    private ToolSelectorAgent toolSelectorAgent;

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
                
                // Create the specific AI instance
                AI integrationAI = createAIFromIntegrationConfig(integrationConfig, actualIntegrationId);
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

    private Map<String, Object> getToolsListSafely() {
        try {
            return new HashMap<>();//mcpController.handleToolsList(null); // Passing null for userId, adjust if needed
        } catch (Exception e) {
            logger.error("Error getting tools list: {}", e.getMessage(), e);
            return Map.of("tools", new ArrayList<>());
        }
    }

    private Map<String, Object> callMcpToolSafely(String toolName, Map<String, Object> arguments) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("name", toolName);
            params.put("arguments", arguments);
            return new HashMap<>();//mcpController.handleToolCall(params, null); // Passing null for userId, adjust if needed
        } catch (Exception e) {
            logger.error("Error calling MCP tool {}: {}", toolName, e.getMessage(), e);
            return Map.of("content", List.of(Map.of("text", "Error executing tool " + toolName + ": " + e.getMessage())));
        }
    }

    private List<Map<String, Object>> filterToolsBasedOnConfig(List<Map<String, Object>> allTools, ChatRequest.AgentToolsConfig config) {
        if (config.getAvailableAgents() == null && config.getAvailableOrchestrators() == null) {
            // Return all tools if no specific filtering requested
            return allTools;
        }
        
        List<Map<String, Object>> filteredTools = new ArrayList<>();
        
        for (Map<String, Object> tool : allTools) {
            String toolName = (String) tool.get("name");
            
            // Check if tool matches available agents
            if (config.getAvailableAgents() != null) {
                for (String agentName : config.getAvailableAgents()) {
                    if (toolName.contains(agentName.toLowerCase()) || toolName.contains("agent")) {
                        filteredTools.add(tool);
                        break;
                    }
                }
            }
            
            // Check if tool matches available orchestrators
            if (config.getAvailableOrchestrators() != null) {
                for (String orchestratorName : config.getAvailableOrchestrators()) {
                    if (toolName.contains(orchestratorName.toLowerCase()) || toolName.contains("orchestrator")) {
                        filteredTools.add(tool);
                        break;
                    }
                }
            }
            
            // Include basic tools if no specific filtering
            if (toolName.startsWith("dmtools_jira_") || toolName.startsWith("dmtools_github_") || toolName.startsWith("dmtools_confluence_")) {
                filteredTools.add(tool);
            }
        }
        
        return filteredTools;
    }

    private String getLastUserMessage(List<Message> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).getRole())) {
                return messages.get(i).getText();
            }
        }
        return "";
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

    private ChatResponse executeToolCallsAndRespond(List<ToolCallRequest> toolCalls, List<Message> messages, ChatRequest request) {
        try {
            List<Message> toolCallResponses = new ArrayList<>();
            for (ToolCallRequest toolCall : toolCalls) {
                logger.info("Executing tool: {}", toolCall.getToolName());
                Map<String, Object> arguments = toolCall.getArguments();
                Map<String, Object> toolResult = callMcpToolSafely(toolCall.getToolName(), arguments);
                String formattedResult = formatToolResult(toolCall.getToolName(), toolResult);
                
                // Create a message with the tool result
                Message resultMessage = new Message("assistant", formattedResult, null);
                toolCallResponses.add(resultMessage);
            }
            
            // Add tool responses to the conversation
            messages.addAll(toolCallResponses);
            
            // Continue the conversation with the tool results
            // Note: This code is currently placeholder and would need aiToUse parameter when MCP tools are implemented
            throw new UnsupportedOperationException("MCP tools execution not yet implemented");
            
        } catch (Exception e) {
            logger.error("Error executing tool calls", e);
            return ChatResponse.error("Failed to execute tool calls: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String formatToolResult(String toolName, Map<String, Object> result) {
        StringBuilder formatted = new StringBuilder();
        if (true) {
            return result.toString();
        }
        if (toolName.contains("jira_get_ticket")) {
            // Format JIRA ticket results
            Object ticket = result.get("content");
            if (ticket instanceof Map) {
                Map<String, Object> ticketData = (Map<String, Object>) ticket;
                formatted.append("**JIRA Ticket Information:**\n");
                formatted.append("- **Key:** ").append(ticketData.get("key")).append("\n");
                formatted.append("- **Summary:** ").append(ticketData.get("summary")).append("\n");
                formatted.append("- **Status:** ").append(ticketData.get("status")).append("\n");
                Object assignee = ticketData.get("assignee");
                if (assignee != null) {
                    formatted.append("- **Assignee:** ").append(assignee).append("\n");
                }
                Object priority = ticketData.get("priority");
                if (priority != null) {
                    formatted.append("- **Priority:** ").append(priority).append("\n");
                }
                formatted.append("- **Description:** ").append(ticketData.get("description")).append("\n");
            }
        } else if (toolName.contains("jira_search")) {
            // Format JIRA search results
            Object issues = result.get("issues");
            if (issues instanceof List) {
                List<Map<String, Object>> issueList = (List<Map<String, Object>>) issues;
                formatted.append("**JIRA Search Results:**\n");
                if (issueList.isEmpty()) {
                    formatted.append("No tickets found matching the search criteria.\n");
                } else {
                    for (Map<String, Object> issue : issueList) {
                        formatted.append("- **").append(issue.get("key")).append(":** ")
                                 .append(issue.get("summary"));
                        Object status = issue.get("status");
                        if (status != null) {
                            formatted.append(" (").append(status).append(")");
                        }
                        formatted.append("\n");
                    }
                }
            }
        } else if (toolName.contains("github_get_pull_requests")) {
            // Format GitHub PR results
            Object pullRequests = result.get("pullRequests");
            if (pullRequests instanceof List) {
                List<Map<String, Object>> prs = (List<Map<String, Object>>) pullRequests;
                formatted.append("**GitHub Pull Requests:**\n");
                for (Map<String, Object> pr : prs) {
                    formatted.append("- **#").append(pr.get("number")).append("** ")
                             .append(pr.get("title")).append(" (").append(pr.get("state")).append(")\n");
                }
            }
        } else if (toolName.contains("confluence_search")) {
            // Format Confluence search results
            Object pages = result.get("results");
            if (pages instanceof List) {
                List<Map<String, Object>> pageList = (List<Map<String, Object>>) pages;
                formatted.append("**Confluence Search Results:**\n");
                for (Map<String, Object> page : pageList) {
                    formatted.append("- **").append(page.get("title")).append("**\n");
                    Object excerpt = page.get("excerpt");
                    if (excerpt != null) {
                        formatted.append("  ").append(excerpt).append("\n");
                    }
                }
            }
        } else {
            // Generic formatting for other tools
            formatted.append("**Tool Result (").append(toolName).append("):**\n");
            
            // Try to extract meaningful content from the result
            Object content = result.get("content");
            if (content instanceof List) {
                List<?> contentList = (List<?>) content;
                for (Object item : contentList) {
                    if (item instanceof Map) {
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        Object text = itemMap.get("text");
                        if (text != null) {
                            formatted.append(text.toString()).append("\n");
                        }
                    } else {
                        formatted.append(item.toString()).append("\n");
                    }
                }
            } else if (content != null) {
                formatted.append(content.toString());
            } else {
                formatted.append(result.toString());
            }
        }
        
        return formatted.toString();
    }

    private String formatToolsToString(List<Map<String, Object>> tools) {
        if (tools == null || tools.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> tool : tools) {
            String name = (String) tool.get("name");
            String description = (String) tool.get("description");
            sb.append("- Name: ").append(name).append("\n");
            sb.append("  Description: ").append(description).append("\n");
        }
        return sb.toString();
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
     * Resolves AI instance from ChatRequest.
     */
    private AI resolveAIFromRequest(ChatRequest request, String userId) {
        return resolveAIFromIntegrationId(request.getAi(), userId);
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
            
            // Create the specific AI instance
            AI integrationAI = createAIFromIntegrationConfig(integrationConfig, actualIntegrationId);
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

    /**
     * Creates an AI instance from resolved integration configuration by recreating the AI creation logic.
     * This replicates the logic from ServerManagedIntegrationsModule.provideAI() for direct use.
     */
    private AI createAIFromIntegrationConfig(JSONObject integrationConfig, String integrationId) {
        try {
            logger.info("🔧 [ChatService] Creating AI instance from integration config for ID: {}", integrationId);
            
            if (integrationConfig == null || integrationConfig.isEmpty()) {
                logger.warn("⚠️ [ChatService] Empty integration configuration provided");
                return null;
            }
            
            logger.info("🔍 [ChatService] Available integration types in config: {}", integrationConfig.keySet());
            
            // Create a ConversationObserver for the AI instance
            ConversationObserver observer = new ConversationObserver();
            
            // The IntegrationResolutionHelper returns nested structure: { "gemini": { "GEMINI_API_KEY": "...", ... } }
            // We need to iterate through the available integration types and create the AI instance
            for (String integrationType : integrationConfig.keySet()) {
                logger.info("🔍 [ChatService] Processing integration type: {}", integrationType);
                
                JSONObject typeConfig = integrationConfig.getJSONObject(integrationType);
                logger.info("🔧 [ChatService] Configuration for type '{}': {} parameters", integrationType, typeConfig.length());
                
                switch (integrationType.toLowerCase()) {
                    case "gemini":
                        AI geminiAI = createGeminiAI(typeConfig, observer);
                        if (geminiAI != null) {
                            logger.info("✅ [ChatService] Successfully created Gemini AI instance");
                            return geminiAI;
                        }
                        break;
                    case "dial":
                        AI dialAI = createDialAI(typeConfig, observer);
                        if (dialAI != null) {
                            logger.info("✅ [ChatService] Successfully created Dial AI instance");
                            return dialAI;
                        }
                        break;
                    default:
                        logger.warn("⚠️ [ChatService] Unsupported integration type: {}", integrationType);
                        break;
                }
            }
            
            logger.warn("⚠️ [ChatService] No suitable AI integration found in configuration");
            return null;
            
        } catch (Exception e) {
            logger.error("❌ [ChatService] Failed to create AI instance from integration config: {}", e.getMessage(), e);
            return null;
        }
    }
    

    
    /**
     * Creates a Gemini AI instance from configuration.
     * Handles both real application format (uppercase) and test format (lowercase).
     */
    private AI createGeminiAI(JSONObject config, ConversationObserver observer) {
        try {
            // Handle both uppercase and lowercase format for flexibility
            String apiKey = config.optString("GEMINI_API_KEY", 
                           config.optString("gemini_api_key", 
                           config.optString("api_key", null)));
            String model = config.optString("GEMINI_DEFAULT_MODEL", 
                          config.optString("gemini_default_model",
                          config.optString("model", "gemini-1.5-flash")));
            String basePath = config.optString("GEMINI_BASE_PATH", 
                             config.optString("gemini_base_path", null));
            
            if (apiKey == null || apiKey.isEmpty()) {
                logger.warn("⚠️ [ChatService] Gemini configuration missing API key");
                return null;
            }
            
            logger.info("✅ [ChatService] Creating custom Gemini JSAIClient with resolved credentials");
            
            // Create configuration JSON for JSAIClient (replicating ServerManagedIntegrationsModule.createCustomGeminiAI)
            JSONObject configJson = new JSONObject();
            configJson.put("jsScriptPath", "js/geminiChatViaJs.js");
            configJson.put("clientName", "GeminiJSAIClientViaChatService");
            configJson.put("defaultModel", model);
            
            if (basePath != null && !basePath.trim().isEmpty()) {
                configJson.put("basePath", basePath);
            }
            
            // Set up secrets with resolved API key
            JSONObject secretsJson = new JSONObject();
            secretsJson.put("GEMINI_API_KEY", apiKey);
            configJson.put("secrets", secretsJson);
            
            logger.info("✅ [ChatService] Initializing Gemini JSAIClient with model: {}", model);
            return new JSAIClient(configJson, observer);
            
        } catch (Exception e) {
            logger.error("❌ [ChatService] Failed to create Gemini AI: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Creates a Dial AI instance from configuration.
     * Handles both real application format (uppercase) and test format (lowercase).
     */
    private AI createDialAI(JSONObject config, ConversationObserver observer) {
        try {
            // Handle both uppercase and lowercase format for flexibility
            String apiKey = config.optString("DIAL_AI_API_KEY", 
                           config.optString("dial_ai_api_key", 
                           config.optString("api_key", null)));
            String model = config.optString("DIAL_AI_MODEL", 
                          config.optString("dial_ai_model",
                          config.optString("model", "gpt-4")));
            String basePath = config.optString("DIAL_AI_BATH_PATH", 
                             config.optString("dial_ai_bath_path", "https://api.openai.com/v1"));
            
            if (apiKey == null || apiKey.isEmpty()) {
                logger.warn("⚠️ [ChatService] Dial configuration missing API key");
                return null;
            }
            
            logger.info("✅ [ChatService] Creating custom DialAIClient with resolved credentials");
            return new DialAIClient(basePath, apiKey, model, observer);
            
        } catch (Exception e) {
            logger.error("❌ [ChatService] Failed to create Dial AI: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Resolves AI instance from integration ID or automatically selects user's first AI integration.
     * Creates the specific AI instance using ServerManagedIntegrationsModule.
     * 
     * @param integrationId The integration ID to resolve (null/empty for automatic selection)
     * @param userId The user ID for access control
     * @return AI instance (specific integration AI)
     * @throws RuntimeException if AI integration resolution fails
     */
    private AI resolveAIFromIntegrationId(String integrationId, String userId) {
        if (userId == null) {
            throw new RuntimeException("User authentication is required for AI integration selection");
        }

        try {
            JSONObject integrationConfig;
            String actualIntegrationId;
            
            if (integrationId != null && !integrationId.trim().isEmpty()) {
                // Use specific integration ID provided by user
                logger.info("🤖 [ChatService] AI integration selection requested for ID: {}", integrationId);
                integrationConfig = integrationResolutionHelper.resolveSingleIntegrationId(integrationId, userId);
                actualIntegrationId = integrationId;
            } else {
                // Automatically use user's first AI integration
                logger.info("🤖 [ChatService] No AI integration specified, auto-selecting user's first AI integration");
                integrationConfig = integrationResolutionHelper.resolveUserFirstAIIntegration(userId);
                
                // Extract the actual integration ID from the resolved config
                IntegrationDto firstAI = integrationResolutionHelper.findUserFirstAIIntegration(userId);
                actualIntegrationId = firstAI != null ? firstAI.getId() : null;
            }
            
            // Create the specific AI instance
            AI integrationAI = createAIFromIntegrationConfig(integrationConfig, actualIntegrationId);
            if (integrationAI != null) {
                logger.info("✅ [ChatService] Successfully created AI instance from integration: {}", actualIntegrationId);
                return integrationAI;
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