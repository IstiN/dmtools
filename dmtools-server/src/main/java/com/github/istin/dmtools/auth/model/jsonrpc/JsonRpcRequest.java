package com.github.istin.dmtools.auth.model.jsonrpc;

import org.json.JSONObject;

/**
 * Represents a JSON-RPC 2.0 request.
 */
public class JsonRpcRequest {
    private final String jsonrpc;
    private final String method;
    private final Object id;
    private final JSONObject params;
    
    public JsonRpcRequest(String method, Object id, JSONObject params) {
        this.jsonrpc = JsonRpcMethods.JSONRPC_VERSION;
        this.method = method;
        this.id = id;
        this.params = params;
    }
    
    public JsonRpcRequest(JSONObject requestJson) {
        this.jsonrpc = requestJson.optString("jsonrpc", JsonRpcMethods.JSONRPC_VERSION);
        this.method = requestJson.optString("method");
        this.id = requestJson.opt("id");
        this.params = requestJson.optJSONObject("params");
    }
    
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    public String getMethod() {
        return method;
    }
    
    public Object getId() {
        return id;
    }
    
    public JSONObject getParams() {
        return params;
    }
    
    public boolean isNotification() {
        return id == null || JSONObject.NULL.equals(id);
    }
    
    public JSONObject toJson() {
        JSONObject json = new JSONObject()
                .put("jsonrpc", jsonrpc)
                .put("method", method);
        
        if (!isNotification()) {
            json.put("id", id);
        }
        
        if (params != null) {
            json.put("params", params);
        }
        
        return json;
    }
    
    @Override
    public String toString() {
        return toJson().toString();
    }
}
