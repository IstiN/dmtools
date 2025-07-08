package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String paramValue;
    
    private boolean sensitive;
} 