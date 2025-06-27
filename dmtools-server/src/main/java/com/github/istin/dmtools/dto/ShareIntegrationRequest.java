package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.auth.model.IntegrationPermissionLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for sharing an integration with a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareIntegrationRequest {
    
    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String userEmail;
    
    @NotNull(message = "Permission level is required")
    private IntegrationPermissionLevel permissionLevel;
} 