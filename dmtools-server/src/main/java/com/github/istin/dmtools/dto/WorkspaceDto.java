package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.istin.dmtools.auth.model.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDto {
    private String id;
    private String name;
    private String ownerId;
    private Set<WorkspaceUserDto> users;
    private String description;
    private String ownerName;
    private String ownerEmail;
    private WorkspaceRole currentUserRole;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public WorkspaceDto(String id, String name, String ownerId, Set<WorkspaceUserDto> users) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.users = users;
    }
} 