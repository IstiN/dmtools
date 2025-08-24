package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.AuthConfigProperties;
import com.github.istin.dmtools.auth.model.AuthConfigurationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthConfigurationControllerTest {

    @Mock
    private AuthConfigProperties authConfigProperties;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @InjectMocks
    private AuthConfigurationController authConfigurationController;

    @BeforeEach
    void setUp() {
        // Ensure clientRegistrationRepository is injected as null if not explicitly mocked for some tests
        authConfigurationController = new AuthConfigurationController();
        authConfigurationController.authConfigProperties = authConfigProperties;
        authConfigurationController.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Test
    void testGetAuthConfiguration_localStandaloneMode() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);

        ResponseEntity<AuthConfigurationResponse> responseEntity = authConfigurationController.getAuthConfiguration();
        AuthConfigurationResponse response = responseEntity.getBody();

        assertTrue(response.isLocalStandaloneMode());
        assertTrue(response.getEnabledProviders().isEmpty());
    }

    @Test
    void testGetAuthConfiguration_oauthMode_noProvidersEnabled() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(clientRegistrationRepository).thenReturn(null); // Simulate no ClientRegistrationRepository available

        ResponseEntity<AuthConfigurationResponse> responseEntity = authConfigurationController.getAuthConfiguration();
        AuthConfigurationResponse response = responseEntity.getBody();

        assertTrue(response.getEnabledProviders().isEmpty());
        assertEquals(false, response.isLocalStandaloneMode());
    }

    @Test
    void testGetAuthConfiguration_oauthMode_withEnabledProviders() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);

        ClientRegistration google = ClientRegistration.withRegistrationId("google").clientId("id").clientSecret("secret").authorizationUri("uri").tokenUri("uri").redirectUri("uri").scope("scope").build();
        ClientRegistration github = ClientRegistration.withRegistrationId("github").clientId("id").clientSecret("secret").authorizationUri("uri").tokenUri("uri").redirectUri("uri").scope("scope").build();
        List<ClientRegistration> registrations = Arrays.asList(google, github);

        when(clientRegistrationRepository).thenReturn(new TestClientRegistrationRepository(registrations));

        ResponseEntity<AuthConfigurationResponse> responseEntity = authConfigurationController.getAuthConfiguration();
        AuthConfigurationResponse response = responseEntity.getBody();

        assertEquals(false, response.isLocalStandaloneMode());
        assertEquals(2, response.getEnabledProviders().size());
        assertTrue(response.getEnabledProviders().contains("google"));
        assertTrue(response.getEnabledProviders().contains("github"));
    }

    @Test
    void testGetAuthConfiguration_oauthMode_emptyClientRegistrationRepository() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(clientRegistrationRepository).thenReturn(new TestClientRegistrationRepository(Collections.emptyList()));

        ResponseEntity<AuthConfigurationResponse> responseEntity = authConfigurationController.getAuthConfiguration();
        AuthConfigurationResponse response = responseEntity.getBody();

        assertEquals(false, response.isLocalStandaloneMode());
        assertTrue(response.getEnabledProviders().isEmpty());
    }

    // Helper class for mocking ClientRegistrationRepository
    private static class TestClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {
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
        public java.util.Iterator<ClientRegistration> iterator() {
            return registrations.iterator();
        }
    }
}
