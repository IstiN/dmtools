package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
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
        public ClientRegistrationRepository clientRegistrationRepository() {
            return new TestClientRegistrationRepository(Collections.emptyList());
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
    }

    // Test implementation that implements both ClientRegistrationRepository and Iterable
    static class TestClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {
        private final List<ClientRegistration> registrations;

        public TestClientRegistrationRepository(List<ClientRegistration> registrations) {
            this.registrations = registrations;
        }

        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return registrations.stream()
                    .filter(reg -> reg.getRegistrationId().equals(registrationId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Iterator<ClientRegistration> iterator() {
            return registrations.iterator();
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
    void testGetAuthConfiguration_oauth2Mode_withProviders() throws Exception {
        // Configure for OAuth2 mode (not standalone)
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        
        // Test with empty providers (as configured in TestConfiguration)
        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationMode").value("oauth2"))
                .andExpect(jsonPath("$.enabledProviders").isEmpty());
    }

    @Test
    void testGetAuthConfiguration_oauth2Mode_singleProvider() throws Exception {
        // Configure for OAuth2 mode (not standalone)
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        
        // Test with empty providers (as configured in TestConfiguration)
        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationMode").value("oauth2"))
                .andExpect(jsonPath("$.enabledProviders").isEmpty());
    }
}