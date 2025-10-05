package com.github.istin.dmtools.common.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ApplicationConfiguration.
 * This implementation stores all configuration values in memory and can be modified at runtime.
 */
public class InMemoryConfiguration implements ApplicationConfiguration {
    
    private final Map<String, String> properties = new ConcurrentHashMap<>();
    private String configFilePath = "/config.properties";
    
    /**
     * Creates a new empty InMemoryConfiguration
     */
    public InMemoryConfiguration() {
        // Initialize with empty properties
    }
    
    /**
     * Creates a new InMemoryConfiguration with the provided properties
     * @param properties The initial properties
     */
    public InMemoryConfiguration(Map<String, String> properties) {
        this.properties.putAll(properties);
    }
    
    /**
     * Sets a property value
     * @param key The property key
     * @param value The property value
     * @return This configuration instance for chaining
     */
    public InMemoryConfiguration setProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }
    
    /**
     * Sets multiple property values
     * @param properties The properties to set
     * @return This configuration instance for chaining
     */
    public InMemoryConfiguration setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
        return this;
    }
    
    @Override
    public void setConfigFile(String resourcePath) {
        this.configFilePath = resourcePath;
    }
    
    @Override
    public String getValue(String propertyKey) {
        String value = properties.get(propertyKey);
        if (value == null || value.isEmpty()) {
            return System.getenv(propertyKey);
        }
        return value;
    }
    
    @Override
    public String getValue(String propertyKey, String defaultValue) {
        String value = getValue(propertyKey);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
    
    @Override
    public Map<String, String> getAllProperties() {
        return new ConcurrentHashMap<>(properties);
    }
    
    // JiraConfiguration
    
    @Override
    public String getJiraLoginPassToken() {
        // Priority 1: Use separate email and API token if both are available
        String email = getJiraEmail();
        String apiToken = getJiraApiToken();
        
        if (email != null && !email.trim().isEmpty() && 
            apiToken != null && !apiToken.trim().isEmpty()) {
            // Automatically combine email:token and base64 encode
            String credentials = email.trim() + ":" + apiToken.trim();
            return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        }
        
        // Priority 2: Fall back to existing base64-encoded token
        return getValue("JIRA_LOGIN_PASS_TOKEN");
    }
    
    @Override
    public String getJiraEmail() {
        return getValue("JIRA_EMAIL");
    }
    
    @Override
    public String getJiraApiToken() {
        return getValue("JIRA_API_TOKEN");
    }
    
    @Override
    public String getJiraBasePath() {
        return getValue("JIRA_BASE_PATH");
    }
    
    @Override
    public String getJiraAuthType() {
        return getValue("JIRA_AUTH_TYPE");
    }
    
    @Override
    public boolean isJiraWaitBeforePerform() {
        String value = getValue("JIRA_WAIT_BEFORE_PERFORM");
        return value != null && Boolean.parseBoolean(value);
    }
    
    @Override
    public boolean isJiraLoggingEnabled() {
        String value = getValue("JIRA_LOGGING_ENABLED");
        return value != null && Boolean.parseBoolean(value);
    }
    
    @Override
    public boolean isJiraClearCache() {
        String value = getValue("JIRA_CLEAR_CACHE");
        return value != null && Boolean.parseBoolean(value);
    }
    
    @Override
    public String getJiraExtraFieldsProject() {
        return getValue("JIRA_EXTRA_FIELDS_PROJECT");
    }
    
    @Override
    public String[] getJiraExtraFields() {
        String value = getValue("JIRA_EXTRA_FIELDS");
        if (value == null) {
            return null;
        }
        return value.split(",");
    }
    
    // AIConfiguration
    
    @Override
    public String getDialBathPath() {
        return getValue("DIAL_BATH_PATH");
    }
    
    @Override
    public String getDialApiKey() {
        return getValue("DIAL_API_KEY");
    }
    
    @Override
    public String getDialModel() {
        return getValue("DIAL_MODEL");
    }
    
    @Override
    public String getCodeAIModel() {
        return getValue("CODE_AI_MODEL");
    }
    
    @Override
    public String getTestAIModel() {
        return getValue("TEST_AI_MODEL");
    }
    
    @Override
    public int getAiRetryAmount() {
        String value = getValue("AI_RETRY_AMOUNT");
        if (value == null || value.trim().isEmpty()) {
            return 3; // Default value
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 3; // Default value
        }
    }
    
    @Override
    public long getAiRetryDelayStep() {
        String value = getValue("AI_RETRY_DELAY_STEP");
        if (value == null || value.trim().isEmpty()) {
            return 20000L; // Default value
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 20000L; // Default value
        }
    }
    
    @Override
    public int getPromptChunkTokenLimit() {
        String value = getValue("PROMPT_CHUNK_TOKEN_LIMIT");
        if (value == null || value.trim().isEmpty()) {
            return 4000; // Default value
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 4000; // Default value
        }
    }
    
    @Override
    public long getPromptChunkMaxSingleFileSize() {
        String value = getValue("PROMPT_CHUNK_MAX_SINGLE_FILE_SIZE_MB");
        if (value == null || value.trim().isEmpty()) {
            return 4 * 1024 * 1024; // Default value
        }
        try {
            return Long.parseLong(value) * 1024 * 1024;
        } catch (NumberFormatException e) {
            return 4 * 1024 * 1024; // Default value
        }
    }
    
    @Override
    public long getPromptChunkMaxTotalFilesSize() {
        String value = getValue("PROMPT_CHUNK_MAX_TOTAL_FILES_SIZE_MB");
        if (value == null || value.trim().isEmpty()) {
            return 4 * 1024 * 1024; // Default value
        }
        try {
            return Long.parseLong(value) * 1024 * 1024;
        } catch (NumberFormatException e) {
            return 4 * 1024 * 1024; // Default value
        }
    }
    
    @Override
    public int getPromptChunkMaxFiles() {
        String value = getValue("PROMPT_CHUNK_MAX_FILES");
        if (value == null || value.trim().isEmpty()) {
            return 10; // Default value
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 10; // Default value
        }
    }
    
    @Override
    public String getGeminiApiKey() {
        return getValue("GEMINI_API_KEY");
    }
    
    @Override
    public String getGeminiDefaultModel() {
        return getValue("GEMINI_DEFAULT_MODEL", "gemini-2.0-flash");
    }
    
    @Override
    public String getGeminiBasePath() {
        return getValue("GEMINI_BASE_PATH", "https://generativelanguage.googleapis.com/v1beta/models");
    }
    
    // JSAIConfiguration
    
    @Override
    public String getJsScriptPath() {
        return getValue("JSAI_SCRIPT_PATH");
    }
    
    @Override
    public String getJsScriptContent() {
        return getValue("JSAI_SCRIPT_CONTENT");
    }
    
    @Override
    public String getJsClientName() {
        String value = getValue("JSAI_CLIENT_NAME");
        return (value == null || value.trim().isEmpty()) ? "JSAIClientFromProperties" : value;
    }
    
    @Override
    public String getJsDefaultModel() {
        return getValue("JSAI_DEFAULT_MODEL");
    }
    
    @Override
    public String getJsBasePath() {
        return getValue("JSAI_BASE_PATH");
    }
    
    @Override
    public String[] getJsSecretsKeys() {
        String value = getValue("JSAI_SECRETS_KEYS");
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.split(",");
    }
    
    // SourceControlConfiguration
    
    @Override
    public String getGithubToken() {
        return getValue("SOURCE_GITHUB_TOKEN");
    }
    
    @Override
    public String getGithubWorkspace() {
        return getValue("SOURCE_GITHUB_WORKSPACE");
    }
    
    @Override
    public String getGithubRepository() {
        return getValue("SOURCE_GITHUB_REPOSITORY");
    }
    
    @Override
    public String getGithubBranch() {
        return getValue("SOURCE_GITHUB_BRANCH");
    }
    
    @Override
    public String getGithubBasePath() {
        return getValue("SOURCE_GITHUB_BASE_PATH");
    }
    
    @Override
    public String getGitLabToken() {
        return getValue("GITLAB_TOKEN");
    }
    
    @Override
    public String getGitLabWorkspace() {
        return getValue("GITLAB_WORKSPACE");
    }
    
    @Override
    public String getGitLabRepository() {
        return getValue("GITLAB_REPOSITORY");
    }
    
    @Override
    public String getGitLabBranch() {
        return getValue("GITLAB_BRANCH");
    }
    
    @Override
    public String getGitLabBasePath() {
        return getValue("GITLAB_BASE_PATH");
    }
    
    @Override
    public String getBitbucketToken() {
        return getValue("BITBUCKET_TOKEN");
    }
    
    @Override
    public String getBitbucketApiVersion() {
        return getValue("BITBUCKET_API_VERSION");
    }
    
    @Override
    public String getBitbucketWorkspace() {
        return getValue("BITBUCKET_WORKSPACE");
    }
    
    @Override
    public String getBitbucketRepository() {
        return getValue("BITBUCKET_REPOSITORY");
    }
    
    @Override
    public String getBitbucketBranch() {
        return getValue("BITBUCKET_BRANCH");
    }
    
    @Override
    public String getBitbucketBasePath() {
        return getValue("BITBUCKET_BASE_PATH");
    }
    
    @Override
    public boolean isReadPullRequestDiff() {
        String value = getValue("IS_READ_PULL_REQUEST_DIFF");
        return value == null || Boolean.parseBoolean(value);
    }
    
    // ConfluenceConfiguration
    
    @Override
    public String getConfluenceBasePath() {
        return getValue("CONFLUENCE_BASE_PATH");
    }
    
    @Override
    public String getConfluenceLoginPassToken() {
        // Priority 1: Use separate email and API token if both are available
        String email = getConfluenceEmail();
        String apiToken = getConfluenceApiToken();
        String authType = getConfluenceAuthType();
        
        if (email != null && !email.trim().isEmpty() && 
            apiToken != null && !apiToken.trim().isEmpty()) {
            
            // For Bearer auth, use token directly without email combination
            if ("Bearer".equalsIgnoreCase(authType)) {
                return apiToken.trim();
            }
            
            // For Basic auth (default), combine email:token and base64 encode
            String credentials = email.trim() + ":" + apiToken.trim();
            return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        }
        
        // Priority 2: Fall back to existing base64-encoded token
        return getValue("CONFLUENCE_LOGIN_PASS_TOKEN");
    }
    
    @Override
    public String getConfluenceEmail() {
        return getValue("CONFLUENCE_EMAIL");
    }
    
    @Override
    public String getConfluenceApiToken() {
        return getValue("CONFLUENCE_API_TOKEN");
    }
    
    @Override
    public String getConfluenceAuthType() {
        String authType = getValue("CONFLUENCE_AUTH_TYPE");
        // Default to Basic if not specified
        return authType != null ? authType : "Basic";
    }
    
    @Override
    public String getConfluenceGraphQLPath() {
        return getValue("CONFLUENCE_GRAPHQL_PATH");
    }
    
    @Override
    public String getConfluenceDefaultSpace() {
        return getValue("CONFLUENCE_DEFAULT_SPACE");
    }
    
    // MiscConfiguration
    
    @Override
    public Long getSleepTimeRequest() {
        String value = getValue("SLEEP_TIME_REQUEST");
        if (value == null) {
            return 300L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 300L;
        }
    }
    
    @Override
    public String getRallyToken() {
        return getValue("RALLY_TOKEN");
    }
    
    @Override
    public String getRallyPath() {
        return getValue("RALLY_PATH");
    }
    
    @Override
    public String getFigmaBasePath() {
        return getValue("FIGMA_BASE_PATH");
    }
    
    @Override
    public String getFigmaApiKey() {
        return getValue("FIGMA_TOKEN");
    }
    
    @Override
    public Integer getDefaultTicketWeightIfNoSPs() {
        String value = getValue("DEFAULT_TICKET_WEIGHT_IF_NO_SP");
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    @Override
    public Double getLinesOfCodeDivider() {
        String value = getValue("LINES_OF_CODE_DIVIDER");
        if (value == null) {
            return 1.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }
    
    @Override
    public Double getTimeSpentOnDivider() {
        String value = getValue("TIME_SPENT_ON_DIVIDER");
        if (value == null) {
            return 1.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }
    
    @Override
    public Double getTicketFieldsChangedDivider(String fieldName) {
        String value = getValue("TICKET_FIELDS_CHANGED_DIVIDER_" + fieldName.toUpperCase());
        if (value == null) {
            String defaultValue = getValue("TICKET_FIELDS_CHANGED_DIVIDER_DEFAULT");
            if (defaultValue != null) {
                try {
                    return Double.parseDouble(defaultValue);
                } catch (NumberFormatException e) {
                    return 1.0;
                }
            }
            return 1.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }
    
}