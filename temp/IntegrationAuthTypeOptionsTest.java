package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.dto.IntegrationTypeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify auth type options bug fix for integration configurations.
 */
@SpringBootTest(classes = com.github.istin.dmtools.server.DmToolsServerApplication.class)
public class IntegrationAuthTypeOptionsTest {

    @Autowired
    private IntegrationConfigurationLoader configurationLoader;

    @Test
    public void testConfluenceAuthTypeOptions_ShouldReturnSelectWithOptions() {
        // When
        IntegrationTypeDto confluence = configurationLoader.getIntegrationType("confluence");

        // Then
        assertNotNull(confluence);
        assertEquals("confluence", confluence.getType());
        
        // Find the AUTH_TYPE parameter
        IntegrationTypeDto.ConfigParamDefinition authTypeParam = confluence.getConfigParams().stream()
                .filter(p -> "CONFLUENCE_AUTH_TYPE".equals(p.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("CONFLUENCE_AUTH_TYPE parameter not found"));
        
        // Verify it's a select type
        assertEquals("select", authTypeParam.getType(), 
                "CONFLUENCE_AUTH_TYPE should be 'select' type");
        
        // Verify it has options
        assertNotNull(authTypeParam.getOptions(), 
                "CONFLUENCE_AUTH_TYPE should have options");
        assertFalse(authTypeParam.getOptions().isEmpty(), 
                "CONFLUENCE_AUTH_TYPE options should not be empty");
        
        // Verify it contains Basic and Bearer options
        assertTrue(authTypeParam.getOptions().contains("Basic"), 
                "CONFLUENCE_AUTH_TYPE should contain 'Basic' option");
        assertTrue(authTypeParam.getOptions().contains("Bearer"), 
                "CONFLUENCE_AUTH_TYPE should contain 'Bearer' option");
        
        // Verify default value
        assertEquals("Basic", authTypeParam.getDefaultValue(), 
                "CONFLUENCE_AUTH_TYPE should have 'Basic' as default value");
        
        System.out.println("✅ Confluence AUTH_TYPE test passed:");
        System.out.println("   Type: " + authTypeParam.getType());
        System.out.println("   Options: " + authTypeParam.getOptions());
        System.out.println("   Default: " + authTypeParam.getDefaultValue());
    }

    @Test
    public void testJiraAuthTypeOptions_ShouldReturnSelectWithOptions() {
        // When
        IntegrationTypeDto jira = configurationLoader.getIntegrationType("jira");

        // Then
        assertNotNull(jira);
        assertEquals("jira", jira.getType());
        
        // Find the AUTH_TYPE parameter
        IntegrationTypeDto.ConfigParamDefinition authTypeParam = jira.getConfigParams().stream()
                .filter(p -> "JIRA_AUTH_TYPE".equals(p.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("JIRA_AUTH_TYPE parameter not found"));
        
        // Verify it's a select type
        assertEquals("select", authTypeParam.getType(), 
                "JIRA_AUTH_TYPE should be 'select' type");
        
        // Verify it has options
        assertNotNull(authTypeParam.getOptions(), 
                "JIRA_AUTH_TYPE should have options");
        assertFalse(authTypeParam.getOptions().isEmpty(), 
                "JIRA_AUTH_TYPE options should not be empty");
        
        // Verify it contains Basic and Bearer options
        assertTrue(authTypeParam.getOptions().contains("Basic"), 
                "JIRA_AUTH_TYPE should contain 'Basic' option");
        assertTrue(authTypeParam.getOptions().contains("Bearer"), 
                "JIRA_AUTH_TYPE should contain 'Bearer' option");
        
        // Verify default value
        assertEquals("Basic", authTypeParam.getDefaultValue(), 
                "JIRA_AUTH_TYPE should have 'Basic' as default value");
        
        System.out.println("✅ Jira AUTH_TYPE test passed:");
        System.out.println("   Type: " + authTypeParam.getType());
        System.out.println("   Options: " + authTypeParam.getOptions());
        System.out.println("   Default: " + authTypeParam.getDefaultValue());
    }
} 