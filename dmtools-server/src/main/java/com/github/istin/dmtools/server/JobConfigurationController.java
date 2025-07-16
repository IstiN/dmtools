package com.github.istin.dmtools.server;

import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.dto.*;
import com.github.istin.dmtools.server.service.JobConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public JobConfigurationController(JobConfigurationService jobConfigurationService, UserService userService) {
        this.jobConfigurationService = jobConfigurationService;
        this.userService = userService;
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
     * This endpoint allows external systems to trigger job execution via webhook.
     * Note: This endpoint has relaxed authentication for webhook usage.
     * 
     * @param id Job configuration ID
     * @param request Optional execution request with parameter overrides
     * @param apiKey Optional API key for webhook authentication (future enhancement)
     * @return ResponseEntity containing execution status
     */
    @PostMapping("/{id}/webhook")
    @Operation(summary = "Webhook endpoint for job execution", 
               description = "Execute a saved job configuration via webhook with optional parameter overrides")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Job execution started successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "404", description = "Job configuration not found",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "500", description = "Job execution failed to start",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    public ResponseEntity<Object> executeJobConfigurationWebhook(
            @Parameter(description = "Job configuration ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Execution request with optional overrides", required = false)
            @RequestBody(required = false) ExecuteJobConfigurationRequest request,
            @Parameter(description = "API key for authentication (future enhancement)", required = false)
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        
        try {
            // TODO: Implement proper API key validation for webhook security
            // For now, we'll use a simplified approach
            
            // Use empty request if none provided
            if (request == null) {
                request = new ExecuteJobConfigurationRequest();
            }
            
            // For webhook execution, we need to find the job configuration owner
            // Since we don't have authentication context, we'll need to modify the service
            // to allow webhook execution. For now, this is a placeholder.
            
            // TODO: Implement webhook-specific execution that doesn't require user authentication
            // This could involve storing webhook tokens with job configurations
            // or implementing a separate webhook authentication mechanism
            
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body("Webhook execution not yet implemented - requires authentication mechanism");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process webhook request: " + e.getMessage());
        }
    }
} 