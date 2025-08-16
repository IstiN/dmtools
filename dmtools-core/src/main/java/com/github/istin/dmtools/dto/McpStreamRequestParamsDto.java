package com.github.istin.dmtools.dto;

import org.json.JSONObject;

public class McpStreamRequestParamsDto {
    private String method;
    private String id; // Can be String or Integer, use String for flexibility
    private String params; // JSON string for the params object

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("jsonrpc", "2.0"); // MCP protocol always uses 2.0
        json.put("method", method);
        if (id != null) {
            try {
                json.put("id", Integer.parseInt(id));
            } catch (NumberFormatException e) {
                json.put("id", id);
            }
        }
        if (params != null && !params.isEmpty()) {
            try {
                json.put("params", new JSONObject(params));
            } catch (Exception e) {
                // Log error or handle invalid JSON params
                System.err.println("Invalid JSON for params: " + params + " - " + e.getMessage());
                json.put("params", new JSONObject()); // Default to empty object
            }
        }
        return json;
    }
}
