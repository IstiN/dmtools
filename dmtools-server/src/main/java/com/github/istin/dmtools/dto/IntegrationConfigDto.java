package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for IntegrationConfig entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationConfigDto {
    
    private String id;
    private String paramKey;
    private String paramValue;
    private boolean sensitive;
} 