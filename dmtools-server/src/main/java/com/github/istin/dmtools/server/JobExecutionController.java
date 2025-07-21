package com.github.istin.dmtools.server;

import com.github.istin.dmtools.dto.JobTypeDto;
import com.github.istin.dmtools.dto.ExecuteJobConfigurationRequest;
import com.github.istin.dmtools.dto.ExecutionParametersDto;
import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.IntegrationConfigDto;
import com.github.istin.dmtools.server.service.JobConfigurationService;
import com.github.istin.dmtools.job.ExecutionMode;
import com.github.istin.dmtools.job.JobParams;
import com.github.istin.dmtools.job.JobRunner;
import com.github.istin.dmtools.server.service.JobConfigurationLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for executing jobs in server-managed mode with integrated credential resolution.
 * This controller handles the hybrid job execution system where the server resolves all integrations
 * and credentials before passing them to dmtools-core for execution.
 */
@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Job Execution", description = "API for server-managed job execution with integrated credential resolution")
public class JobExecutionController {

    private static final Logger logger = LogManager.getLogger(JobExecutionController.class);

    @Autowired
    private IntegrationResolutionService integrationResolutionService;
    
    @Autowired
    private com.github.istin.dmtools.auth.service.IntegrationService integrationService;
    
    @Autowired
    private JobConfigurationLoader jobConfigurationLoader;
    
    @Autowired
    private JobConfigurationService jobConfigurationService;
    
    @Autowired
    private UserService userService;

    private String getUserId(Authentication authentication) {
        // Handle PlaceholderAuthentication during OAuth flow
        if (authentication instanceof com.github.istin.dmtools.auth.PlaceholderAuthentication) {
            throw new IllegalArgumentException("Authentication still in progress, cannot extract user ID");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            return ((OAuth2User) principal).getAttribute("sub");
        } else if (principal instanceof UserDetails) {
            // For JWT authentication, the username is the email
            // We need to find the user by email and return the user ID
            String email = ((UserDetails) principal).getUsername();
            return userService.findByEmail(email)
                    .map(user -> user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
        } else if (principal instanceof String) {
            String principalStr = (String) principal;
            // Check if it's a placeholder string from PlaceholderAuthentication
            if (principalStr.startsWith("placeholder_")) {
                throw new IllegalArgumentException("Authentication still in progress, cannot extract user ID");
            }
            return principalStr;
        }
        return authentication.getName();
    }

    /**
     * Resolves integration IDs from the database to JSONObject format for job execution.
     * 
     * @param integrationIds List of integration IDs to resolve
     * @param userId The user ID for access control
     * @return JSONObject containing resolved integration configurations
     */
    private JSONObject resolveIntegrationIds(List<String> integrationIds, String userId) {
        JSONObject resolved = new JSONObject();
        
        for (String integrationId : integrationIds) {
            try {
                logger.info("🔍 Resolving integration ID: {}", integrationId);
                
                // Get integration configuration from database with sensitive data
                var integrationDto = integrationService.getIntegrationById(integrationId, userId, true);
                
                // Log detailed config parameters for debugging
                logger.info("🔧 Integration '{}' (type: {}) has {} config parameters:", 
                    integrationId, integrationDto.getType(), 
                    integrationDto.getConfigParams() != null ? integrationDto.getConfigParams().size() : 0);
                
                if (integrationDto.getConfigParams() != null) {
                    for (var param : integrationDto.getConfigParams()) {
                        logger.info("  📋 Parameter: {}={}", param.getParamKey(), 
                            param.isSensitive() ? "[SENSITIVE]" : param.getParamValue());
                    }
                }
                
                // Convert to JSONObject format expected by job execution based on integration type
                JSONObject integrationConfig = mapIntegrationConfig(integrationDto);
                
                // Log the final mapped configuration
                logger.info("🎯 Final mapped config for integration '{}' (type: {}): {}", 
                    integrationId, integrationDto.getType(), 
                    integrationConfig.keySet().stream()
                        .map(key -> key + "=" + (key.toLowerCase().contains("key") || key.toLowerCase().contains("token") || key.toLowerCase().contains("password") ? "[SENSITIVE]" : integrationConfig.get(key)))
                        .toList());
                
                // Use integration type as key for job execution compatibility
                resolved.put(integrationDto.getType(), integrationConfig);
                
                logger.info("✅ Successfully resolved integration ID '{}' as type '{}' with {} config parameters", 
                    integrationId, integrationDto.getType(), integrationConfig.length());
                
                // Record usage
                integrationService.recordIntegrationUsage(integrationId);
                
            } catch (Exception e) {
                logger.error("❌ Failed to resolve integration ID '{}': {}", integrationId, e.getMessage(), e);
                // Continue with other integrations even if one fails
            }
        }
        
        return resolved;
    }
    
    /**
     * Maps integration configuration from database format to job execution format.
     * 
     * @param integrationDto The integration configuration from database
     * @return JSONObject in the format expected by job execution system
     */
    private JSONObject mapIntegrationConfig(IntegrationDto integrationDto) {
        JSONObject config = new JSONObject();
        
        // Create a map of config parameters for easy lookup
        java.util.Map<String, String> params = new java.util.HashMap<>();
        if (integrationDto.getConfigParams() != null) {
            for (IntegrationConfigDto param : integrationDto.getConfigParams()) {
                params.put(param.getParamKey(), param.getParamValue());
            }
        }
        
        logger.info("🔧 Mapping integration config for type '{}' with parameters: {}", 
            integrationDto.getType(), params.keySet());
        
        // Map based on integration type
        switch (integrationDto.getType().toLowerCase()) {
            case "tracker":
            case "jira":
                logger.info("🔍 Processing JIRA integration mapping...");
                // Map database parameters to expected JIRA format
                if (params.containsKey("url")) {
                    config.put("url", params.get("url"));
                    logger.info("  ✅ Mapped 'url' parameter: {}", params.get("url"));
                } else if (params.containsKey("basePath")) {
                    config.put("url", params.get("basePath"));
                    logger.info("  ✅ Mapped 'basePath' to 'url': {}", params.get("basePath"));
                } else if (params.containsKey("baseUrl")) {
                    config.put("url", params.get("baseUrl"));
                    logger.info("  ✅ Mapped 'baseUrl' to 'url': {}", params.get("baseUrl"));
                } else if (params.containsKey("JIRA_BASE_PATH")) {
                    config.put("url", params.get("JIRA_BASE_PATH"));
                    logger.info("  ✅ Mapped 'JIRA_BASE_PATH' to 'url': {}", params.get("JIRA_BASE_PATH"));
                } else {
                    logger.warn("  ⚠️  No URL parameter found for JIRA integration");
                }
                
                // Priority 1: Use separate email and API token if both are available
                if (params.containsKey("JIRA_EMAIL") && params.containsKey("JIRA_API_TOKEN")) {
                    String email = params.get("JIRA_EMAIL");
                    String apiToken = params.get("JIRA_API_TOKEN");
                    if (email != null && !email.trim().isEmpty() && 
                        apiToken != null && !apiToken.trim().isEmpty()) {
                        // Automatically combine email:token and base64 encode
                        String credentials = email.trim() + ":" + apiToken.trim();
                        String encodedToken = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                        config.put("token", encodedToken);
                        logger.info("  ✅ Mapped 'JIRA_EMAIL' + 'JIRA_API_TOKEN' to auto-encoded token: [SENSITIVE]");
                    }
                } 
                // Priority 2: Use legacy token methods
                else if (params.containsKey("token")) {
                    config.put("token", params.get("token"));
                    logger.info("  ✅ Mapped 'token' parameter: [SENSITIVE]");
                } else if (params.containsKey("password")) {
                    config.put("token", params.get("password"));
                    logger.info("  ✅ Mapped 'password' to 'token': [SENSITIVE]");
                } else if (params.containsKey("JIRA_LOGIN_PASS_TOKEN")) {
                    config.put("token", params.get("JIRA_LOGIN_PASS_TOKEN"));
                    logger.info("  ✅ Mapped 'JIRA_LOGIN_PASS_TOKEN' to 'token': [SENSITIVE]");
                } else {
                    logger.warn("  ⚠️  No authentication parameters found for JIRA integration");
                }
                
                if (params.containsKey("authType")) {
                    config.put("authType", params.get("authType"));
                    logger.info("  ✅ Mapped 'authType' parameter: {}", params.get("authType"));
                } else if (params.containsKey("JIRA_AUTH_TYPE")) {
                    config.put("authType", params.get("JIRA_AUTH_TYPE"));
                    logger.info("  ✅ Mapped 'JIRA_AUTH_TYPE' to 'authType': {}", params.get("JIRA_AUTH_TYPE"));
                } else {
                    config.put("authType", "token"); // Default to token auth
                    logger.info("  ✅ Set default 'authType': token");
                }
                
                if (params.containsKey("username")) {
                    config.put("username", params.get("username"));
                    logger.info("  ✅ Mapped 'username' parameter: {}", params.get("username"));
                } else if (params.containsKey("JIRA_USERNAME")) {
                    config.put("username", params.get("JIRA_USERNAME"));
                    logger.info("  ✅ Mapped 'JIRA_USERNAME' to 'username': {}", params.get("JIRA_USERNAME"));
                }
                break;
                
            case "wiki":
            case "confluence":
                logger.info("🔍 Processing Confluence integration mapping...");
                if (params.containsKey("url")) {
                    config.put("url", params.get("url"));
                    logger.info("  ✅ Mapped 'url' parameter: {}", params.get("url"));
                } else if (params.containsKey("basePath")) {
                    config.put("url", params.get("basePath"));
                    logger.info("  ✅ Mapped 'basePath' to 'url': {}", params.get("basePath"));
                } else if (params.containsKey("CONFLUENCE_BASE_PATH")) {
                    config.put("url", params.get("CONFLUENCE_BASE_PATH"));
                    logger.info("  ✅ Mapped 'CONFLUENCE_BASE_PATH' to 'url': {}", params.get("CONFLUENCE_BASE_PATH"));
                } else {
                    logger.warn("  ⚠️  No URL parameter found for Confluence integration");
                }
                
                // Priority 1: Use separate email and API token if both are available
                if (params.containsKey("CONFLUENCE_EMAIL") && params.containsKey("CONFLUENCE_API_TOKEN")) {
                    String email = params.get("CONFLUENCE_EMAIL");
                    String apiToken = params.get("CONFLUENCE_API_TOKEN");
                    String authType = params.getOrDefault("CONFLUENCE_AUTH_TYPE", "Basic");
                    
                    if (email != null && !email.trim().isEmpty() && 
                        apiToken != null && !apiToken.trim().isEmpty()) {
                        
                        // For Bearer auth, use token directly without email combination
                        if ("Bearer".equalsIgnoreCase(authType)) {
                            config.put("token", apiToken.trim());
                            config.put("authType", "Bearer");
                            logger.info("  ✅ Mapped 'CONFLUENCE_EMAIL' + 'CONFLUENCE_API_TOKEN' to Bearer token: [SENSITIVE]");
                        } else {
                            // For Basic auth (default), combine email:token and base64 encode
                            String credentials = email.trim() + ":" + apiToken.trim();
                            String encodedToken = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                            config.put("token", encodedToken);
                            config.put("authType", "Basic");
                            logger.info("  ✅ Mapped 'CONFLUENCE_EMAIL' + 'CONFLUENCE_API_TOKEN' to auto-encoded Basic token: [SENSITIVE]");
                        }
                    }
                } 
                // Priority 2: Use legacy token methods
                else if (params.containsKey("token")) {
                    config.put("token", params.get("token"));
                    logger.info("  ✅ Mapped 'token' parameter: [SENSITIVE]");
                } else if (params.containsKey("CONFLUENCE_LOGIN_PASS_TOKEN")) {
                    config.put("token", params.get("CONFLUENCE_LOGIN_PASS_TOKEN"));
                    logger.info("  ✅ Mapped 'CONFLUENCE_LOGIN_PASS_TOKEN' to 'token': [SENSITIVE]");
                } else {
                    logger.warn("  ⚠️  No authentication parameters found for Confluence integration");
                }
                
                // Handle auth type if provided separately
                if (params.containsKey("CONFLUENCE_AUTH_TYPE") && !config.has("authType")) {
                    config.put("authType", params.get("CONFLUENCE_AUTH_TYPE"));
                    logger.info("  ✅ Mapped 'CONFLUENCE_AUTH_TYPE' to 'authType': {}", params.get("CONFLUENCE_AUTH_TYPE"));
                }
                
                if (params.containsKey("defaultSpace")) {
                    config.put("defaultSpace", params.get("defaultSpace"));
                    logger.info("  ✅ Mapped 'defaultSpace' parameter: {}", params.get("defaultSpace"));
                } else if (params.containsKey("CONFLUENCE_DEFAULT_SPACE")) {
                    config.put("defaultSpace", params.get("CONFLUENCE_DEFAULT_SPACE"));
                    logger.info("  ✅ Mapped 'CONFLUENCE_DEFAULT_SPACE' to 'defaultSpace': {}", params.get("CONFLUENCE_DEFAULT_SPACE"));
                } else {
                    logger.warn("  ⚠️  No defaultSpace parameter found for Confluence integration");
                }
                break;
                
            case "ai":
            case "openai":
                logger.info("🔍 Processing OpenAI integration mapping...");
                if (params.containsKey("apiKey")) {
                    config.put("apiKey", params.get("apiKey"));
                    logger.info("  ✅ Mapped 'apiKey' parameter: [SENSITIVE]");
                } else {
                    logger.warn("  ⚠️  No apiKey parameter found for OpenAI integration");
                }
                
                if (params.containsKey("model")) {
                    config.put("model", params.get("model"));
                    logger.info("  ✅ Mapped 'model' parameter: {}", params.get("model"));
                }
                
                if (params.containsKey("basePath")) {
                    config.put("basePath", params.get("basePath"));
                    logger.info("  ✅ Mapped 'basePath' parameter: {}", params.get("basePath"));
                }
                break;
                
            case "gemini":
                logger.info("🔍 Processing Gemini integration mapping...");
                if (params.containsKey("apiKey")) {
                    config.put("apiKey", params.get("apiKey"));
                    logger.info("  ✅ Mapped 'apiKey' parameter: [SENSITIVE]");
                } else if (params.containsKey("GEMINI_API_KEY")) {
                    config.put("apiKey", params.get("GEMINI_API_KEY"));
                    logger.info("  ✅ Mapped 'GEMINI_API_KEY' to 'apiKey': [SENSITIVE]");
                } else {
                    logger.warn("  ⚠️  No apiKey parameter found for Gemini integration");
                }
                
                if (params.containsKey("model")) {
                    config.put("model", params.get("model"));
                    logger.info("  ✅ Mapped 'model' parameter: {}", params.get("model"));
                } else if (params.containsKey("GEMINI_DEFAULT_MODEL")) {
                    config.put("model", params.get("GEMINI_DEFAULT_MODEL"));
                    logger.info("  ✅ Mapped 'GEMINI_DEFAULT_MODEL' to 'model': {}", params.get("GEMINI_DEFAULT_MODEL"));
                }
                
                if (params.containsKey("basePath")) {
                    config.put("basePath", params.get("basePath"));
                    logger.info("  ✅ Mapped 'basePath' parameter: {}", params.get("basePath"));
                } else if (params.containsKey("GEMINI_BASE_PATH")) {
                    config.put("basePath", params.get("GEMINI_BASE_PATH"));
                    logger.info("  ✅ Mapped 'GEMINI_BASE_PATH' to 'basePath': {}", params.get("GEMINI_BASE_PATH"));
                }
                break;
                
            default:
                logger.info("🔍 Processing unknown integration type '{}' - copying all parameters as-is", integrationDto.getType());
                // For unknown types, copy all parameters as-is
                if (integrationDto.getConfigParams() != null) {
                    for (IntegrationConfigDto param : integrationDto.getConfigParams()) {
                        config.put(param.getParamKey(), param.getParamValue());
                        logger.info("  ✅ Copied parameter: {}={}", param.getParamKey(), 
                            param.isSensitive() ? "[SENSITIVE]" : param.getParamValue());
                    }
                }
                break;
        }
        
        logger.info("🎯 Final mapped config for type '{}': {} parameters", integrationDto.getType(), config.length());
        
        return config;
    }



    /**
     * Request model for job execution
     */
    public static class JobExecutionRequest {
        private String jobName;
        private JSONObject params;
        private List<String> requiredIntegrations;

        // Getters and setters
        public String getJobName() { return jobName; }
        public void setJobName(String jobName) { this.jobName = jobName; }
        
        public JSONObject getParams() { return params; }
        public void setParams(JSONObject params) { this.params = params; }
        
        public List<String> getRequiredIntegrations() { return requiredIntegrations; }
        public void setRequiredIntegrations(List<String> requiredIntegrations) { this.requiredIntegrations = requiredIntegrations; }
    }

    /**
     * Executes a job in server-managed mode with pre-resolved integrations.
     * 
     * @param request The job execution request containing job name, parameters, and required integrations
     * @return ResponseEntity containing the job execution result
     */
    @PostMapping("/execute")
    @Operation(summary = "Execute a job in server-managed mode", 
               description = "Executes a job with server-managed integration resolution and credential injection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job executed successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "500", description = "Job execution failed",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    public ResponseEntity<Object> executeJob(
            @Parameter(description = "Job execution request", required = true,
                    content = @Content(examples = @ExampleObject(value = 
                        "{\n" +
                        "  \"jobName\": \"Expert\",\n" +
                        "  \"params\": {\n" +
                        "    \"request\": \"Analyze this ticket\",\n" +
                        "    \"inputJql\": \"key = DMC-123\",\n" +
                        "    \"initiator\": \"user@example.com\"\n" +
                        "  },\n" +
                        "  \"requiredIntegrations\": [\"tracker\", \"ai\", \"wiki\"]\n" +
                        "}")))
            @RequestBody JobExecutionRequest request,
            Authentication authentication) {
        
        try {
            logger.info("Received job execution request for job: {}", request.getJobName());
            
            // Validate request
            if (request.getJobName() == null || request.getJobName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Job name is required");
            }
            
            if (request.getParams() == null) {
                return ResponseEntity.badRequest().body("Job parameters are required");
            }
            
            // Determine required integrations if not provided
            List<String> requiredIntegrations = request.getRequiredIntegrations();
            if (requiredIntegrations == null || requiredIntegrations.isEmpty()) {
                requiredIntegrations = integrationResolutionService.getRequiredIntegrationsForJob(request.getJobName());
                logger.info("Auto-determined required integrations for {}: {}", request.getJobName(), requiredIntegrations);
            }
            
            // 1. Resolve integrations with credentials based on whether they are IDs or types
            logger.info("🔄 Resolving integrations for job '{}' with values: {}", request.getJobName(), requiredIntegrations);
            
            // Check if we have integration IDs (UUIDs) or integration types (strings like "jira", "openai")
            boolean hasIntegrationIds = requiredIntegrations.stream()
                .anyMatch(id -> id.contains("-") && id.length() > 30); // UUIDs contain dashes and are ~36 chars
            
            JSONObject resolvedIntegrations;
            if (hasIntegrationIds) {
                logger.info("🆔 Detected integration IDs (UUIDs), resolving from database");
                resolvedIntegrations = resolveIntegrationIds(requiredIntegrations, getUserId(authentication));
            } else {
                logger.info("🔧 Detected integration types, resolving from properties");
                resolvedIntegrations = integrationResolutionService.resolveIntegrationsForJob(
                    request.getJobName(), 
                    requiredIntegrations
                );
            }
            
            logger.info("✅ Resolved {} integrations for job '{}'", resolvedIntegrations.length(), request.getJobName());
            
            // Log integration details without exposing secrets
            for (String key : resolvedIntegrations.keySet()) {
                JSONObject integration = resolvedIntegrations.getJSONObject(key);
                String type = integration.optString("type", "unknown");
                String basePath = integration.optString("basePath", "not-set");
                String url = integration.optString("url", "not-set");
                String token = integration.optString("token", "not-set");
                String defaultSpace = integration.optString("defaultSpace", "not-set");
                
                logger.info("  📋 Integration '{}': type={}, basePath={}, url={}, token={}, defaultSpace={}", 
                    key, type, basePath, url, 
                    token.equals("not-set") ? "not-set" : "[SENSITIVE]", 
                    defaultSpace);
            }
            
            // 2. Prepare JobParams with pre-resolved integrations
            JobParams jobParams = new JobParams();
            jobParams.setName(request.getJobName());
            jobParams.set("params", request.getParams());
            jobParams.setExecutionMode(ExecutionMode.SERVER_MANAGED);
            jobParams.setResolvedIntegrations(resolvedIntegrations);
            
            logger.info("🚀 Prepared JobParams for '{}' with execution mode: SERVER_MANAGED", request.getJobName());
            logger.info("📝 Job parameters: {}", request.getParams().toString());
            
            // 3. Execute in managed thread context
            return CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info("⚡ Starting server-managed execution of job: {}", request.getJobName());
                    long startTime = System.currentTimeMillis();
                    
                    Object result = new JobRunner().run(jobParams);
                    
                    long duration = System.currentTimeMillis() - startTime;
                    logger.info("✅ Successfully completed job execution: {} (took {} ms)", request.getJobName(), duration);
                    return ResponseEntity.ok(result);
                } catch (Exception e) {
                    logger.error("❌ Job execution failed for {}: {}", request.getJobName(), e.getMessage(), e);
                    
                    // Log additional context for debugging
                    logger.error("🔍 Job execution context - Name: {}, ExecutionMode: {}, IntegrationsCount: {}", 
                        jobParams.getName(), jobParams.getExecutionMode(), resolvedIntegrations.length());
                    
                    return ResponseEntity.status(500).body((Object) ("Job execution failed: " + e.getMessage()));
                }
            }).join();
            
        } catch (Exception e) {
            logger.error("Failed to process job execution request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to process request: " + e.getMessage());
        }
    }
    
    /**
     * Gets the list of supported jobs for server-managed execution.
     * 
     * @return ResponseEntity containing the list of available jobs
     */
    @GetMapping("/available")
    @Operation(summary = "Get available jobs", description = "Returns the list of jobs available for server-managed execution")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved available jobs",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "array")))
    })
    public ResponseEntity<List<String>> getAvailableJobs() {
        try {
            List<JobTypeDto> jobTypes = jobConfigurationLoader.getAllJobTypes();
            List<String> availableJobs = jobTypes.stream()
                    .map(JobTypeDto::getType)
                    .toList();
            logger.info("Returning available jobs: {}", availableJobs);
            return ResponseEntity.ok(availableJobs);
        } catch (Exception e) {
            logger.error("Failed to get available jobs: {}", e.getMessage());
            // Fallback to hardcoded list
            List<String> fallbackJobs = List.of("Expert", "TestCasesGenerator");
            logger.info("Returning fallback jobs: {}", fallbackJobs);
            return ResponseEntity.ok(fallbackJobs);
        }
    }
    
    /**
     * Gets detailed information about available jobs.
     * 
     * @return ResponseEntity containing the list of job type details
     */
    @GetMapping("/types")
    @Operation(summary = "Get job types", description = "Returns detailed information about available job types including parameters and descriptions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved job types",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "array")))
    })
    public ResponseEntity<List<JobTypeDto>> getJobTypes() {
        try {
            List<JobTypeDto> jobTypes = jobConfigurationLoader.getAllJobTypes();
            logger.info("Returning {} job types", jobTypes.size());
            return ResponseEntity.ok(jobTypes);
        } catch (Exception e) {
            logger.error("Failed to get job types: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Gets detailed information about a specific job type.
     * 
     * @param jobName The name of the job type
     * @return ResponseEntity containing the job type details
     */
    @GetMapping("/types/{jobName}")
    @Operation(summary = "Get job type details", description = "Returns detailed information about a specific job type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved job type details",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = JobTypeDto.class))),
        @ApiResponse(responseCode = "404", description = "Job type not found",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    public ResponseEntity<JobTypeDto> getJobType(
            @Parameter(description = "Name of the job type", required = true)
            @PathVariable String jobName) {
        
        try {
            JobTypeDto jobType = jobConfigurationLoader.getJobType(jobName);
            logger.info("Returning job type details for: {}", jobName);
            return ResponseEntity.ok(jobType);
        } catch (IllegalArgumentException e) {
            logger.error("Job type not found: {}", jobName);
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            logger.error("Failed to get job type {}: {}", jobName, e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Gets the required integrations for a specific job.
     * 
     * @param jobName The name of the job
     * @return ResponseEntity containing the list of required integrations
     */
    @GetMapping("/{jobName}/integrations")
    @Operation(summary = "Get required integrations for a job", 
               description = "Returns the list of integrations required for the specified job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved required integrations",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "array"))),
        @ApiResponse(responseCode = "404", description = "Job not found",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    public ResponseEntity<List<String>> getRequiredIntegrations(
            @Parameter(description = "Name of the job", required = true)
            @PathVariable String jobName) {
        
        try {
            List<String> requiredIntegrations = integrationResolutionService.getRequiredIntegrationsForJob(jobName);
            logger.info("Required integrations for {}: {}", jobName, requiredIntegrations);
            return ResponseEntity.ok(requiredIntegrations);
        } catch (Exception e) {
            logger.error("Failed to get required integrations for {}: {}", jobName, e.getMessage());
            return ResponseEntity.status(404).body(null);
        }
    }
    
    /**
     * Executes a saved job configuration by ID with optional parameter overrides.
     * 
     * @param configId The ID of the saved job configuration
     * @param request Optional execution request with parameter overrides
     * @param authentication Authentication information
     * @return ResponseEntity containing the job execution result
     */
    @PostMapping("/configurations/{configId}/execute")
    @Operation(summary = "Execute a saved job configuration", 
               description = "Executes a saved job configuration by ID with optional parameter overrides")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job executed successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "404", description = "Job configuration not found",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "500", description = "Job execution failed",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    public ResponseEntity<Object> executeSavedJobConfiguration(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String configId,
            @Parameter(description = "Execution request with optional overrides", required = false,
                    content = @Content(examples = @ExampleObject(value = 
                        "{\n" +
                        "  \"parameterOverrides\": {\n" +
                        "    \"request\": \"Updated analysis request\"\n" +
                        "  },\n" +
                        "  \"integrationOverrides\": {\n" +
                        "    \"tracker\": \"integration-id-123\"\n" +
                        "  },\n" +
                        "  \"executionMode\": \"HYBRID\"\n" +
                        "}")))
            @RequestBody(required = false) ExecuteJobConfigurationRequest request,
            Authentication authentication) {
        
        try {
            String userId = getUserId(authentication);
            logger.info("Received execution request for saved job configuration: {} by user: {}", configId, userId);
            
            // Use empty request if none provided
            if (request == null) {
                request = new ExecuteJobConfigurationRequest();
            }
            
            // Get execution parameters from saved configuration with overrides
            var executionParamsOpt = jobConfigurationService.getExecutionParameters(configId, request, userId);
            if (executionParamsOpt.isEmpty()) {
                logger.error("Job configuration not found or not accessible: {}", configId);
                return ResponseEntity.notFound().build();
            }
            
            ExecutionParametersDto executionParams = executionParamsOpt.get();
            logger.info("Executing saved job configuration {} of type {}", configId, executionParams.getJobType());
            
            // Convert integration mappings to JSONObject
            JSONObject integrationMappings = new JSONObject(executionParams.getIntegrationMappings().toString());
            
            // Extract integration values (IDs or types) from the mappings
            List<String> integrationValues = new java.util.ArrayList<>();
            integrationMappings.keys().forEachRemaining(key -> {
                String value = integrationMappings.getString(key);
                integrationValues.add(value);
            });
            
            // Determine if we have integration IDs (UUIDs) or integration types
            boolean hasIntegrationIds = integrationValues.stream()
                .anyMatch(id -> id.contains("-") && id.length() > 30); // UUIDs contain dashes and are ~36 chars
            
            JSONObject resolvedIntegrations;
            if (hasIntegrationIds) {
                logger.info("🆔 Detected integration IDs (UUIDs) in saved job configuration, resolving from database");
                resolvedIntegrations = resolveIntegrationIds(integrationValues, userId);
                
                // For Expert job, automatically include Confluence integration if available and not already included
                if ("Expert".equals(executionParams.getJobType()) && !resolvedIntegrations.has("confluence")) {
                    logger.info("🔧 Expert job detected - checking for available Confluence integration");
                    try {
                        // Find user's Confluence integration by type
                        var userIntegrations = integrationService.getIntegrationsForUser(userId);
                        var confluenceIntegrations = userIntegrations.stream()
                            .filter(integration -> "confluence".equals(integration.getType()))
                            .toList();
                        
                        if (!confluenceIntegrations.isEmpty()) {
                            String confluenceId = confluenceIntegrations.get(0).getId();
                            logger.info("✅ Found user's Confluence integration: {}, adding to resolved integrations", confluenceId);
                            JSONObject confluenceConfig = resolveIntegrationIds(List.of(confluenceId), userId);
                            if (confluenceConfig.has("confluence")) {
                                resolvedIntegrations.put("confluence", confluenceConfig.getJSONObject("confluence"));
                                logger.info("✅ Successfully added Confluence integration for Expert job");
                            }
                        } else {
                            logger.warn("⚠️ No Confluence integration found for Expert job - URI processing may fail");
                        }
                    } catch (Exception e) {
                        logger.error("❌ Failed to auto-include Confluence integration for Expert job: {}", e.getMessage());
                    }
                }
            } else {
                logger.info("🔧 Detected integration types in saved job configuration, resolving from properties");
                resolvedIntegrations = integrationResolutionService.resolveIntegrationsForJob(
                    executionParams.getJobType(), 
                    integrationValues
                );
            }
            
            logger.debug("Resolved {} integrations for saved job {}", resolvedIntegrations.length(), configId);
            
            // Prepare JobParams with pre-resolved integrations
            JobParams jobParams = new JobParams();
            jobParams.setName(executionParams.getJobType());
            jobParams.set("params", new JSONObject(executionParams.getJobParameters().toString()));
            jobParams.setExecutionMode(ExecutionMode.valueOf(executionParams.getExecutionMode()));
            jobParams.setResolvedIntegrations(resolvedIntegrations);
            
            // Record execution before starting
            jobConfigurationService.recordExecution(configId, userId);
            
            // Execute in managed thread context
            return CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info("Starting execution of saved job configuration: {}", configId);
                    Object result = new JobRunner().run(jobParams);
                    logger.info("Successfully completed execution of saved job configuration: {}", configId);
                    return ResponseEntity.ok(result);
                } catch (Exception e) {
                    logger.error("Saved job execution failed for {}: {}", configId, e.getMessage(), e);
                    return ResponseEntity.status(500).body((Object) ("Job execution failed: " + e.getMessage()));
                }
            }).join();
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for saved job execution {}: {}", configId, e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to process saved job execution request for {}: {}", configId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to process request: " + e.getMessage());
        }
    }
    

} 