package com.github.istin.dmtools.server;

import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.dto.*;
import com.github.istin.dmtools.server.service.JobConfigurationService;
import com.github.istin.dmtools.server.service.WebhookKeyService;
import com.github.istin.dmtools.server.service.WebhookExamplesService;
import com.github.istin.dmtools.server.model.WebhookKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing saved job configurations.
 */
@RestController
@RequestMapping("/api/v1/job-configurations")
@Tag(name = "Job Configurations", description = "API for managing saved job configurations")
public class JobConfigurationController {

    private final JobConfigurationService jobConfigurationService;
    private final UserService userService;
    private final WebhookKeyService webhookKeyService;
    private final WebhookExamplesService webhookExamplesService;
    private final JobExecutionController jobExecutionController;

    public JobConfigurationController(JobConfigurationService jobConfigurationService, 
                                     UserService userService,
                                     WebhookKeyService webhookKeyService,
                                     WebhookExamplesService webhookExamplesService,
                                     JobExecutionController jobExecutionController) {
        this.jobConfigurationService = jobConfigurationService;
        this.userService = userService;
        this.webhookKeyService = webhookKeyService;
        this.webhookExamplesService = webhookExamplesService;
        this.jobExecutionController = jobExecutionController;
    }

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

    @GetMapping
    @Operation(summary = "Get all job configurations", 
               description = "Retrieve all job configurations accessible to the authenticated user")
    public ResponseEntity<List<JobConfigurationDto>> getJobConfigurations(
            @Parameter(description = "Only return enabled configurations", required = false)
            @RequestParam(value = "enabled", required = false, defaultValue = "false") boolean enabledOnly,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            List<JobConfigurationDto> jobConfigs = enabledOnly 
                    ? jobConfigurationService.getEnabledJobConfigurationsForUser(userId)
                    : jobConfigurationService.getJobConfigurationsForUser(userId);
            return ResponseEntity.ok(jobConfigs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job configuration by ID", 
               description = "Retrieve a specific job configuration by its ID")
    public ResponseEntity<JobConfigurationDto> getJobConfiguration(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            Optional<JobConfigurationDto> jobConfig = jobConfigurationService.getJobConfiguration(id, userId);
            return jobConfig.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a new job configuration", 
               description = "Create a new saved job configuration")
    public ResponseEntity<JobConfigurationDto> createJobConfiguration(
            @Parameter(description = "Job configuration creation request", required = true)
            @Valid @RequestBody CreateJobConfigurationRequest request,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            JobConfigurationDto jobConfig = jobConfigurationService.createJobConfiguration(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(jobConfig);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job configuration", 
               description = "Update an existing job configuration")
    public ResponseEntity<JobConfigurationDto> updateJobConfiguration(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Job configuration update request", required = true)
            @Valid @RequestBody UpdateJobConfigurationRequest request,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            Optional<JobConfigurationDto> jobConfig = jobConfigurationService.updateJobConfiguration(id, request, userId);
            return jobConfig.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job configuration", 
               description = "Delete a job configuration by its ID")
    public ResponseEntity<Void> deleteJobConfiguration(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            boolean deleted = jobConfigurationService.deleteJobConfiguration(id, userId);
            return deleted ? ResponseEntity.noContent().build() 
                          : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute a saved job configuration", 
               description = "Execute a saved job configuration with optional parameter overrides")
    public ResponseEntity<ExecutionParametersDto> getExecutionParameters(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Execution request with optional overrides", required = false)
            @RequestBody(required = false) ExecuteJobConfigurationRequest request,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            
            // Use empty request if none provided
            if (request == null) {
                request = new ExecuteJobConfigurationRequest();
            }
            
            Optional<ExecutionParametersDto> executionParams = 
                    jobConfigurationService.getExecutionParameters(id, request, userId);
            
            if (executionParams.isPresent()) {
                // Record the execution
                jobConfigurationService.recordExecution(id, userId);
                return ResponseEntity.ok(executionParams.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Webhook endpoint for executing a saved job configuration.
     * This endpoint allows external systems to trigger job execution via webhook with API key authentication.
     * 
     * @param id Job configuration ID
     * @param request Optional webhook execution request with parameter overrides
     * @param apiKey API key for webhook authentication
     * @return ResponseEntity containing execution status
     */
    @PostMapping("/{id}/webhook")
    @Operation(summary = "Webhook endpoint for job execution", 
               description = "Execute a saved job configuration via webhook with API key authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Job execution started successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = WebhookExecutionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "404", description = "Job configuration not found",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "500", description = "Job execution failed to start",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    public ResponseEntity<WebhookExecutionResponse> executeJobConfigurationWebhook(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Webhook execution request with optional overrides", required = false)
            @RequestBody(required = false) WebhookExecuteRequest request,
            @Parameter(description = "API key for authentication", required = true)
            @RequestHeader(value = "X-API-Key", required = true) String apiKey) {
        
        try {
            // Validate API key for this job configuration
            Optional<WebhookKey> optionalWebhookKey = webhookKeyService.validateApiKeyForJobConfig(apiKey, id);
            if (optionalWebhookKey.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(WebhookExecutionResponse.error("Invalid or missing API key for this job configuration", "INVALID_API_KEY"));
            }
            
            WebhookKey webhookKey = optionalWebhookKey.get();
            String userId = webhookKey.getCreatedBy().getId(); // Execute as job configuration owner
            
            // Use empty request if none provided
            if (request == null) {
                request = new WebhookExecuteRequest();
            }
            
            // Convert webhook request to standard execution request format
            ExecuteJobConfigurationRequest execRequest = request.toExecuteJobConfigurationRequest();
            
            // Get execution parameters from saved configuration with overrides
            var executionParamsOpt = jobConfigurationService.getExecutionParameters(id, execRequest, userId);
            if (executionParamsOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(WebhookExecutionResponse.error("Job configuration not found", "JOB_CONFIG_NOT_FOUND"));
            }
            
            // Create authentication for the webhook user (job configuration owner)
            Authentication webhookAuth = new UsernamePasswordAuthenticationToken(userId, null, null);
            
            // Delegate to the existing job execution controller which has all the logic
            ResponseEntity<JobExecutionResponse> executionResponse = 
                jobExecutionController.executeSavedJobConfiguration(id, execRequest, webhookAuth);
            
            // Convert JobExecutionResponse to WebhookExecutionResponse
            if (executionResponse.getStatusCode().is2xxSuccessful() && executionResponse.getBody() != null) {
                JobExecutionResponse jobResponse = executionResponse.getBody();
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(WebhookExecutionResponse.success(jobResponse.getExecutionId(), id));
            } else {
                // Handle error cases
                String errorMessage = executionResponse.getBody() != null ? 
                    executionResponse.getBody().getMessage() : "Unknown error";
                return ResponseEntity.status(executionResponse.getStatusCode())
                        .body(WebhookExecutionResponse.error(errorMessage, "EXECUTION_ERROR"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(WebhookExecutionResponse.error("Failed to process webhook request: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Create a new webhook API key for a job configuration.
     * 
     * @param id Job configuration ID
     * @param request Create webhook key request
     * @param authentication User authentication
     * @return ResponseEntity containing the created webhook key with API key value
     */
    @PostMapping("/{id}/webhook-keys")
    @Operation(summary = "Create webhook API key", 
               description = "Create a new API key for webhook authentication on this job configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Webhook key created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateWebhookKeyResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "404", description = "Job configuration not found",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "500", description = "Failed to create webhook key",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    public ResponseEntity<CreateWebhookKeyResponse> createWebhookKey(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Create webhook key request", required = true)
            @Valid @RequestBody CreateWebhookKeyRequest request,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            
            Optional<CreateWebhookKeyResponse> response = webhookKeyService.createWebhookKey(id, request, userId);
            return response.map(key -> ResponseEntity.status(HttpStatus.CREATED).body(key))
                    .orElse(ResponseEntity.notFound().build());
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all webhook API keys for a job configuration.
     * 
     * @param id Job configuration ID
     * @param authentication User authentication
     * @return ResponseEntity containing list of webhook keys (without key values)
     */
    @GetMapping("/{id}/webhook-keys")
    @Operation(summary = "List webhook API keys", 
               description = "Get all webhook API keys for this job configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook keys retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "array"))),
        @ApiResponse(responseCode = "404", description = "Job configuration not found",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "500", description = "Failed to retrieve webhook keys",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    public ResponseEntity<List<WebhookKeyDto>> getWebhookKeys(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            
            List<WebhookKeyDto> webhookKeys = webhookKeyService.getWebhookKeys(id, userId);
            return ResponseEntity.ok(webhookKeys);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete a webhook API key.
     * 
     * @param id Job configuration ID
     * @param keyId Webhook key ID
     * @param authentication User authentication
     * @return ResponseEntity indicating success or failure
     */
    @DeleteMapping("/{id}/webhook-keys/{keyId}")
    @Operation(summary = "Delete webhook API key", 
               description = "Delete a webhook API key by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Webhook key deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Job configuration or webhook key not found"),
        @ApiResponse(responseCode = "500", description = "Failed to delete webhook key")
    })
    public ResponseEntity<Void> deleteWebhookKey(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Webhook key ID", required = true)
            @PathVariable String keyId,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            
            boolean deleted = webhookKeyService.deleteWebhookKey(id, keyId, userId);
            return deleted ? ResponseEntity.noContent().build() 
                          : ResponseEntity.notFound().build();
                          
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get webhook integration examples for a job configuration.
     * 
     * @param id Job configuration ID
     * @param authentication User authentication
     * @return ResponseEntity containing webhook examples with templates
     */
    @GetMapping("/{id}/webhook-examples")
    @Operation(summary = "Get webhook integration examples", 
               description = "Get webhook integration examples and templates for this job configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook examples retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = WebhookExamplesDto.class))),
        @ApiResponse(responseCode = "404", description = "Job configuration not found",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "500", description = "Failed to retrieve webhook examples",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    public ResponseEntity<WebhookExamplesDto> getWebhookExamples(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            
            Optional<WebhookExamplesDto> examples = webhookExamplesService.getWebhookExamples(id, userId);
            return examples.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

} 