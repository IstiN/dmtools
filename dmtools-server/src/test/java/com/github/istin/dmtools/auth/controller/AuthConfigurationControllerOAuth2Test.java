package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthConfigurationController.class)
@ContextConfiguration(classes = {AuthConfigurationControllerOAuth2Test.TestConfiguration.class})
@TestPropertySource(properties = {"auth.enabled-providers=google,github"})
class AuthConfigurationControllerOAuth2Test {

    @Configuration
    @EnableConfigurationProperties(AuthConfigProperties.class)
    @Import(AuthConfigurationController.class)
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
        
        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration google = ClientRegistration.withRegistrationId("google")
                    .clientId("google-client-id")
                    .clientSecret("google-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .userNameAttributeName("email")
                    .build();

            ClientRegistration github = ClientRegistration.withRegistrationId("github")
                    .clientId("github-client-id")
                    .clientSecret("github-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationUri("https://github.com/login/oauth/authorize")
                    .tokenUri("https://github.com/login/oauth/access_token")
                    .userInfoUri("https://api.github.com/user")
                    .redirectUri("http://localhost:8080/login/oauth2/code/github")
                    .userNameAttributeName("id")
                    .build();

            return new TestClientRegistrationRepository(Arrays.asList(google, github));
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
    void testGetAuthConfiguration_oauth2Mode_withProviders() throws Exception {
        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationMode").value("oauth2"))
                .andExpect(jsonPath("$.enabledProviders").isArray())
                .andExpect(jsonPath("$.enabledProviders[0]").value("google"))
                .andExpect(jsonPath("$.enabledProviders[1]").value("github"));
    }
}
