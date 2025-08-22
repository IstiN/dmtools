package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.config.AuthProperties;
import com.github.istin.dmtools.auth.dto.AuthConfigurationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthConfigurationControllerTest {

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private AuthConfigurationController authConfigurationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAuthConfiguration_externalProvidersEnabled() {
        when(authProperties.getEnabledProviders()).thenReturn(Arrays.asList("google", "github"));
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(false);

        AuthConfigurationDto result = authConfigurationController.getAuthConfiguration();

        assertEquals(Arrays.asList("google", "github"), result.getEnabledProviders());
        assertEquals(false, result.isLocalStandaloneModeEnabled());
    }

    @Test
    void getAuthConfiguration_localStandaloneModeEnabled() {
        when(authProperties.getEnabledProviders()).thenReturn(Collections.emptyList());
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(true);

        AuthConfigurationDto result = authConfigurationController.getAuthConfiguration();

        assertEquals(Collections.emptyList(), result.getEnabledProviders());
        assertEquals(true, result.isLocalStandaloneModeEnabled());
    }

    @Test
    void getAuthConfiguration_noProvidersConfigured() {
        when(authProperties.getEnabledProviders()).thenReturn(Collections.emptyList());
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(true);

        AuthConfigurationDto result = authConfigurationController.getAuthConfiguration();

        assertEquals(Collections.emptyList(), result.getEnabledProviders());
        assertEquals(true, result.isLocalStandaloneModeEnabled());
    }
}
