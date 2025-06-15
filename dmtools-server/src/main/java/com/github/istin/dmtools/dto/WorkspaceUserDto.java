package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.auth.model.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceUserDto {
    private String id;
    private String email;
    private WorkspaceRole role;
} 