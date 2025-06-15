package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.auth.model.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareWorkspaceRequest {
    
    @NotEmpty
    @Email
    private String email;
    
    @NotNull
    private WorkspaceRole role;
} 