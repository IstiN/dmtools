package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthConfigurationController.class)
@ContextConfiguration(classes = {AuthConfigurationControllerTest.TestConfiguration.class})
class AuthConfigurationControllerTest {

    @MockBean
    private AuthConfigProperties authConfigProperties;

    @Configuration
    @Import(AuthConfigurationController.class)
    @EnableWebSecurity
    static class TestConfiguration {

        @Bean
        @Primary // Ensure this is the primary bean if multiple are present
        public ClientRegistrationRepository clientRegistrationRepository() {
            // Provide a default Google client registration for tests that need OAuth2
            return new InMemoryClientRegistrationRepository(googleClientRegistration());
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/api/auth/config").permitAll()
                    .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable());
            return http.build();
        }

        // Helper method to create a minimal Google ClientRegistration for testing
        private ClientRegistration googleClientRegistration() {
            return ClientRegistration.withRegistrationId("google")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v2/userinfo")
                .userNameAttributeName("sub") // Common for OAuth2 providers
                .build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAuthConfiguration_standaloneMode_noProviders() throws Exception {
        // Configure for standalone mode
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationMode").value("standalone"))
                .andExpect(jsonPath("$.enabledProviders").isEmpty());
    }

    @Test
    @TestPropertySource(properties = "auth.enabled-providers=google") // Explicitly enable google provider for this test
    void testGetAuthConfiguration_oauth2Mode_withProviders() throws Exception {
        // Configure for OAuth2 mode (not standalone)
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationMode").value("oauth2"))
                .andExpect(jsonPath("$.enabledProviders[0]").value("google")); // Expect 'google' to be enabled
    }

    @Test
    @TestPropertySource(properties = "auth.enabled-providers=google") // Explicitly enable google provider for this test
    void testGetAuthConfiguration_oauth2Mode_singleProvider() throws Exception {
        // Configure for OAuth2 mode (not standalone)
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationMode").value("oauth2"))
                .andExpect(jsonPath("$.enabledProviders[0]").value("google")); // Expect 'google' to be enabled
    }
}
