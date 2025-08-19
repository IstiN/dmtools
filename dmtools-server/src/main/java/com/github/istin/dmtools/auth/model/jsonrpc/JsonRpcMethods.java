package com.github.istin.dmtools.auth.model.jsonrpc;

/**
 * Constants for JSON-RPC and MCP protocol used in MCP (Model Context Protocol).
 */
public final class JsonRpcMethods {
    
    // Core MCP methods
    public static final String INITIALIZE = "initialize";
    public static final String PING = "ping";
    public static final String TOOLS_LIST = "tools/list";
    public static final String TOOLS_CALL = "tools/call";
    
    // Notification methods
    public static final String NOTIFICATIONS_INITIALIZED = "notifications/initialized";
    
    // Protocol constants
    public static final String JSONRPC_VERSION = "2.0";
    
    // HTTP methods
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    
    // JSON-RPC error codes (standard)
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    
    private JsonRpcMethods() {
        // Utility class - prevent instantiation
    }
}
