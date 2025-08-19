package com.github.istin.dmtools.auth.model.mcp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Represents an MCP tools/list response result.
 */
public class McpToolsListResponse {
    private final JSONArray tools;
    
    public McpToolsListResponse(JSONArray tools) {
        this.tools = tools;
    }
    
    public McpToolsListResponse(List<Map<String, Object>> toolsList) {
        this.tools = new JSONArray();
        for (Map<String, Object> tool : toolsList) {
            this.tools.put(new JSONObject(tool));
        }
    }
    
    public JSONObject toJson() {
        return new JSONObject().put("tools", tools);
    }
    
    public int getToolCount() {
        return tools.length();
    }
    
    public static McpToolsListResponse empty() {
        return new McpToolsListResponse(new JSONArray());
    }
    
}

