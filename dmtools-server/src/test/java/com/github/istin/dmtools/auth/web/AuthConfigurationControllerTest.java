package com.github.istin.dmtools.auth.web;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.web.dto.AuthConfigurationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthConfigurationControllerTest {

    private AuthConfigProperties authConfigProperties;
    private AuthConfigurationController authConfigurationController;

    @BeforeEach
    void setUp() {
        authConfigProperties = Mockito.mock(AuthConfigProperties.class);
        authConfigurationController = new AuthConfigurationController(authConfigProperties);
    }

    @Test
    void getAuthConfiguration_localStandaloneMode() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getEnabledProviders()).thenReturn(Collections.emptyList());

        AuthConfigurationDto dto = authConfigurationController.getAuthConfiguration();

        assertEquals(true, dto.isLocalStandaloneMode());
        assertEquals(Collections.emptyList(), dto.getEnabledProviders());
    }

    @Test
    void getAuthConfiguration_oauthProvidersEnabled() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authConfigProperties.getEnabledProviders()).thenReturn(Arrays.asList("google", "github"));

        AuthConfigurationDto dto = authConfigurationController.getAuthConfiguration();

        assertEquals(false, dto.isLocalStandaloneMode());
        assertEquals(Arrays.asList("google", "github"), dto.getEnabledProviders());
    }
}
