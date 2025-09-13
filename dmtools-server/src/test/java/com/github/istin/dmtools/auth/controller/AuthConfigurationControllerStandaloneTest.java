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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthConfigurationController.class)
@TestPropertySource(properties = "auth.enabled-providers=")
class AuthConfigurationControllerStandaloneTest {

    @MockBean
    private AuthConfigProperties authConfigProperties;

    @Configuration
    @Import({AuthConfigurationController.class, AuthConfigProperties.class}) // Import AuthConfigProperties
    @EnableWebSecurity
    static class TestConfiguration {

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
}
