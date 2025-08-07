package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.Message;
import com.github.istin.dmtools.ai.agent.ToolSelectorAgent;
import com.github.istin.dmtools.auth.controller.DynamicMCPController;
import com.github.istin.dmtools.dto.ChatMessage;
import com.github.istin.dmtools.dto.ChatRequest;
import com.github.istin.dmtools.dto.ChatResponse;
import com.github.istin.dmtools.dto.ToolCallRequest;
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

    public ChatResponse chat(ChatRequest request) {
        try {
            if (request.getMessages() == null || request.getMessages().isEmpty()) {
                logger.warn("Chat request received with no messages.");
                return ChatResponse.error("Failed to process chat request: Message list cannot be empty.");
            }
            logger.info("Processing chat request with {} messages", request.getMessages().size());
            
            // Convert ChatMessage DTOs to AI Message objects
            List<Message> messages = convertToAIMessages(request.getMessages());
            
            // Check if agent tools are enabled
            if (request.getAgentTools() != null && request.getAgentTools().isEnabled()) {
                return chatWithMcpTools(messages, request);
            } else {
                return chatWithoutTools(messages, request);
            }
            
        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            return ChatResponse.error("Failed to process chat request: " + e.getMessage());
        }
    }

    public ChatResponse chatWithFiles(ChatRequest request, List<File> files) {
        try {
            logger.info("Processing chat request with {} messages and {} files", 
                       request.getMessages().size(), files != null ? files.size() : 0);
            
            // Convert ChatMessage DTOs to AI Message objects, attaching files to the last user message
            List<Message> messages = convertToAIMessagesWithFiles(request.getMessages(), files);
            
            // Check if agent tools are enabled
            if (request.getAgentTools() != null && request.getAgentTools().isEnabled()) {
                return chatWithMcpTools(messages, request);
            } else {
                return chatWithoutTools(messages, request);
            }
            
        } catch (Exception e) {
            logger.error("Error processing chat request with files", e);
            return ChatResponse.error("Failed to process chat request with files: " + e.getMessage());
        }
    }

    public ChatResponse simpleChatMessage(String message, String model) {
        try {
            logger.info("Processing simple chat message");
            
            String response;
            if (model != null && !model.trim().isEmpty()) {
                response = ai.chat(model, message);
            } else {
                response = ai.chat(message);
            }
            
            logger.info("Successfully processed simple chat message");
            return ChatResponse.success(response, model);
            
        } catch (Exception e) {
            logger.error("Error processing simple chat message", e);
            return ChatResponse.error("Failed to process message: " + e.getMessage());
        }
    }

    private ChatResponse chatWithoutTools(List<Message> messages, ChatRequest request) throws Exception {
        // Use the AI service to get response without tools
        String response;
        if (request.getModel() != null && !request.getModel().trim().isEmpty()) {
            response = ai.chat(request.getModel(), messages.toArray(new Message[0]));
        } else {
            response = ai.chat(messages.toArray(new Message[0]));
        }
        
        logger.info("Successfully processed chat request without tools");
        return ChatResponse.success(response, request.getModel());
    }

    private ChatResponse chatWithMcpTools(List<Message> messages, ChatRequest request) {
        try {
            logger.info("Processing chat request with MCP tools enabled");
            
            // TODO: Uncomment this logic after the MCP generated classes are available
            // Map<String, Object> toolsResult = getToolsListSafely();
            // @SuppressWarnings("unchecked")
            // List<Map<String, Object>> availableTools = (List<Map<String, Object>>) toolsResult.get("tools");
            
            // if (availableTools == null || availableTools.isEmpty()) {
            //     logger.warn("No MCP tools available, falling back to regular chat");
            //     return chatWithoutTools(messages, request);
            // }
            
            // List<Map<String, Object>> filteredTools = filterToolsBasedOnConfig(availableTools, request.getAgentTools());
            
            // String lastUserMessage = getLastUserMessage(messages);
            // String formattedTools = formatToolsToString(filteredTools);
            // ToolSelectorAgent.Params params = new ToolSelectorAgent.Params(lastUserMessage, formattedTools);
            // List<ToolCallRequest> toolCalls = toolSelectorAgent.run(params);

            // if (toolCalls != null && !toolCalls.isEmpty()) {
            //     return executeToolCallsAndRespond(toolCalls, messages, request);
            // } else {
            //     return chatWithoutTools(messages, request);
            // }
            return chatWithoutTools(messages, request); // Placeholder
        } catch (Exception e) {
            logger.error("Error in chat with MCP tools", e);
            // Fallback to regular chat if tool integration fails
            logger.warn("Falling back to regular chat without tools");
            try {
                return chatWithoutTools(messages, request);
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
            return chatWithoutTools(messages, request);
            
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
} 