package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.service.McpConfigurationService;
import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.dto.AccessCodeResponse;
import com.github.istin.dmtools.dto.CreateMcpConfigurationRequest;
import com.github.istin.dmtools.dto.McpConfigurationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing MCP (Model Context Protocol) configurations.
 * Provides endpoints for CRUD operations and access code generation.
 */
@RestController
@RequestMapping("/api/mcp")
@Tag(name = "MCP Configurations", description = "API for managing Model Context Protocol (MCP) configurations and integrations")
public class McpConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(McpConfigurationController.class);

    private final McpConfigurationService mcpConfigurationService;
    private final UserService userService;

    @Autowired
    public McpConfigurationController(McpConfigurationService mcpConfigurationService, UserService userService) {
        this.mcpConfigurationService = mcpConfigurationService;
        this.userService = userService;
    }

    /**
     * Extract user ID from authentication context.
     */
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
     * Get all MCP configurations for the authenticated user.
     */
    @GetMapping("/configurations")
    @Operation(summary = "Get MCP configurations", description = "Retrieve all MCP configurations for authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of user's MCP configurations"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<McpConfigurationDto>> getConfigurations(Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            List<McpConfigurationDto> configurations = mcpConfigurationService.getUserConfigurations(userId);
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            logger.error("Error retrieving MCP configurations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new MCP configuration.
     */
    @PostMapping("/configurations")
    @Operation(summary = "Create MCP configuration", description = "Create new MCP configuration (pass integration IDs for MCP)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "MCP configuration created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors or duplicate name"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> createConfiguration(
            @Valid @RequestBody CreateMcpConfigurationRequest request,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            McpConfigurationDto createdConfiguration = mcpConfigurationService.createConfiguration(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdConfiguration);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for creating MCP configuration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "validation_error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating MCP configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal_error", "message", "Failed to create configuration"));
        }
    }

    /**
     * Get a specific MCP configuration by ID.
     */
    @GetMapping("/configurations/{configId}")
    @Operation(summary = "Get MCP configuration", description = "Get specific MCP configuration details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MCP configuration retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<?> getConfiguration(
            @Parameter(description = "Configuration ID") @PathVariable String configId,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            return mcpConfigurationService.getUserConfiguration(configId, userId)
                    .map(config -> ResponseEntity.ok().body(config))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error retrieving MCP configuration {}", configId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal_error", "message", "Failed to retrieve configuration"));
        }
    }

    /**
     * Update an existing MCP configuration.
     */
    @PutMapping("/configurations/{configId}")
    @Operation(summary = "Update MCP configuration", description = "Update existing MCP configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MCP configuration updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<?> updateConfiguration(
            @Parameter(description = "Configuration ID") @PathVariable String configId,
            @Valid @RequestBody CreateMcpConfigurationRequest request,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            McpConfigurationDto updatedConfiguration = mcpConfigurationService.updateConfiguration(configId, request, userId);
            return ResponseEntity.ok(updatedConfiguration);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for updating MCP configuration {}: {}", configId, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "validation_error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating MCP configuration {}", configId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal_error", "message", "Failed to update configuration"));
        }
    }

    /**
     * Delete an MCP configuration.
     */
    @DeleteMapping("/configurations/{configId}")
    @Operation(summary = "Delete MCP configuration", description = "Delete MCP configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "MCP configuration deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<?> deleteConfiguration(
            @Parameter(description = "Configuration ID") @PathVariable String configId,
            Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            mcpConfigurationService.deleteConfiguration(configId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for deleting MCP configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting MCP configuration {}", configId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal_error", "message", "Failed to delete configuration"));
        }
    }

    /**
     * Generate access code for an MCP configuration.
     */
    @GetMapping("/configurations/{configId}/access-code")
    @Operation(summary = "Generate access code", description = "Generate configuration code for external tools")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Access code generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid format specified"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<?> generateAccessCode(
            @Parameter(description = "Configuration ID") @PathVariable String configId,
            @Parameter(description = "Output format for the configuration code") 
            @RequestParam(defaultValue = "cursor") String format,
            Authentication authentication) {
        try {
            // Validate format parameter
            if (!isValidFormat(format)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_format", 
                    "message", "Format must be one of: cursor, json, shell"
                ));
            }

            String userId = getUserId(authentication);
            AccessCodeResponse response = mcpConfigurationService.generateAccessCode(configId, userId, format);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for generating access code for configuration {}: {}", configId, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", "validation_error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error generating access code for configuration {}", configId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal_error", "message", "Failed to generate access code"));
        }
    }

    /**
     * Validate format parameter.
     */
    private boolean isValidFormat(String format) {
        return format != null && (format.equals("cursor") || format.equals("json") || format.equals("shell"));
    }
} 