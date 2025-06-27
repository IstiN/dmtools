package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for testing an integration connection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestIntegrationRequest {
    
    /**
     * The type of integration to test.
     */
    private String type;
    
    /**
     * Configuration parameters for the test.
     * Key is the parameter name, value is the parameter value.
     */
    private Map<String, String> configParams = new HashMap<>();
} 