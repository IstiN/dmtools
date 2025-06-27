package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.istin.dmtools.auth.model.IntegrationPermissionLevel;
import com.github.istin.dmtools.auth.model.IntegrationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for IntegrationUser entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationUserDto {
    
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPictureUrl;
    private IntegrationPermissionLevel permissionLevel;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime addedAt;
    
    /**
     * Converts an IntegrationUser entity to DTO.
     * 
     * @param integrationUser The integration user entity
     * @return The integration user DTO
     */
    public static IntegrationUserDto fromEntity(IntegrationUser integrationUser) {
        IntegrationUserDto dto = new IntegrationUserDto();
        dto.setId(integrationUser.getId());
        dto.setUserId(integrationUser.getUser().getId());
        dto.setUserName(integrationUser.getUser().getName());
        dto.setUserEmail(integrationUser.getUser().getEmail());
        dto.setUserPictureUrl(integrationUser.getUser().getPictureUrl());
        dto.setPermissionLevel(integrationUser.getPermissionLevel());
        dto.setAddedAt(integrationUser.getAddedAt());
        return dto;
    }
} 