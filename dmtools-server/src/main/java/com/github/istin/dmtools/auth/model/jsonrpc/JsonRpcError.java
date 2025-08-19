package com.github.istin.dmtools.auth.model.jsonrpc;

import org.json.JSONObject;

/**
 * Represents a JSON-RPC 2.0 error.
 */
public class JsonRpcError {
    
    // Standard JSON-RPC error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    
    private final int code;
    private final String message;
    private final Object data;
    
    public JsonRpcError(int code, String message) {
        this(code, message, null);
    }
    
    public JsonRpcError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
    
    public JSONObject toJson() {
        JSONObject json = new JSONObject()
                .put("code", code)
                .put("message", message);
        
        if (data != null) {
            json.put("data", data);
        }
        
        return json;
    }
    
    @Override
    public String toString() {
        return toJson().toString();
    }
    
    // Common error factory methods
    public static JsonRpcError parseError(String details) {
        return new JsonRpcError(PARSE_ERROR, "Parse error" + (details != null ? ": " + details : ""));
    }
    
    public static JsonRpcError invalidRequest(String details) {
        return new JsonRpcError(INVALID_REQUEST, "Invalid Request" + (details != null ? ": " + details : ""));
    }
    
    public static JsonRpcError methodNotFound(String method) {
        return new JsonRpcError(METHOD_NOT_FOUND, "Method not found: " + method);
    }
    
    public static JsonRpcError invalidParams(String details) {
        return new JsonRpcError(INVALID_PARAMS, "Invalid params" + (details != null ? ": " + details : ""));
    }
    
    public static JsonRpcError internalError(String details) {
        return new JsonRpcError(INTERNAL_ERROR, "Internal error" + (details != null ? ": " + details : ""));
    }
}

