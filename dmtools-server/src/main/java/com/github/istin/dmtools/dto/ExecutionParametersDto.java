package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for execution parameters with merged overrides.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionParametersDto {
    
    private String jobType;
    private JsonNode jobParameters;
    private JsonNode integrationMappings;
    private String executionMode;
} 