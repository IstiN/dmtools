package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.auth.service.IntegrationConfigurationLoader;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.IntegrationConfigDto;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;

@ExtendWith(MockitoExtension.class)
class IntegrationResolutionHelperTest {

    @Mock
    private IntegrationService integrationService;

    @Mock
    private IntegrationConfigurationLoader configurationLoader;

    private IntegrationResolutionHelper integrationResolutionHelper;

    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_INTEGRATION_ID = "integration-uuid-456";
    private static final String TEST_INTEGRATION_TYPE = "gemini";

    @BeforeEach
    void setUp() {
        integrationResolutionHelper = new IntegrationResolutionHelper();
        ReflectionTestUtils.setField(integrationResolutionHelper, "integrationService", integrationService);
        ReflectionTestUtils.setField(integrationResolutionHelper, "configurationLoader", configurationLoader);
    }

    @Test
    void testResolveIntegrationIds_success_returnsResolvedConfig() throws Exception {
        // Arrange
        IntegrationDto mockIntegration = createMockIntegration();
        JSONObject expectedConfig = new JSONObject();
        expectedConfig.put("api_key", "test-api-key");
        expectedConfig.put("model", "gemini-pro");

        when(integrationService.getIntegrationById(TEST_INTEGRATION_ID, TEST_USER_ID, true))
            .thenReturn(mockIntegration);

        List<String> integrationIds = Arrays.asList(TEST_INTEGRATION_ID);

        // Act
        try (MockedStatic<com.github.istin.dmtools.server.util.IntegrationConfigMapper> mockedMapper = 
             mockStatic(com.github.istin.dmtools.server.util.IntegrationConfigMapper.class)) {
            
            mockedMapper.when(() -> com.github.istin.dmtools.server.util.IntegrationConfigMapper
                .mapIntegrationConfig(eq(mockIntegration), eq(configurationLoader)))
                .thenReturn(expectedConfig);

            JSONObject result = integrationResolutionHelper.resolveIntegrationIds(integrationIds, TEST_USER_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.has(TEST_INTEGRATION_TYPE));
            assertEquals(expectedConfig.toString(), result.getJSONObject(TEST_INTEGRATION_TYPE).toString());
        }
        
        verify(integrationService).getIntegrationById(TEST_INTEGRATION_ID, TEST_USER_ID, true);
        verify(integrationService).recordIntegrationUsage(TEST_INTEGRATION_ID);
    }

    @Test
    void testResolveIntegrationIds_multipleIntegrations_returnsAllResolved() throws Exception {
        // Arrange
        String integration1Id = "integration-1";
        String integration2Id = "integration-2";
        String type1 = "gemini";
        String type2 = "jira";

        IntegrationDto integration1 = createMockIntegration(integration1Id, type1);
        IntegrationDto integration2 = createMockIntegration(integration2Id, type2);

        JSONObject config1 = new JSONObject().put("api_key", "key1");
        JSONObject config2 = new JSONObject().put("url", "jira.example.com");

        when(integrationService.getIntegrationById(integration1Id, TEST_USER_ID, true))
            .thenReturn(integration1);
        when(integrationService.getIntegrationById(integration2Id, TEST_USER_ID, true))
            .thenReturn(integration2);

        List<String> integrationIds = Arrays.asList(integration1Id, integration2Id);

        // Act
        try (MockedStatic<com.github.istin.dmtools.server.util.IntegrationConfigMapper> mockedMapper = 
             mockStatic(com.github.istin.dmtools.server.util.IntegrationConfigMapper.class)) {
            
            mockedMapper.when(() -> com.github.istin.dmtools.server.util.IntegrationConfigMapper
                .mapIntegrationConfig(eq(integration1), any()))
                .thenReturn(config1);
            mockedMapper.when(() -> com.github.istin.dmtools.server.util.IntegrationConfigMapper
                .mapIntegrationConfig(eq(integration2), any()))
                .thenReturn(config2);

            JSONObject result = integrationResolutionHelper.resolveIntegrationIds(integrationIds, TEST_USER_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.has(type1));
            assertTrue(result.has(type2));
            assertEquals(config1.toString(), result.getJSONObject(type1).toString());
            assertEquals(config2.toString(), result.getJSONObject(type2).toString());
        }
        
        verify(integrationService).recordIntegrationUsage(integration1Id);
        verify(integrationService).recordIntegrationUsage(integration2Id);
    }

    @Test
    void testResolveIntegrationIds_integrationNotFound_continuesWithOthers() throws Exception {
        // Arrange
        String validId = "valid-integration";
        String invalidId = "invalid-integration";
        
        IntegrationDto validIntegration = createMockIntegration(validId, "gemini");
        JSONObject validConfig = new JSONObject().put("api_key", "test-key");

        when(integrationService.getIntegrationById(validId, TEST_USER_ID, true))
            .thenReturn(validIntegration);
        when(integrationService.getIntegrationById(invalidId, TEST_USER_ID, true))
            .thenThrow(new RuntimeException("Integration not found"));

        List<String> integrationIds = Arrays.asList(validId, invalidId);

        // Act
        try (MockedStatic<com.github.istin.dmtools.server.util.IntegrationConfigMapper> mockedMapper = 
             mockStatic(com.github.istin.dmtools.server.util.IntegrationConfigMapper.class)) {
            
            mockedMapper.when(() -> com.github.istin.dmtools.server.util.IntegrationConfigMapper
                .mapIntegrationConfig(eq(validIntegration), any()))
                .thenReturn(validConfig);

            JSONObject result = integrationResolutionHelper.resolveIntegrationIds(integrationIds, TEST_USER_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.has("gemini"));
            assertFalse(result.has("invalid")); // Should not have failed integration
            assertEquals(1, result.length()); // Only one integration resolved
        }
        
        verify(integrationService).recordIntegrationUsage(validId);
        verify(integrationService, never()).recordIntegrationUsage(invalidId);
    }

    @Test
    void testResolveIntegrationIds_emptyList_returnsEmptyConfig() {
        // Arrange
        List<String> emptyList = new ArrayList<>();

        // Act
        JSONObject result = integrationResolutionHelper.resolveIntegrationIds(emptyList, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length());
        verifyNoInteractions(integrationService);
    }

    @Test
    void testResolveSingleIntegrationId_success_returnsSingleConfig() throws Exception {
        // Arrange
        IntegrationDto mockIntegration = createMockIntegration();
        JSONObject expectedConfig = new JSONObject().put("api_key", "test-key");

        when(integrationService.getIntegrationById(TEST_INTEGRATION_ID, TEST_USER_ID, true))
            .thenReturn(mockIntegration);

        // Act
        try (MockedStatic<com.github.istin.dmtools.server.util.IntegrationConfigMapper> mockedMapper = 
             mockStatic(com.github.istin.dmtools.server.util.IntegrationConfigMapper.class)) {
            
            mockedMapper.when(() -> com.github.istin.dmtools.server.util.IntegrationConfigMapper
                .mapIntegrationConfig(eq(mockIntegration), any()))
                .thenReturn(expectedConfig);

            JSONObject result = integrationResolutionHelper.resolveSingleIntegrationId(TEST_INTEGRATION_ID, TEST_USER_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.has(TEST_INTEGRATION_TYPE));
            assertEquals(expectedConfig.toString(), result.getJSONObject(TEST_INTEGRATION_TYPE).toString());
        }
    }

    @Test
    void testResolveSingleIntegrationId_integrationNotFound_throwsException() throws Exception {
        // Arrange
        when(integrationService.getIntegrationById(TEST_INTEGRATION_ID, TEST_USER_ID, true))
            .thenThrow(new RuntimeException("Integration not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            integrationResolutionHelper.resolveSingleIntegrationId(TEST_INTEGRATION_ID, TEST_USER_ID));

        assertTrue(exception.getMessage().contains("Failed to resolve integration ID"));
    }

    @Test
    void testResolveSingleIntegrationId_mappingFails_throwsException() throws Exception {
        // Arrange
        IntegrationDto mockIntegration = createMockIntegration();

        when(integrationService.getIntegrationById(TEST_INTEGRATION_ID, TEST_USER_ID, true))
            .thenReturn(mockIntegration);

        // Act & Assert
        try (MockedStatic<com.github.istin.dmtools.server.util.IntegrationConfigMapper> mockedMapper = 
             mockStatic(com.github.istin.dmtools.server.util.IntegrationConfigMapper.class)) {
            
            mockedMapper.when(() -> com.github.istin.dmtools.server.util.IntegrationConfigMapper
                .mapIntegrationConfig(eq(mockIntegration), any()))
                .thenThrow(new RuntimeException("Mapping failed"));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                integrationResolutionHelper.resolveSingleIntegrationId(TEST_INTEGRATION_ID, TEST_USER_ID));

            assertTrue(exception.getMessage().contains("Failed to resolve integration ID"));
        }
    }

    private IntegrationDto createMockIntegration() {
        return createMockIntegration(TEST_INTEGRATION_ID, TEST_INTEGRATION_TYPE);
    }

    private IntegrationDto createMockIntegration(String id, String type) {
        IntegrationDto integration = new IntegrationDto();
        integration.setId(id);
        integration.setType(type);
        integration.setName("Test Integration");

        Set<IntegrationConfigDto> configParams = new HashSet<>(Arrays.asList(
            createConfigParam("api_key", "test-api-key", true),
            createConfigParam("model", "gemini-pro", false)
        ));
        integration.setConfigParams(configParams);

        return integration;
    }

    private IntegrationConfigDto createConfigParam(String key, String value, boolean sensitive) {
        IntegrationConfigDto param = new IntegrationConfigDto();
        param.setParamKey(key);
        param.setParamValue(value);
        param.setSensitive(sensitive);
        return param;
    }
}
