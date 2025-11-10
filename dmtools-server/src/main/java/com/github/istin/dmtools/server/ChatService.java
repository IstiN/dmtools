package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.Message;


import com.github.istin.dmtools.common.utils.LLMOptimizedJson;
import com.github.istin.dmtools.dto.ChatMessage;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
import com.github.istin.dmtools.dto.IntegrationDto;

import com.github.istin.dmtools.server.service.IntegrationResolutionHelper;
import com.github.istin.dmtools.server.service.McpConfigurationResolverService;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.ai.agent.ToolSelectorAgent;
import com.github.istin.dmtools.dto.ToolCallRequest;
import org.json.JSONArray;
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
    
    @Autowired
    private McpConfigurationResolverService mcpConfigurationResolverService;

    public ChatResponse chat(ChatRequest request) {
        return chat(request, null);
    }

    public ChatResponse chat(ChatRequest request, String userId) {
        return chatWithFiles(request, null, userId);
    }

    public ChatResponse chatWithFiles(ChatRequest request, List<File> files) {
        return chatWithFiles(request, files, null);
    }

    public ChatResponse chatWithFiles(ChatRequest request, List<File> files, String userId) {
        return processChatRequest(request, files, userId);
    }

    public ChatResponse simpleChatMessage(String message, String model, String aiIntegrationId, String userId) {
        // Create a simple ChatRequest with single message
        ChatRequest request = new ChatRequest();
        request.setMessages(Arrays.asList(new ChatMessage("user", message, null))); // null for fileNames
        request.setModel(model);
        request.setAi(aiIntegrationId); // Set AI integration ID if provided
        
        return processChatRequest(request, null, userId);
    }

    /**
     * Main processing method that handles all chat request types with unified resolution logic
     */
    private ChatResponse processChatRequest(ChatRequest request, List<File> files, String userId) {
        try {
            // Validate request
            if (request.getMessages() == null || request.getMessages().isEmpty()) {
                logger.warn("Chat request received with no messages.");
                return ChatResponse.error("Failed to process chat request: Message list cannot be empty.");
            }
            
            logger.info("Processing chat request with {} messages and {} files", 
                       request.getMessages().size(), files != null ? files.size() : 0);
            
            // Step 1: Resolve AI integration (required)
            AI aiToUse = resolveAIFromRequest(request, userId);
            
            // Step 2: Convert ChatMessage DTOs to AI Message objects (with or without files)
            List<Message> messages = (files != null && !files.isEmpty()) 
                ? convertToAIMessagesWithFiles(request.getMessages(), files)
                : convertToAIMessages(request.getMessages());
            
                    // Step 3: Process with or without MCP tools based on configuration
        if (request.getMcpConfigId() != null && !request.getMcpConfigId().trim().isEmpty()) {
            // Resolve MCP configuration once
            McpConfigurationResolverService.McpConfigurationResult mcpConfigResult;
            Map<String, Object> toolsResult;
            try {
                mcpConfigResult = mcpConfigurationResolverService.resolveMcpConfiguration(request.getMcpConfigId());
                toolsResult = mcpConfigurationResolverService.getToolsListAsMap(mcpConfigResult);
            } catch (Exception e) {
                logger.error("Failed to load MCP tools for config {}: {}", request.getMcpConfigId(), e.getMessage());
                return chatWithoutTools(messages, request, aiToUse);
            }
            
            return chatWithMcpTools(messages, request, aiToUse, toolsResult, mcpConfigResult);
        } else {
            return chatWithoutTools(messages, request, aiToUse);
        }
            
        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            return ChatResponse.error("Failed to process chat request: " + e.getMessage());
        }
    }

    private ChatResponse chatWithoutTools(List<Message> messages, ChatRequest request, AI aiToUse) throws Exception {
        // Use the AI service to get response without tools
        String response;
        if (request.getModel() != null && !request.getModel().trim().isEmpty()) {
            response = aiToUse.chat(request.getModel(), messages.toArray(new Message[0]));
        } else {
            response = aiToUse.chat(messages.toArray(new Message[0]));
        }
        
        logger.info("Successfully processed chat request without tools");
        return ChatResponse.success(response);
    }

    private ChatResponse chatWithMcpTools(List<Message> messages, ChatRequest request, AI aiToUse, Map<String, Object> toolsResult, McpConfigurationResolverService.McpConfigurationResult mcpConfigResult) {
        try {
            logger.info("Processing chat request with MCP tools enabled using ToolSelectorAgent workflow");
            
            // Extract available tools from resolved MCP configuration
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> availableTools = (List<Map<String, Object>>) toolsResult.get("tools");
            
            if (availableTools == null || availableTools.isEmpty()) {
                logger.warn("No MCP tools available for config {}, falling back to regular chat", request.getMcpConfigId());
                return chatWithoutTools(messages, request, aiToUse);
            }
            
            logger.info("Found {} MCP tools available for config {}", availableTools.size(), request.getMcpConfigId());
            
            // Step 2: Create ToolSelectorAgent instance
            ToolSelectorAgent toolSelectorAgent = new ToolSelectorAgent(aiToUse, new com.github.istin.dmtools.prompt.PromptManager());
            logger.info("Created ToolSelectorAgent with provided AI instance");
            
            // Step 3: Iteratively call tools until no more tools are needed
            List<Message> workingMessages = new ArrayList<>(messages);
            List<ToolCallRequest> allSelectedToolCalls = new ArrayList<>();
            List<String> allToolResults = new ArrayList<>();
            String availableToolsString = formatToolsForToolSelector(availableTools);
            
            int iteration = 0;
            final int MAX_ITERATIONS = 5; // Prevent infinite loops
            
            while (iteration < MAX_ITERATIONS) {
                iteration++;
                logger.info("ToolSelector iteration {}", iteration);
                
                // Prepare parameters for ToolSelectorAgent with current conversation state
                String currentMessages = formatMessagesForToolSelector(workingMessages);
                ToolSelectorAgent.Params toolSelectorParams = new ToolSelectorAgent.Params(currentMessages, availableToolsString);
                
                // Call ToolSelectorAgent to determine which tools to use
                List<ToolCallRequest> selectedToolCalls;
                try {
                    selectedToolCalls = toolSelectorAgent.run(toolSelectorParams);
                    logger.info("ToolSelectorAgent iteration {} selected {} tools", iteration, 
                        selectedToolCalls != null ? selectedToolCalls.size() : 0);
                } catch (Exception e) {
                    logger.error("ToolSelectorAgent execution failed on iteration {}: {}", iteration, e.getMessage());
                    break; // Exit loop on error
                }
                
                // If no tools selected, we're done
                if (selectedToolCalls == null || selectedToolCalls.isEmpty()) {
                    logger.info("ToolSelectorAgent returned no tools on iteration {}, stopping", iteration);
                    break;
                }
                
                // Execute selected tools and add results to working messages
                ToolExecutionResult iterationResult = executeToolsAndPrepareMessages(
                    workingMessages, selectedToolCalls, mcpConfigResult);
                
                // Update working messages with new tool results
                workingMessages = iterationResult.messages;
                
                // Track all tool calls and results for final formatting
                allSelectedToolCalls.addAll(selectedToolCalls);
                allToolResults.addAll(iterationResult.toolResults);
                
                logger.info("Completed iteration {} with {} tools executed", iteration, selectedToolCalls.size());
            }
            
            if (iteration >= MAX_ITERATIONS) {
                logger.warn("Reached maximum iterations ({}) for tool selection", MAX_ITERATIONS);
            }
            
            logger.info("Tool selection completed after {} iterations with {} total tools executed", 
                iteration, allSelectedToolCalls.size());
            
            // Step 4: Send all messages to AI for final response
            String finalResponse;
            if (request.getModel() != null && !request.getModel().trim().isEmpty()) {
                finalResponse = aiToUse.chat(request.getModel(), workingMessages.toArray(new Message[0]));
            } else {
                finalResponse = aiToUse.chat(workingMessages.toArray(new Message[0]));
            }
            
            // Step 5: Format final response with tool execution information
            String formattedResponse = formatFinalResponseWithToolInfo(finalResponse, allSelectedToolCalls, allToolResults);
            
            logger.info("Successfully processed chat request with MCP tools using ToolSelectorAgent workflow");
            return ChatResponse.success(formattedResponse);
            
        } catch (Exception e) {
            logger.error("Error in chat with MCP tools", e);
            // Fallback to regular chat if tool integration fails
            logger.warn("Falling back to regular chat without tools");
            try {
                return chatWithoutTools(messages, request, aiToUse);
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
     * Inner class to hold tool execution results
     */
    private static class ToolExecutionResult {
        final List<Message> messages;
        final List<String> toolResults;
        
        ToolExecutionResult(List<Message> messages, List<String> toolResults) {
            this.messages = messages;
            this.toolResults = toolResults;
        }
    }



    /**
     * Resolves AI instance from ChatRequest
     */
    private AI resolveAIFromRequest(ChatRequest request, String userId) {
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
                return integrationAI; // Successfully resolved
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
     * Formats all messages for ToolSelectorAgent consumption
     */
    private String formatMessagesForToolSelector(List<Message> messages) {
        StringBuilder messageText = new StringBuilder();
        for (Message message : messages) {
            messageText.append(message.getRole()).append(": ").append(message.getText()).append("\n\n");
        }
        return messageText.toString().trim();
    }

    /**
     * Formats available tools for ToolSelectorAgent consumption
     */
    private String formatToolsForToolSelector(List<Map<String, Object>> tools) {
        try {
            if (true) {
                return LLMOptimizedJson.format(new JSONArray().put(tools).get(0).toString());
            }
            // Convert tools list to JSON string for ToolSelectorAgent
            JSONObject toolsJson = new JSONObject();
            toolsJson.put("tools", tools);
            return toolsJson.toString(2); // Pretty print with indent
        } catch (Exception e) {
            logger.error("Error formatting tools for ToolSelectorAgent: {}", e.getMessage());
            // Fallback to simple string representation
            StringBuilder toolsInfo = new StringBuilder();
            for (Map<String, Object> tool : tools) {
                String name = (String) tool.get("name");
                String description = (String) tool.get("description");
                toolsInfo.append("Tool: ").append(name).append(" - ").append(description).append("\n");
            }
            return toolsInfo.toString();
        }
    }

    /**
     * Executes selected tools and prepares messages with results inserted before last user message
     */
    private ToolExecutionResult executeToolsAndPrepareMessages(List<Message> originalMessages, 
                                                         List<ToolCallRequest> toolCalls, 
                                                         McpConfigurationResolverService.McpConfigurationResult mcpConfigResult) {
        List<Message> messagesWithResults = new ArrayList<>(originalMessages);
        
        // If no tools were selected, return original messages
        if (toolCalls == null || toolCalls.isEmpty()) {
            logger.info("No tools selected for execution, proceeding with original messages");
            return new ToolExecutionResult(messagesWithResults, new ArrayList<>());
        }
        
        // Find the index of the last user message
        int lastUserMessageIndex = -1;
        for (int i = messagesWithResults.size() - 1; i >= 0; i--) {
            if ("user".equals(messagesWithResults.get(i).getRole())) {
                lastUserMessageIndex = i;
                break;
            }
        }
        
        // If no user message found, insert tool results at the end
        if (lastUserMessageIndex == -1) {
            lastUserMessageIndex = messagesWithResults.size();
        }
        
        // Execute each tool and insert results as model messages before the last user message
        List<String> toolExecutionResults = new ArrayList<>();
        for (ToolCallRequest toolCall : toolCalls) {
            try {
                logger.info("Executing tool: {} with arguments: {}", toolCall.getToolName(), toolCall.getArguments());
                
                // Execute the tool
                Object result = mcpConfigurationResolverService.executeToolCallRaw(
                    mcpConfigResult, toolCall.getToolName(), toolCall.getArguments());
                
                String resultString = result != null ? result.toString() : "Tool executed successfully but returned no result.";
                
                // Store result for final response formatting
                String toolReason = (toolCall.getReason() != null && !toolCall.getReason().trim().isEmpty()) 
                    ? String.format(" (%s)", toolCall.getReason()) 
                    : "";
                toolExecutionResults.add(String.format("Tool: %s%s\nResult: %s", toolCall.getToolName(), toolReason, resultString));
                
                // Create model message with tool execution result
                Message toolResultMessage = new Message("model", 
                    String.format("Tool execution result for '%s'%s: %s", toolCall.getToolName(), toolReason, resultString), 
                    null);
                
                // Insert before the last user message
                messagesWithResults.add(lastUserMessageIndex, toolResultMessage);
                lastUserMessageIndex++; // Adjust index for next insertion
                
            } catch (Exception e) {
                logger.error("Failed to execute tool {}: {}", toolCall.getToolName(), e.getMessage());
                
                String errorMsg = String.format("Error executing tool '%s': %s", toolCall.getToolName(), e.getMessage());
                toolExecutionResults.add(String.format("Tool: %s\nError: %s", toolCall.getToolName(), e.getMessage()));
                
                // Create model message with error
                Message errorMessage = new Message("model", errorMsg, null);
                messagesWithResults.add(lastUserMessageIndex, errorMessage);
                lastUserMessageIndex++;
            }
        }
        
        logger.info("Executed {} tools and inserted results into message chain", toolCalls.size());
        return new ToolExecutionResult(messagesWithResults, toolExecutionResults);
    }

    /**
     * Formats the final response with markdown showing tool executions BEFORE the AI response
     */
    private String formatFinalResponseWithToolInfo(String finalResponse, List<ToolCallRequest> toolCalls, List<String> toolResults) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return finalResponse;
        }
        
        StringBuilder formattedResponse = new StringBuilder();
        
        // Add tool execution information as markdown FIRST
        formattedResponse.append("## Tools Used\n\n");
        
        for (int i = 0; i < toolCalls.size(); i++) {
            ToolCallRequest toolCall = toolCalls.get(i);
            formattedResponse.append(String.format("**%d. %s**\n", i + 1, toolCall.getToolName()));
            
            if (toolCall.getReason() != null && !toolCall.getReason().trim().isEmpty()) {
                formattedResponse.append("- Reason: ").append(toolCall.getReason()).append("\n");
            }
            
            if (toolCall.getArguments() != null && !toolCall.getArguments().isEmpty()) {
                formattedResponse.append("- Arguments: `").append(formatArgumentsForDisplay(toolCall.getArguments())).append("`\n");
            }
            
            // Add tool result as code block
            if (toolResults != null && i < toolResults.size()) {
                formattedResponse.append("- Response:\n```\n");
                formattedResponse.append(toolResults.get(i));
                formattedResponse.append("\n```\n");
            }
            
            formattedResponse.append("\n");
        }
        
        // Add separator and then the AI response
        formattedResponse.append("---\n\n");
        formattedResponse.append(finalResponse);
        
        return formattedResponse.toString();
    }
    
    /**
     * Helper method to format arguments for display in markdown
     */
    private String formatArgumentsForDisplay(Map<String, Object> arguments) {
        try {
            JSONObject argsJson = new JSONObject(arguments);
            return argsJson.toString();
        } catch (Exception e) {
            return arguments.toString();
        }
    }


} 