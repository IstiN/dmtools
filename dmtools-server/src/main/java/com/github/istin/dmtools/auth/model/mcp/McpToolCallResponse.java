package com.github.istin.dmtools.auth.model.mcp;

import org.json.JSONObject;

/**
 * Represents an MCP tools/call response result.
 */
public class McpToolCallResponse {
    private final McpContent content;
    
    public McpToolCallResponse(McpContent content) {
        this.content = content;
    }
    
    public McpToolCallResponse(String text) {
        this.content = McpContent.text(text);
    }
    
    public JSONObject toJson() {
        return new JSONObject().put("content", content.toJsonArray());
    }
    
    public static McpToolCallResponse text(String text) {
        return new McpToolCallResponse(text);
    }
    
    public static McpToolCallResponse file(String downloadUrl, String filename, String mimeType, String expiresIn) {
        // Return file information as JSON text for MCP compatibility
        McpContent fileContent = McpContent.file(downloadUrl, filename, mimeType, expiresIn);
        String jsonText = fileContent.toJsonArray().getJSONObject(0).toString(2);
        return new McpToolCallResponse(jsonText);
    }
    
}