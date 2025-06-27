package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing integrations.
 */
@RestController
@RequestMapping("/api/integrations")
@Tag(name = "Integrations", description = "API for managing integrations with external systems")
public class IntegrationController {

    private final IntegrationService integrationService;

    @Autowired
    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    private String getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            return ((OAuth2User) principal).getAttribute("sub");
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return authentication.getName();
    }

    /**
     * Get all integrations accessible to the current user.
     *
     * @param authentication The authenticated user
     * @return List of integration DTOs
     */
    @GetMapping
    @Operation(summary = "Get all integrations", description = "Retrieves all integrations accessible to the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of integrations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<IntegrationDto>> getIntegrations(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(integrationService.getIntegrationsForUser(userId));
    }

    /**
     * Get integrations for a specific workspace.
     *
     * @param workspaceId The ID of the workspace
     * @return List of integration DTOs
     */
    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get workspace integrations", description = "Retrieves all integrations for a specific workspace")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of integrations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Workspace not found")
    })
    public ResponseEntity<List<IntegrationDto>> getWorkspaceIntegrations(@PathVariable String workspaceId) {
        return ResponseEntity.ok(integrationService.getIntegrationsForWorkspace(workspaceId));
    }

    /**
     * Get an integration by ID.
     *
     * @param id The ID of the integration
     * @param includeSensitive Whether to include sensitive configuration values
     * @param authentication The authenticated user
     * @return The integration DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get integration by ID", description = "Retrieves a specific integration by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<IntegrationDto> getIntegration(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeSensitive,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(integrationService.getIntegrationById(id, userId, includeSensitive));
    }

    /**
     * Create a new integration.
     *
     * @param request The create integration request
     * @param authentication The authenticated user
     * @return The created integration DTO
     */
    @PostMapping
    @Operation(summary = "Create integration", description = "Creates a new integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<IntegrationDto> createIntegration(
            @Valid @RequestBody CreateIntegrationRequest request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(integrationService.createIntegration(request, userId));
    }

    /**
     * Update an existing integration.
     *
     * @param id The ID of the integration to update
     * @param request The update integration request
     * @param authentication The authenticated user
     * @return The updated integration DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update integration", description = "Updates an existing integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<IntegrationDto> updateIntegration(
            @PathVariable String id,
            @Valid @RequestBody UpdateIntegrationRequest request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(integrationService.updateIntegration(id, request, userId));
    }

    /**
     * Delete an integration.
     *
     * @param id The ID of the integration to delete
     * @param authentication The authenticated user
     * @return Empty response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete integration", description = "Deletes an existing integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<Void> deleteIntegration(
            @PathVariable String id,
            Authentication authentication) {
        String userId = getUserId(authentication);
        integrationService.deleteIntegration(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Enable an integration.
     *
     * @param id The ID of the integration
     * @param authentication The authenticated user
     * @return The updated integration DTO
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "Enable integration", description = "Enables an integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration enabled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<IntegrationDto> enableIntegration(
            @PathVariable String id,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(integrationService.setIntegrationEnabled(id, userId, true));
    }

    /**
     * Disable an integration.
     *
     * @param id The ID of the integration
     * @param authentication The authenticated user
     * @return The updated integration DTO
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "Disable integration", description = "Disables an integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration disabled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<IntegrationDto> disableIntegration(
            @PathVariable String id,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(integrationService.setIntegrationEnabled(id, userId, false));
    }

    /**
     * Share an integration with a user.
     *
     * @param id The ID of the integration
     * @param request The share integration request
     * @param authentication The authenticated user
     * @return The integration user DTO
     */
    @PostMapping("/{id}/users")
    @Operation(summary = "Share integration with user", description = "Shares an integration with another user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration shared successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration or user not found")
    })
    public ResponseEntity<IntegrationUserDto> shareWithUser(
            @PathVariable String id,
            @Valid @RequestBody ShareIntegrationRequest request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(integrationService.shareIntegrationWithUser(id, request, userId));
    }

    /**
     * Remove a user's access to an integration.
     *
     * @param id The ID of the integration
     * @param userId The ID of the user to remove
     * @param authentication The authenticated user
     * @return Empty response
     */
    @DeleteMapping("/{id}/users/{userId}")
    @Operation(summary = "Remove user access", description = "Removes a user's access to an integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User access removed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration or user not found")
    })
    public ResponseEntity<Void> removeUserAccess(
            @PathVariable String id,
            @PathVariable String userId,
            Authentication authentication) {
        String currentUserId = getUserId(authentication);
        integrationService.removeUserAccess(id, userId, currentUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Share an integration with a workspace.
     *
     * @param id The ID of the integration
     * @param request The share integration with workspace request
     * @param authentication The authenticated user
     * @return The workspace DTO
     */
    @PostMapping("/{id}/workspaces")
    @Operation(summary = "Share integration with workspace", description = "Shares an integration with a workspace")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration shared with workspace successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration or workspace not found")
    })
    public ResponseEntity<WorkspaceDto> shareWithWorkspace(
            @PathVariable String id,
            @Valid @RequestBody ShareIntegrationWithWorkspaceRequest request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(integrationService.shareIntegrationWithWorkspace(id, request, userId));
    }

    /**
     * Remove an integration from a workspace.
     *
     * @param id The ID of the integration
     * @param workspaceId The ID of the workspace
     * @param authentication The authenticated user
     * @return Empty response
     */
    @DeleteMapping("/{id}/workspaces/{workspaceId}")
    @Operation(summary = "Remove from workspace", description = "Removes an integration from a workspace")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration removed from workspace successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Integration or workspace not found")
    })
    public ResponseEntity<Void> removeFromWorkspace(
            @PathVariable String id,
            @PathVariable String workspaceId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        integrationService.removeIntegrationFromWorkspace(id, workspaceId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Test an integration connection.
     *
     * @param request The test integration request
     * @return Test result
     */
    @PostMapping("/test")
    @Operation(summary = "Test integration", description = "Tests an integration connection with provided configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> testIntegration(
            @Valid @RequestBody TestIntegrationRequest request) {
        return ResponseEntity.ok(integrationService.testIntegration(request));
    }

    /**
     * Get available integration types.
     *
     * @return List of integration type DTOs
     */
    @GetMapping("/types")
    @Operation(summary = "Get integration types", description = "Retrieves all available integration types")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Integration types retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<IntegrationTypeDto>> getIntegrationTypes() {
        return ResponseEntity.ok(integrationService.getAvailableIntegrationTypes());
    }

    /**
     * Get schema for a specific integration type.
     *
     * @param type The integration type
     * @return The integration type schema
     */
    @GetMapping("/types/{type}/schema")
    @Operation(summary = "Get integration type schema", description = "Retrieves the configuration schema for a specific integration type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Schema retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Integration type not found")
    })
    public ResponseEntity<IntegrationTypeDto> getIntegrationTypeSchema(@PathVariable String type) {
        return ResponseEntity.ok(integrationService.getIntegrationTypeSchema(type));
    }

    /**
     * Record usage of an integration.
     *
     * @param id The ID of the integration
     * @return Empty response
     */
    @PostMapping("/{id}/usage")
    @Operation(summary = "Record integration usage", description = "Records usage of an integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usage recorded successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<Void> recordUsage(@PathVariable String id) {
        integrationService.recordIntegrationUsage(id);
        return ResponseEntity.ok().build();
    }
}