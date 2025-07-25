package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for creating new MCP configurations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new MCP configuration")
public class CreateMcpConfigurationRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @Schema(description = "Name for the MCP configuration", example = "Development MCP")
    private String name;
    
    @NotEmpty(message = "At least one integration ID is required")
    @Schema(description = "List of integration IDs to enable", example = "[\"integration-uuid-1\", \"integration-uuid-2\"]")
    private List<String> integrationIds;
} 