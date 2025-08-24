package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthConfigurationControllerTest {

    @Mock
    private AuthConfigProperties authConfigProperties;

    private AuthConfigurationController authConfigurationController;

    @BeforeEach
    void setUp() {
        authConfigurationController = new AuthConfigurationController(authConfigProperties);
    }

    @Test
    void testGetAuthConfig_localStandaloneMode() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getEnabledProviders()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = authConfigurationController.getAuthConfig();

        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("localStandaloneMode"));
        assertTrue(((List<?>) response.getBody().get("enabledProviders")).isEmpty());
    }

    @Test
    void testGetAuthConfig_externalProvidersEnabled() {
        List<String> enabledProviders = Arrays.asList("google", "github");
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authConfigProperties.getEnabledProviders()).thenReturn(enabledProviders);

        ResponseEntity<Map<String, Object>> response = authConfigurationController.getAuthConfig();

        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("localStandaloneMode"));
        assertEquals(enabledProviders, response.getBody().get("enabledProviders"));
    }
}
