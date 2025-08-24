package com.github.istin.dmtools.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthConfigurationControllerTest {

    @Mock
    private AuthConfigProperties authConfigProperties;

    @InjectMocks
    private AuthConfigurationController authConfigurationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authConfigurationController).build();
    }

    @Test
    void testGetAuthConfig_localStandaloneMode() throws Exception {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localStandaloneMode").value(true))
                .andExpect(jsonPath("$.enabledProviders").isEmpty());

        Map<String, Object> response = authConfigurationController.getAuthConfig();
        assertEquals(true, response.get("localStandaloneMode"));
        assertEquals(Collections.emptyList(), response.get("enabledProviders"));
    }

    @Test
    void testGetAuthConfig_withExternalProviders() throws Exception {
        List<String> providers = Arrays.asList("google", "github");
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(providers);

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localStandaloneMode").value(false))
                .andExpect(jsonPath("$.enabledProviders").isArray())
                .andExpect(jsonPath("$.enabledProviders", hasSize(2)))
                .andExpect(jsonPath("$.enabledProviders", hasItem("google")))
                .andExpect(jsonPath("$.enabledProviders", hasItem("github")));

        Map<String, Object> response = authConfigurationController.getAuthConfig();
        assertEquals(false, response.get("localStandaloneMode"));
        assertEquals(providers, response.get("enabledProviders"));
    }
}
