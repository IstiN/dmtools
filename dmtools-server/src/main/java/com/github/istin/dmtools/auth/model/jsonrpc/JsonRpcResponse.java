package com.github.istin.dmtools.auth.model.jsonrpc;

import org.json.JSONObject;

/**
 * Represents a JSON-RPC 2.0 response.
 */
public class JsonRpcResponse {
    private final String jsonrpc;
    private final Object id;
    private final Object result;
    private final JsonRpcError error;
    
    private JsonRpcResponse(Object id, Object result, JsonRpcError error) {
        this.jsonrpc = JsonRpcMethods.JSONRPC_VERSION;
        this.id = id;
        this.result = result;
        this.error = error;
    }
    
    /**
     * Creates a successful JSON-RPC response.
     */
    public static JsonRpcResponse success(Object id, Object result) {
        return new JsonRpcResponse(id, result, null);
    }
    
    /**
     * Creates an error JSON-RPC response.
     */
    public static JsonRpcResponse error(Object id, JsonRpcError error) {
        return new JsonRpcResponse(id, null, error);
    }
    
    /**
     * Creates an error JSON-RPC response with code and message.
     */
    public static JsonRpcResponse error(Object id, int code, String message) {
        return error(id, new JsonRpcError(code, message));
    }
    
    /**
     * Creates a notification response (no id).
     */
    public static JsonRpcResponse notification() {
        return new JsonRpcResponse(null, new JSONObject(), null);
    }
    
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    public Object getId() {
        return id;
    }
    
    public Object getResult() {
        return result;
    }
    
    public JsonRpcError getError() {
        return error;
    }
    
    public boolean isError() {
        return error != null;
    }
    
    public boolean isNotification() {
        return id == null || JSONObject.NULL.equals(id);
    }
    
    public JSONObject toJson() {
        JSONObject json = new JSONObject()
                .put("jsonrpc", jsonrpc);
        
        if (!isNotification()) {
            json.put("id", id);
        }
        
        if (isError()) {
            json.put("error", error.toJson());
        } else {
            json.put("result", result);
        }
        
        return json;
    }
    
    @Override
    public String toString() {
        return toJson().toString();
    }
}
