package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for sharing an integration with a workspace.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareIntegrationWithWorkspaceRequest {
    
    @NotBlank(message = "Workspace ID is required")
    private String workspaceId;
} 