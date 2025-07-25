package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for MCP configuration responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MCP Configuration information")
public class McpConfigurationDto {
    
    @Schema(description = "Unique configuration identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "User-defined name for the configuration", example = "Development MCP")
    private String name;
    
    @Schema(description = "Owner user ID", example = "user-123")
    private String userId;
    
    @Schema(description = "List of enabled integration IDs")
    private List<String> integrationIds;
    
    @Schema(description = "Configuration creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Configuration last update timestamp")
    private LocalDateTime updatedAt;
} 