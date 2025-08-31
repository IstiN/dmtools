package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.server.service.McpConfigurationResolverService;

import com.github.istin.dmtools.auth.model.jsonrpc.JsonRpcRequest;
import com.github.istin.dmtools.auth.model.jsonrpc.JsonRpcResponse;
import com.github.istin.dmtools.auth.model.jsonrpc.JsonRpcError;
import com.github.istin.dmtools.auth.model.jsonrpc.JsonRpcMethods;

import com.github.istin.dmtools.auth.model.mcp.McpInitializeResponse;
import com.github.istin.dmtools.auth.model.mcp.McpToolsListResponse;
import com.github.istin.dmtools.auth.model.mcp.McpToolCallResponse;



import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;


@RestController
@RequestMapping("/mcp")
public class DynamicMCPController {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMCPController.class);

    // ===== LOGGING HELPER METHODS =====

    private void logRequestStart(String endpoint, String configId, String method, String body) {
        if (logger.isInfoEnabled()) {
            logger.info("===== MCP {} Request for configId: {} =====", endpoint, configId);
            if (method != null) {
                logger.info("🎯 Method: {}, Body: {}", method, body != null ? body.substring(0, Math.min(100, body.length())) + (body.length() > 100 ? "..." : "") : "null");
            }
        }
    }

    private void logRequestAnalysis(HttpServletRequest request, String configId) {
        if (logger.isDebugEnabled()) {
            logger.debug("=== Request Analysis for {} ===", configId);
            logger.debug("Method: {}, Path: {}, Content-Type: {}", 
                request.getMethod(), request.getRequestURI(), request.getContentType());
            logger.debug("User-Agent: {}", request.getHeader("User-Agent"));
        }
    }




    private void logError(String operation, Exception e) {
        logger.error("Error in {}: {}", operation, e.getMessage());
        if (logger.isDebugEnabled()) {
            logger.debug("Full stack trace for " + operation, e);
        }
    }



    private final McpConfigurationResolverService mcpConfigurationResolverService;

    public DynamicMCPController(McpConfigurationResolverService mcpConfigurationResolverService) {
        this.mcpConfigurationResolverService = mcpConfigurationResolverService;
    }


    /**
     * Core business logic for initialize - unified for both SSE and JSON-RPC
     */
    private McpInitializeResponse processInitialize(JSONObject params) {
        String clientProtocolVersion = params != null ? params.optString("protocolVersion", "2025-01-08") : "2025-01-08";
        return McpInitializeResponse.defaultResponse(clientProtocolVersion);
    }

    /**
     * Core business logic for generating tools list - unified for both SSE and JSON-RPC
     */
    private McpToolsListResponse generateToolsList(String configId) throws Exception {
        return mcpConfigurationResolverService.generateToolsList(configId);
    }

    /**
     * Core business logic for tool calls - unified for both SSE and JSON-RPC
     */
    private McpToolCallResponse processToolCall(String configId, String toolName, JSONObject arguments, HttpServletRequest request) throws Exception {
        return mcpConfigurationResolverService.processToolCall(configId, toolName, arguments, request);
    }



    @GetMapping(value = "/stream/{configId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter mcpStreamGet(@PathVariable String configId, HttpServletRequest request) {
        logRequestStart("SSE GET", configId, "GET", null);
        logRequestAnalysis(request, configId);
        
        // Default initialization for SSE GET requests
        JsonRpcRequest initRequest = new JsonRpcRequest(JsonRpcMethods.INITIALIZE, "init-get", new JSONObject());
        return mcpStreamPost(configId, initRequest.toString(), request);
    }

    @PostMapping(value = "/stream/{configId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter mcpStreamPost(@PathVariable String configId, @RequestBody String body, HttpServletRequest request) {
        logRequestStart("SSE POST", configId, "POST", body);
        logRequestAnalysis(request, configId);

        SseEmitter emitter = new SseEmitter();

        // Process SSE request directly (no need for additional async wrapper)
        try {
            JSONObject requestJson = new JSONObject(body);
            String method = requestJson.getString("method");
            Object id = requestJson.opt("id");

            // Handle different MCP methods
            switch (method) {
                case JsonRpcMethods.INITIALIZE:
                    handleInitialize(emitter, id, requestJson.optJSONObject("params"), configId, request);
                    break;

                case JsonRpcMethods.TOOLS_LIST:
                    handleToolsList(emitter, id, configId);
                    break;

                case JsonRpcMethods.TOOLS_CALL:
                    handleToolCall(emitter, id, requestJson.optJSONObject("params"), configId, request);
                    break;

                case JsonRpcMethods.NOTIFICATIONS_INITIALIZED:
                    // This is just a notification - no response needed
                    break;

                default:
                    sendError(emitter, id, -32601, "Method not found: " + method);
                    break;
            }

        } catch (Exception e) {
            logError("SSE processing", e);
            try {
                sendError(emitter, null, -32603, "Internal error: " + e.getMessage());
            } catch (Exception sendError) {
                logError("SSE error response", sendError);
            }
        } finally {
            // Complete the emitter to close SSE connection
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // Emitter already completed
            }
        }

        return emitter;
    }
    
    private void sendSseEvent(SseEmitter emitter, String data) throws IOException {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IllegalStateException e) {
            logger.warn("Emitter already completed, skipping send");
            throw e;
        }
    }

    private void handleInitialize(SseEmitter emitter, Object id, JSONObject params, String configId, HttpServletRequest request) throws Exception {
        McpInitializeResponse initResponse = processInitialize(params);
        JsonRpcResponse response = JsonRpcResponse.success(id, initResponse.toJson());

        sendSseEvent(emitter, response.toString());
        
        // For Gemini CLI (user-agent: node), auto-send tools/list after initialize over SSE
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.contains("node")) {
            handleToolsList(emitter, "auto-tools-list", configId);
        }
    }

    private void handleToolsList(SseEmitter emitter, Object id, String configId) throws Exception {
        try {
            McpToolsListResponse toolsResponse = generateToolsList(configId);
            JsonRpcResponse response = JsonRpcResponse.success(id, toolsResponse.toJson());
            sendSseEvent(emitter, response.toString());
        } catch (IllegalArgumentException e) {
            logError("SSE tools/list - invalid params", e);
            sendError(emitter, id, JsonRpcError.INVALID_PARAMS, "Invalid params: " + e.getMessage());
        } catch (Exception e) {
            logError("SSE tools/list", e);
            sendError(emitter, id, JsonRpcError.INTERNAL_ERROR, "Internal error: " + e.getMessage());
        }
    }

    private void handleToolCall(SseEmitter emitter, Object id, JSONObject params, String configId, HttpServletRequest request) throws Exception {
        try {
            String toolName = params.optString("name");
            JSONObject arguments = params.optJSONObject("arguments");

            // Use unified business logic with request context for file handling
            McpToolCallResponse toolResponse = processToolCall(configId, toolName, arguments, request);
            JsonRpcResponse response = JsonRpcResponse.success(id, toolResponse.toJson());

            sendSseEvent(emitter, response.toString());

        } catch (IllegalArgumentException e) {
            logError("SSE tools/call - invalid params", e);
            sendError(emitter, id, JsonRpcError.INVALID_PARAMS, "Invalid params: " + e.getMessage());
        } catch (Exception e) {
            logError("SSE tools/call", e);
            sendError(emitter, id, JsonRpcError.INTERNAL_ERROR, "Internal error: " + e.getMessage());
        }
    }




    




    private void sendError(SseEmitter emitter, Object id, int code, String message) {
        try {
            JsonRpcResponse response = JsonRpcResponse.error(id, new JsonRpcError(code, message));

            logger.error("Sending error response: {}", response);
            sendSseEvent(emitter, response.toString());
        } catch (Exception e) {
            logger.error("Failed to send error", e);
        }
    }


    

    
    /**
     * Determines MIME type for the file based on extension.
     */
} 