package com.github.istin.dmtools.job;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.mcp.generated.MCPSchemaGenerator;
import com.github.istin.dmtools.mcp.generated.MCPToolExecutor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JavaScript bridge for AI jobs using generated MCP infrastructure.
 * Provides sandboxed JavaScript execution with access to MCP-compatible methods
 * and support for loading JavaScript from GitHub URLs, resources, or inline code.
 */
@Singleton
public class JobJavaScriptBridge {

    private static final Logger logger = LogManager.getLogger(JobJavaScriptBridge.class);

    private final TrackerClient<?> trackerClient;
    private final AI ai;
    private final Confluence confluence;
    private final SourceCode sourceCode;
    private final Map<String, String> resourceCache = new ConcurrentHashMap<>();
    private Context jsContext;
    private final Map<String, Object> clientInstances;

    @Inject
    public JobJavaScriptBridge(TrackerClient<?> trackerClient, AI ai, Confluence confluence, SourceCode sourceCode) {
        this.trackerClient = trackerClient;
        this.ai = ai;
        this.confluence = confluence;
        this.sourceCode = sourceCode;
        
        // Prepare client instances for MCP executor
        this.clientInstances = new HashMap<>();
        this.clientInstances.put("jira", trackerClient);
        this.clientInstances.put("ai", ai);
        this.clientInstances.put("confluence", confluence);
        
        initializeJavaScriptContext();
    }

    /**
     * Initialize JavaScript context with MCP method bindings
     */
    private void initializeJavaScriptContext() {
        try {
            jsContext = Context.newBuilder("js")
                    .allowAllAccess(false) // Restricted access for security
                    .build();

            // Expose the Java bridge for tool execution using ProxyObject
            jsContext.getBindings("js").putMember("executeToolViaJava", new ExecuteToolProxy());

            // Expose MCP tools using generated infrastructure
            exposeMCPToolsUsingGenerated();

        } catch (Exception e) {
            logger.error("Failed to initialize JavaScript context", e);
            throw new RuntimeException("JavaScript bridge initialization failed", e);
        }
    }
    
    /**
     * Execute MCP tool from JavaScript
     */
    public Object executeToolFromJS(String toolName, Object jsArgs) {
        try {
            // Convert JavaScript object to Map
            Map<String, Object> argsMap = new HashMap<>();
            if (jsArgs != null) {
                logger.debug("Converting JS args for tool {}: {} (type: {})", toolName, jsArgs, jsArgs.getClass().getName());
                
                Value argsValue = Value.asValue(jsArgs);
                logger.debug("JS args as Value: hasMembers={}, isHostObject={}", argsValue.hasMembers(), argsValue.isHostObject());
                
                if (argsValue.hasMembers()) {
                    for (String key : argsValue.getMemberKeys()) {
                        Object memberValue = argsValue.getMember(key).as(Object.class);
                        argsMap.put(key, memberValue);
                        logger.debug("Converted JS arg: {} = {} (type: {})", key, memberValue, memberValue != null ? memberValue.getClass().getName() : "null");
                    }
                } else if (argsValue.isHostObject()) {
                    // Handle case where JS object is passed as host object
                    Object hostObject = argsValue.asHostObject();
                    if (hostObject instanceof Map) {
                        argsMap.putAll((Map<String, Object>) hostObject);
                        logger.debug("Used host object as Map: {}", argsMap);
                    }
                }
            }
            
            logger.debug("Final args map for tool {}: {}", toolName, argsMap);
            
            // Execute using generated MCP infrastructure
            return MCPToolExecutor.executeTool(toolName, argsMap, clientInstances);
        } catch (Exception e) {
            logger.error("Tool execution failed for {}: {}", toolName, e.getMessage(), e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Expose MCP tools using generated MCPToolExecutor - much better than reflection!
     */
    private void exposeMCPToolsUsingGenerated() {
        // Get all available integrations
        Set<String> integrations = Set.of("jira", "ai", "confluence", "figma");
        
        // Generate tool schemas using MCP infrastructure
        Map<String, Object> toolsResponse = MCPSchemaGenerator.generateToolsListResponse(integrations);
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> tools = (java.util.List<Map<String, Object>>) toolsResponse.get("tools");
        
        // Expose each tool to JavaScript
        for (Map<String, Object> tool : tools) {
            String toolName = (String) tool.get("name");
            exposeToolToJS(toolName);
        }
        
        logger.info("Exposed {} MCP tools to JavaScript using generated infrastructure", tools.size());
    }

    /**
     * ProxyExecutable implementation for GraalVM Polyglot compatibility
     */
    private class ExecuteToolProxy implements ProxyExecutable {
        @Override
        public Object execute(Value... arguments) {
            if (arguments.length < 1) {
                throw new IllegalArgumentException("executeToolViaJava requires at least 1 argument: toolName");
            }
            
            String toolName = arguments[0].asString();
            Object jsArgs = arguments.length > 1 ? arguments[1] : null;
            
            return executeToolFromJS(toolName, jsArgs);
        }
    }

    /**
     * Expose a single MCP tool to JavaScript context using generated executor
     */
    private void exposeToolToJS(String toolName) {
        // Create a JavaScript function that calls the generated MCP executor
        String jsFunction = String.format("""
            function %s(argsObj) {
                // Handle both object parameter and individual arguments
                var args = {};
                if (typeof argsObj === 'object' && argsObj !== null) {
                    // Object parameter - use directly
                    args = argsObj;
                } else if (arguments.length > 0) {
                    // Individual arguments - map to object
                    if ('%s'.includes('ai_chat')) {
                        args.message = arguments[0];
                    } else {
                        // For other tools, create object from first argument
                        args = arguments[0] || {};
                    }
                }
                console.log('Calling tool %s with args:', JSON.stringify(args));
                return executeToolViaJava('%s', args);
            }
            """, toolName, toolName, toolName, toolName);
            
        try {
            jsContext.eval("js", jsFunction);
            logger.debug("Exposed MCP tool {} to JavaScript", toolName);
        } catch (Exception e) {
            logger.error("Failed to expose tool {} to JavaScript", toolName, e);
        }
    }

    /**
     * Execute JavaScript code with dynamic JSON parameters
     */
    public Object executeJavaScript(String jsSourceOrPath, JSONObject parameters) throws Exception {
        try {
            String jsCode = loadJavaScriptCode(jsSourceOrPath);
            
            // Make the tool executor available to JavaScript using ProxyExecutable
            jsContext.getBindings("js").putMember("executeToolViaJava", new ExecuteToolProxy());
            
            // Evaluate the JavaScript code
            jsContext.eval("js", jsCode);
            
            // Get the action function
            Value actionFunction = jsContext.getBindings("js").getMember("action");
            if (actionFunction == null || !actionFunction.canExecute()) {
                throw new IllegalArgumentException("JavaScript code must define an 'action' function");
            }
            
            // Convert JSONObject to JavaScript-compatible object
            Object jsCompatibleParams = convertToJSCompatible(parameters);
            
            // Execute the function with proper parameter passing
            Value result = actionFunction.execute(jsCompatibleParams);
            
            // Convert result back to Java object
            return convertJSResultToJava(result);
            
        } catch (Exception e) {
            logger.error("JavaScript execution failed for source: {}", 
                         jsSourceOrPath.length() > 100 ? jsSourceOrPath.substring(0, 100) + "..." : jsSourceOrPath, e);
            throw new RuntimeException("JavaScript execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Convert JavaScript result to Java object
     */
    private Object convertJSResultToJava(Value result) {
        if (result.isNull()) {
            return null;
        } else if (result.isString()) {
            return result.asString();
        } else if (result.isNumber()) {
            return result.asDouble();
        } else if (result.isBoolean()) {
            return result.asBoolean();
        } else {
            return result.toString();
        }
    }

    /**
     * Load JavaScript code from inline string, resource file, or GitHub URL
     */
    private String loadJavaScriptCode(String jsSourceOrPath) throws IOException {
        // GitHub URL support
        if (jsSourceOrPath.startsWith("http://github.com/") || jsSourceOrPath.startsWith("https://github.com/")) {
            return loadFromGitHub(jsSourceOrPath);
        }
        
        // Better detection: check if it's actually JavaScript code
        if (jsSourceOrPath.trim().startsWith("function") || jsSourceOrPath.contains("action")) {
            return jsSourceOrPath; // Inline JavaScript code
        }
        
        // If it contains "/" or ends with ".js", treat as resource path
        if (jsSourceOrPath.contains("/") || jsSourceOrPath.endsWith(".js")) {
            return loadFromResources(jsSourceOrPath);
        } else {
            return jsSourceOrPath; // Fallback to inline
        }
    }

    /**
     * Load JavaScript from GitHub URL with caching
     */
    private String loadFromGitHub(String githubUrl) throws IOException {
        return resourceCache.computeIfAbsent(githubUrl, url -> {
            try {
                if (sourceCode == null) {
                    throw new RuntimeException("SourceCode not configured - cannot load from GitHub");
                }
                logger.info("Loading JavaScript from GitHub: {}", url);
                String content = sourceCode.getFileContent(url);
                logger.debug("Successfully loaded {} characters from GitHub", content.length());
                return content;
            } catch (IOException e) {
                throw new RuntimeException("Failed to load JS from GitHub: " + url, e);
            }
        });
    }

    /**
     * Load JavaScript from classpath resources with caching
     */
    private String loadFromResources(String resourcePath) throws IOException {
        return resourceCache.computeIfAbsent(resourcePath, path -> {
            try (InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(path)) {
                if (inputStream == null) {
                    throw new RuntimeException("Resource not found: " + path);
                }
                logger.debug("Loading JavaScript from resource: {}", path);
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load JS resource: " + path, e);
            }
        });
    }

    /**
     * Convert JSONObject to JavaScript-compatible object using GraalVM's Value system
     */
    private Object convertToJSCompatible(JSONObject jsonObject) {
        // Convert JSONObject to a simple string and parse it in JavaScript
        // This ensures proper conversion to JavaScript object
        String jsonString = jsonObject.toString();
        return jsContext.eval("js", "(" + jsonString + ")");
    }

    /**
     * Convert JSONObject to Map recursively (backup method)
     */
    private Map<String, Object> convertJSONObjectToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.put(key, convertJSONObjectToMap((JSONObject) value));
            } else if (value instanceof org.json.JSONArray) {
                map.put(key, convertJSONArrayToList((org.json.JSONArray) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }
    
    /**
     * Convert JSONArray to List recursively
     */
    private java.util.List<Object> convertJSONArrayToList(org.json.JSONArray jsonArray) {
        java.util.List<Object> list = new java.util.ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                list.add(convertJSONObjectToMap((JSONObject) value));
            } else if (value instanceof org.json.JSONArray) {
                list.add(convertJSONArrayToList((org.json.JSONArray) value));
            } else {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * Clean up resources
     */
    public void close() {
        if (jsContext != null) {
            jsContext.close();
        }
    }
}
