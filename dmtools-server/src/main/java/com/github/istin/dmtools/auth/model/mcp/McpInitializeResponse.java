package com.github.istin.dmtools.auth.model.mcp;

import org.json.JSONObject;

/**
 * Represents an MCP initialize response result.
 */
public class McpInitializeResponse {
    private final String protocolVersion;
    private final JSONObject capabilities;
    private final ServerInfo serverInfo;
    private final String instructions;
    
    public McpInitializeResponse(String protocolVersion, JSONObject capabilities, ServerInfo serverInfo) {
        this(protocolVersion, capabilities, serverInfo, null);
    }
    
    public McpInitializeResponse(String protocolVersion, JSONObject capabilities, ServerInfo serverInfo, String instructions) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
        this.serverInfo = serverInfo;
        this.instructions = instructions;
    }
    
    public JSONObject toJson() {
        JSONObject result = new JSONObject()
                .put("protocolVersion", protocolVersion)
                .put("capabilities", capabilities)
                .put("serverInfo", serverInfo.toJson());
        
        if (instructions != null) {
            result.put("instructions", instructions);
        }
        
        return result;
    }
    
    public static class ServerInfo {
        private final String name;
        private final String version;
        
        public ServerInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }
        
        public JSONObject toJson() {
            return new JSONObject()
                    .put("name", name)
                    .put("version", version);
        }
    }
    
    public static McpInitializeResponse defaultResponse(String protocolVersion) {
        return new McpInitializeResponse(
                protocolVersion,
                new JSONObject().put("tools", new JSONObject()),
                new ServerInfo("dmtools-mcp-server", "1.0.0")
        );
    }
    
    public static McpInitializeResponse genericResponse() {
        return new McpInitializeResponse(
                "2025-07-27",
                new JSONObject().put("tools", new JSONObject()),
                new ServerInfo("dmtools-mcp-server", "1.0.0"),
                "This is a generic MCP endpoint. For full functionality, please use a configured MCP endpoint with a specific configuration ID."
        );
    }
}

