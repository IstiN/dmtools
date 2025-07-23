package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.google.BasicGeminiAI;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.config.ApplicationConfiguration;
import com.github.istin.dmtools.common.config.InMemoryConfiguration;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.context.UriToObjectFactory;
import com.github.istin.dmtools.di.SourceCodeFactory;
import com.github.istin.dmtools.logging.CallbackLogger;
import com.github.istin.dmtools.logging.LogCallback;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.OpenAIClient;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import dagger.Module;
import dagger.Provides;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.inject.Singleton;
import java.io.IOException;

/**
 * Dagger module for server-managed job execution with pre-resolved integrations.
 * This module creates dependencies from credentials that have been resolved by the server.
 * Enhanced to support callback logger injection for execution monitoring.
 */
@Module
public class ServerManagedIntegrationsModule {
    
    private final JSONObject resolvedIntegrations;
    private final String executionId;
    private final LogCallback logCallback;
    
    // Original constructor for backward compatibility
    public ServerManagedIntegrationsModule(JSONObject resolvedIntegrations) {
        this(resolvedIntegrations, null, null);
    }
    
    // Enhanced constructor with callback logger support
    public ServerManagedIntegrationsModule(JSONObject resolvedIntegrations, String executionId, LogCallback logCallback) {
        this.resolvedIntegrations = resolvedIntegrations;
        this.executionId = executionId;
        this.logCallback = logCallback;
    }
    
    @Provides
    @Singleton
    ApplicationConfiguration provideConfiguration() {
        // Create an in-memory configuration from resolved integrations
        InMemoryConfiguration config = new InMemoryConfiguration();
        
        // Map resolved integrations to configuration properties using correct property names
        if (resolvedIntegrations.has("jira")) {
            JSONObject jiraConfig = resolvedIntegrations.getJSONObject("jira");
            if (jiraConfig.has("url")) {
                config.setProperty("JIRA_BASE_PATH", jiraConfig.getString("url"));
            }
            if (jiraConfig.has("token")) {
                config.setProperty("JIRA_LOGIN_PASS_TOKEN", jiraConfig.getString("token"));
            }
            if (jiraConfig.has("authType")) {
                config.setProperty("JIRA_AUTH_TYPE", jiraConfig.getString("authType"));
            }
            if (jiraConfig.has("username")) {
                config.setProperty("JIRA_USERNAME", jiraConfig.getString("username"));
            }
        }
        
        if (resolvedIntegrations.has("confluence")) {
            JSONObject confluenceConfig = resolvedIntegrations.getJSONObject("confluence");
            if (confluenceConfig.has("url")) {
                config.setProperty("CONFLUENCE_BASE_PATH", confluenceConfig.getString("url"));
            }
            if (confluenceConfig.has("token")) {
                config.setProperty("CONFLUENCE_LOGIN_PASS_TOKEN", confluenceConfig.getString("token"));
            }
            if (confluenceConfig.has("defaultSpace")) {
                config.setProperty("CONFLUENCE_DEFAULT_SPACE", confluenceConfig.getString("defaultSpace"));
            }
        }
        
        if (resolvedIntegrations.has("openai")) {
            JSONObject openaiConfig = resolvedIntegrations.getJSONObject("openai");
            if (openaiConfig.has("apiKey")) {
                config.setProperty("OPEN_AI_API_KEY", openaiConfig.getString("apiKey"));
            }
            if (openaiConfig.has("model")) {
                config.setProperty("OPEN_AI_MODEL", openaiConfig.getString("model"));
            }
            if (openaiConfig.has("basePath")) {
                config.setProperty("OPEN_AI_BATH_PATH", openaiConfig.getString("basePath"));
            }
        }
        
        if (resolvedIntegrations.has("gemini")) {
            JSONObject geminiConfig = resolvedIntegrations.getJSONObject("gemini");
            if (geminiConfig.has("apiKey")) {
                config.setProperty("GEMINI_API_KEY", geminiConfig.getString("apiKey"));
            }
            if (geminiConfig.has("model")) {
                config.setProperty("GEMINI_DEFAULT_MODEL", geminiConfig.getString("model"));
            }
            if (geminiConfig.has("basePath")) {
                config.setProperty("GEMINI_BASE_PATH", geminiConfig.getString("basePath"));
            }
        }
        
        return config;
    }
    
    @Provides
    @Singleton
    TrackerClient<? extends ITicket> provideTrackerClient() {
        try {
            // Create a custom JiraClient instance using resolved credentials
            if (resolvedIntegrations.has("jira")) {
                JSONObject jiraConfig = resolvedIntegrations.getJSONObject("jira");
                String basePath = jiraConfig.optString("url", "");
                String token = jiraConfig.optString("token", "");
                String authType = jiraConfig.optString("authType", "Basic");
                
                if (!basePath.isEmpty() && !token.isEmpty()) {
                    // Create a custom JiraClient that replicates BasicJiraClient functionality
                    // but uses resolved credentials instead of static properties
                    // Create a custom JiraClient that replicates BasicJiraClient functionality
                    // but uses resolved credentials instead of static properties
                    return new CustomServerManagedJiraClient(basePath, token, authType);
                }
            }
            
            // Fallback to default instance
            return new BasicJiraClient();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create TrackerClient instance with resolved credentials", e);
        }
    }
    
    /**
     * Custom JiraClient implementation that replicates BasicJiraClient functionality
     * but uses resolved credentials instead of static properties from PropertyReader
     */
    private static class CustomServerManagedJiraClient extends JiraClient<com.github.istin.dmtools.atlassian.jira.model.Ticket> {
        private final String[] defaultJiraFields;
        private final String[] extendedJiraFields;
        
        public CustomServerManagedJiraClient(String basePath, String token, String authType) throws IOException {
            // Call parent constructor with resolved credentials
            super(basePath, token);
            
            // Set auth type if provided
            if (authType != null) {
                setAuthType(authType);
            }
            
            // Replicate BasicJiraClient configuration with reasonable defaults
            setLogEnabled(true);
            setWaitBeforePerform(false);
            setSleepTimeRequest(0L);
            setClearCache(false);
            
            // Initialize field arrays exactly like BasicJiraClient
            java.util.List<String> defaultFields = new java.util.ArrayList<>();
            defaultFields.addAll(java.util.Arrays.asList(com.github.istin.dmtools.atlassian.jira.BasicJiraClient.DEFAULT_QUERY_FIELDS));
            defaultJiraFields = defaultFields.toArray(new String[0]);
            
            java.util.List<String> extendedFields = new java.util.ArrayList<>();
            extendedFields.addAll(java.util.Arrays.asList(com.github.istin.dmtools.atlassian.jira.BasicJiraClient.EXTENDED_QUERY_FIELDS));
            extendedFields.addAll(defaultFields);
            extendedJiraFields = extendedFields.toArray(new String[0]);
        }
        
        @Override
        public String getTextFieldsOnly(com.github.istin.dmtools.common.model.ITicket ticket) {
            StringBuilder ticketDescription = null;
            try {
                ticketDescription = new StringBuilder(ticket.getTicketTitle());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ticketDescription.append("\n").append(ticket.getTicketDescription());
            return ticketDescription.toString();
        }
        
        @Override
        public void deleteCommentIfExists(String ticketKey, String comment) throws IOException {
            // Empty implementation like BasicJiraClient
        }
        
        @Override
        public String[] getDefaultQueryFields() {
            return defaultJiraFields;
        }
        
        @Override
        public String[] getExtendedQueryFields() {
            return extendedJiraFields;
        }
        
        @Override
        public java.util.List<? extends com.github.istin.dmtools.common.model.ITicket> getTestCases(com.github.istin.dmtools.common.model.ITicket ticket) throws IOException {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public com.github.istin.dmtools.common.tracker.TrackerClient.TextType getTextType() {
            return com.github.istin.dmtools.common.tracker.TrackerClient.TextType.MARKDOWN;
        }
    }
    
    @Provides
    @Singleton
    Confluence provideConfluence() {
        try {
            System.out.println("🔧 [ServerManagedIntegrationsModule] Providing Confluence integration...");
            
            if (resolvedIntegrations == null) {
                System.out.println("⚠️ [ServerManagedIntegrationsModule] No resolved integrations found, falling back to BasicConfluence");
                return BasicConfluence.getInstance();
            }
            
            if (!resolvedIntegrations.has("confluence")) {
                System.out.println("⚠️ [ServerManagedIntegrationsModule] No Confluence integration found in resolved integrations, falling back to BasicConfluence");
                return BasicConfluence.getInstance();
            }
            
            JSONObject confluenceConfig = resolvedIntegrations.getJSONObject("confluence");
            System.out.println("🔧 [ServerManagedIntegrationsModule] Found Confluence configuration: " + confluenceConfig.length() + " parameters");
            
            // Log configuration details (without sensitive data)
            for (String key : confluenceConfig.keySet()) {
                Object value = confluenceConfig.get(key);
                System.out.println("  📋 Confluence config: " + key + "=" + 
                    (key.toLowerCase().contains("token") || key.toLowerCase().contains("password") ? "[SENSITIVE]" : value));
            }
            
            String url = confluenceConfig.optString("url", null);
            String token = confluenceConfig.optString("token", null);
            String defaultSpace = confluenceConfig.optString("defaultSpace", null);
            
            if (url == null || token == null) {
                System.err.println("❌ [ServerManagedIntegrationsModule] Confluence configuration missing required parameters (url=" + 
                    (url != null ? url : "null") + ", token=" + (token != null ? "[SENSITIVE]" : "null") + ")");
                return BasicConfluence.getInstance();
            }
            
            // Ensure URL ends with /wiki for proper URL parsing by parseUris method
            if (!url.endsWith("/wiki")) {
                url = url + "/wiki";
            }
            
            System.out.println("✅ [ServerManagedIntegrationsModule] Creating CustomServerManagedConfluence with url=" + url + 
                ", defaultSpace=" + (defaultSpace != null ? defaultSpace : "null") +
                ", executionTracking=" + (executionId != null && logCallback != null ? "enabled" : "disabled"));
            
            return new CustomServerManagedConfluence(url, token, defaultSpace, executionId, logCallback);
            
        } catch (Exception e) {
            System.err.println("❌ [ServerManagedIntegrationsModule] Failed to provide Confluence integration: " + e.getMessage());
            e.printStackTrace();
            try {
                return BasicConfluence.getInstance();
            } catch (Exception fallbackException) {
                System.err.println("❌ [ServerManagedIntegrationsModule] Fallback to BasicConfluence also failed: " + fallbackException.getMessage());
                return null;
            }
        }
    }
    
    /**
     * Custom Confluence implementation that replicates BasicConfluence functionality
     * but uses resolved credentials instead of static properties from PropertyReader
     */
    private static class CustomServerManagedConfluence extends Confluence {
        private final String defaultSpace;
        
        private final CallbackLogger callbackLogger;
        
        public CustomServerManagedConfluence(String basePath, String token, String defaultSpace) throws IOException {
            this(basePath, token, defaultSpace, null, null);
        }
        
        public CustomServerManagedConfluence(String basePath, String token, String defaultSpace, String executionId, LogCallback logCallback) throws IOException {
            super(basePath, token);
            this.defaultSpace = defaultSpace;
            this.callbackLogger = (executionId != null && logCallback != null) ? 
                new CallbackLogger(Confluence.class, executionId, logCallback) : null;
            System.out.println("🔧 [CustomServerManagedConfluence] Initialized with basePath=" + basePath + 
                ", defaultSpace=" + (defaultSpace != null ? defaultSpace : "null") +
                ", callbackLogging=" + (callbackLogger != null ? "enabled" : "disabled"));
        }
        
        public String getDefaultSpace() {
            return defaultSpace;
        }
        
        // Add BasicConfluence-like methods if needed
        public com.github.istin.dmtools.atlassian.confluence.model.Content findOrCreate(String title, String parentId, String body) throws IOException {
            com.github.istin.dmtools.atlassian.confluence.model.Content content = findContent(title, defaultSpace);
            if (content == null) {
                content = createPage(title, parentId, body, defaultSpace);
            }
            return content;
        }
        
        public com.github.istin.dmtools.atlassian.confluence.model.Content findContent(String title) throws IOException {
            return findContent(title, defaultSpace);
        }
        
        public com.github.istin.dmtools.atlassian.confluence.model.ContentResult content(String title) throws IOException {
            return content(title, defaultSpace);
        }
    }
    
    @Provides
    @Singleton
    ConversationObserver provideConversationObserver() {
        return new ConversationObserver();
    }
    
    @Provides
    @Singleton
    AI provideAI(ConversationObserver observer, ApplicationConfiguration configuration) {
        try {
            System.out.println("🤖 [ServerManagedIntegrationsModule] Providing AI integration...");
            
            if (resolvedIntegrations == null) {
                System.out.println("⚠️ [ServerManagedIntegrationsModule] No resolved integrations found, falling back to BasicOpenAI");
                return new BasicOpenAI(observer, configuration);
            }
            
            // Check available AI integrations in priority order
            System.out.println("🔍 [ServerManagedIntegrationsModule] Checking available AI integrations...");
            
            // First check for Gemini
            if (resolvedIntegrations.has("gemini")) {
                JSONObject geminiConfig = resolvedIntegrations.getJSONObject("gemini");
                System.out.println("🔍 [ServerManagedIntegrationsModule] Found Gemini configuration: " + geminiConfig.length() + " parameters");
                
                String apiKey = geminiConfig.optString("apiKey", null);
                if (apiKey != null && !apiKey.isEmpty()) {
                    System.out.println("🤖 Using resolved Gemini integration for AI provider");
                    return BasicGeminiAI.create(observer, configuration);
                } else {
                    System.out.println("⚠️ [ServerManagedIntegrationsModule] Gemini configuration missing apiKey, skipping");
                }
            }
            
            // Then check for OpenAI
            if (resolvedIntegrations.has("openai")) {
                JSONObject openaiConfig = resolvedIntegrations.getJSONObject("openai");
                System.out.println("🔍 [ServerManagedIntegrationsModule] Found OpenAI configuration: " + openaiConfig.length() + " parameters");
                
                String apiKey = openaiConfig.optString("apiKey", null);
                if (apiKey != null && !apiKey.isEmpty()) {
                    System.out.println("🤖 Using resolved OpenAI integration for AI provider");
                    return new BasicOpenAI(observer, configuration);
                } else {
                    System.out.println("⚠️ [ServerManagedIntegrationsModule] OpenAI configuration missing apiKey, skipping");
                }
            }
            
            System.out.println("⚠️ [ServerManagedIntegrationsModule] No valid AI integration found, falling back to BasicOpenAI");
            return new BasicOpenAI(observer, configuration);
            
        } catch (Exception e) {
            System.err.println("❌ [ServerManagedIntegrationsModule] Failed to provide AI integration: " + e.getMessage());
            e.printStackTrace();
            try {
                return new BasicOpenAI(observer, configuration);
            } catch (Exception fallbackException) {
                System.err.println("❌ [ServerManagedIntegrationsModule] Fallback to BasicOpenAI also failed: " + fallbackException.getMessage());
                return null;
            }
        }
    }
    
    @Provides
    @Singleton
    IPromptTemplateReader providePromptTemplateReader() {
        return new PromptManager();
    }

    @Provides
    @Singleton
    UriToObjectFactory provideUriToObjectFactory(TrackerClient<? extends ITicket> trackerClient, 
                                                 Confluence confluence, 
                                                 SourceCodeFactory sourceCodeFactory) {
        System.out.println("🔧 [ServerManagedIntegrationsModule] Creating UriToObjectFactory with server-managed integrations");
        return new UriToObjectFactory(trackerClient, confluence, sourceCodeFactory);
    }
} 