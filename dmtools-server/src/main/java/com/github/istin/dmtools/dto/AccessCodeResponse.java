package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for MCP access code generation responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generated access code for MCP configuration")
public class AccessCodeResponse {
    
    @Schema(description = "Name of the MCP configuration", example = "Development MCP")
    private String configurationName;
    
    @Schema(description = "Format of the generated code", example = "cursor", allowableValues = {"cursor", "json", "shell"})
    private String format;
    
    @Schema(description = "Ready-to-use configuration code")
    private String code;
    
    @Schema(description = "Direct MCP endpoint URL", example = "http://localhost:8080/mcp/config/550e8400-e29b-41d4-a716-446655440000")
    private String endpointUrl;
    
    @Schema(description = "Human-readable setup instructions")
    private String instructions;
} 