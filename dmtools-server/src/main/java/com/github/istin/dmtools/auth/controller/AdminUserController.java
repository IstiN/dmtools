package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.RoleService;
import com.github.istin.dmtools.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for admin user management endpoints with role assignment capabilities
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin User Management", description = "Admin-only endpoints for user management and role assignment")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;

    /**
     * Get paginated users with role information and search (Admin only)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get paginated list of users", 
        description = "Retrieve a paginated list of all users with role information and optional search functionality. Admin access required.",
        security = @SecurityRequirement(name = "bearer_jwt")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUsers(
            @Parameter(description = "Page number (zero-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100)", example = "50") 
            @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Search term for email or name", example = "john@example.com") 
            @RequestParam(required = false) String search) {
        
        logger.info("Admin user listing requested - page: {}, size: {}, search: '{}'", page, size, search);
        
        try {
            // Validate pagination parameters
            if (page < 0) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Page number must be non-negative", "code", "INVALID_PAGE"));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Page size must be between 1 and 100", "code", "INVALID_SIZE"));
            }
            
            // Create pageable with sorting by email
            Pageable pageable = PageRequest.of(page, size, Sort.by("email").ascending());
            
            // Get users with search
            Page<User> userPage = userService.getUsers(pageable, search);
            
            // Convert to response format
            Map<String, Object> response = new HashMap<>();
            
            response.put("users", userPage.getContent().stream()
                .map(this::convertUserToResponse)
                .collect(Collectors.toList()));
                
            response.put("pagination", Map.of(
                "currentPage", userPage.getNumber(),
                "totalPages", userPage.getTotalPages(),
                "totalElements", userPage.getTotalElements(),
                "size", userPage.getSize(),
                "hasNext", userPage.hasNext(),
                "hasPrevious", userPage.hasPrevious()
            ));
            
            logger.info("Returning {} users for admin listing", userPage.getNumberOfElements());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving users for admin: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to retrieve users", "code", "INTERNAL_ERROR"));
        }
    }

    /**
     * Assign/update user role (Admin only)
     */
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user role", 
        description = "Assign or update the role for a specific user. Admin access required.",
        security = @SecurityRequirement(name = "bearer_jwt")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role or user not found"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateUserRole(
            @Parameter(description = "User ID", example = "user-123") 
            @PathVariable String userId,
            @Parameter(description = "Role update request", schema = @Schema(example = "{\"role\": \"ADMIN\"}"))
            @RequestBody Map<String, String> request) {
        
        String role = request.get("role");
        logger.info("Admin role assignment requested - userId: {}, role: {}", userId, role);
        
        try {
            // Validate role
            if (role == null || role.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Role is required", "code", "MISSING_ROLE"));
            }
            
            role = role.trim().toUpperCase();
            if (!"ADMIN".equals(role) && !"REGULAR_USER".equals(role)) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid role. Must be ADMIN or REGULAR_USER", "code", "INVALID_ROLE"));
            }
            
            // Update role via RoleService (handles cache invalidation)
            User updatedUser = roleService.updateUserRole(userId, role);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Role updated successfully",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "role", userService.getUserRole(updatedUser)
                )
            );
            
            logger.info("Role updated successfully for user {}: {}", userId, role);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role assignment request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage(), "code", "INVALID_REQUEST"));
                
        } catch (Exception e) {
            logger.error("Error updating user role for {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to update user role", "code", "INTERNAL_ERROR"));
        }
    }

    /**
     * Get role cache statistics (Admin only)
     */
    @GetMapping("/cache/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCacheStats() {
        logger.info("Admin requested role cache statistics");
        
        try {
            Map<String, Object> stats = roleService.getCacheStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error retrieving cache stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to retrieve cache statistics", "code", "INTERNAL_ERROR"));
        }
    }

    /**
     * Clear role cache (Admin only)
     */
    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> clearCache() {
        logger.info("Admin requested role cache clear");
        
        try {
            roleService.clearCache();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Role cache cleared successfully"
            ));
            
        } catch (Exception e) {
            logger.error("Error clearing cache: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to clear cache", "code", "INTERNAL_ERROR"));
        }
    }

    /**
     * Re-evaluate and update all users' roles based on current admin email configuration (Admin only)
     */
    @PostMapping("/users/roles/reevaluate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Re-evaluate all user roles",
        description = "Re-evaluate and update all users' roles based on the current admin email configuration. This will promote users to admin if their email is in the admin list, or demote them if they're no longer admin. Admin access required.",
        security = @SecurityRequirement(name = "bearer_jwt")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User roles re-evaluated successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> reevaluateUserRoles() {
        logger.info("Admin requested user roles re-evaluation");
        try {
            userService.updateAllUserRoles();
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "All user roles have been re-evaluated and updated successfully"
            ));
        } catch (Exception e) {
            logger.error("Error re-evaluating user roles: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to re-evaluate user roles", "code", "INTERNAL_ERROR")
            );
        }
    }

    /**
     * Convert User entity to response format
     */
    private Map<String, Object> convertUserToResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("name", user.getName());
        userResponse.put("role", userService.getUserRole(user));
        
        // Convert joinedAt (we'll use a placeholder since User doesn't have creation date)
        // In a real implementation, you'd add a createdAt field to User entity
        userResponse.put("joinedAt", LocalDateTime.now().minusDays(30).toString() + "Z");
        
        // Convert lastLogin (placeholder - would need to track this in a real implementation)
        userResponse.put("lastLogin", LocalDateTime.now().minusDays(1).toString() + "Z");
        
        return userResponse;
    }
}
