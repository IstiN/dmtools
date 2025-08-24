package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.AuthConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthConfigurationControllerTest {

    @Mock
    private AuthConfigProperties authConfigProperties;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @InjectMocks
    private AuthConfigurationController authConfigurationController;

    private ClientRegistration googleClientRegistration;
    private ClientRegistration githubClientRegistration;

    @BeforeEach
    void setUp() {
        googleClientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("google-client-id")
                .clientSecret("google-client-secret")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .userNameAttributeName("name")
                .clientName("Google")
                .build();

        githubClientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("github-client-id")
                .clientSecret("github-client-secret")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("user:email")
                .userNameAttributeName("name")
                .clientName("GitHub")
                .build();
    }

    @Test
    void testGetAuthConfiguration_localStandaloneModeEnabled() {
        when(authConfigProperties.isLocalStandaloneModeEnabled()).thenReturn(true);

        AuthConfiguration config = authConfigurationController.getAuthConfiguration();

        assertTrue(config.isLocalStandaloneMode());
        assertTrue(config.getEnabledProviders().isEmpty());
    }

    @Test
    void testGetAuthConfiguration_oauthProvidersEnabled() {
        when(authConfigProperties.isLocalStandaloneModeEnabled()).thenReturn(false);
        when(authConfigProperties.getEnabledProviders()).thenReturn(Arrays.asList("google", "github"));
        when(clientRegistrationRepository.isPresent()).thenReturn(true);

        // Mock the iterator behavior for forEach
        when(clientRegistrationRepository.get().iterator()).thenReturn(Arrays.asList(googleClientRegistration, githubClientRegistration).iterator());

        AuthConfiguration config = authConfigurationController.getAuthConfiguration();

        assertFalse(config.isLocalStandaloneMode());
        assertEquals(2, config.getEnabledProviders().size());
        assertTrue(config.getEnabledProviders().contains("google"));
        assertTrue(config.getEnabledProviders().contains("github"));
    }

    @Test
    void testGetAuthConfiguration_oauthProvidersEnabled_filtered() {
        when(authConfigProperties.isLocalStandaloneModeEnabled()).thenReturn(false);
        when(authConfigProperties.getEnabledProviders()).thenReturn(Collections.singletonList("google"));
        when(clientRegistrationRepository.isPresent()).thenReturn(true);

        // Mock the iterator behavior for forEach
        when(clientRegistrationRepository.get().iterator()).thenReturn(Arrays.asList(googleClientRegistration, githubClientRegistration).iterator());

        AuthConfiguration config = authConfigurationController.getAuthConfiguration();

        assertFalse(config.isLocalStandaloneMode());
        assertEquals(1, config.getEnabledProviders().size());
        assertTrue(config.getEnabledProviders().contains("google"));
        assertFalse(config.getEnabledProviders().contains("github"));
    }

    @Test
    void testGetAuthConfiguration_noClientRegistrationRepository() {
        when(authConfigProperties.isLocalStandaloneModeEnabled()).thenReturn(false);
        when(clientRegistrationRepository.isPresent()).thenReturn(false);

        AuthConfiguration config = authConfigurationController.getAuthConfiguration();

        assertFalse(config.isLocalStandaloneMode());
        assertTrue(config.getEnabledProviders().isEmpty());
    }

    @Test
    void testGetAuthConfiguration_noEnabledProvidersConfigured() {
        when(authConfigProperties.isLocalStandaloneModeEnabled()).thenReturn(false);
        when(authConfigProperties.getEnabledProviders()).thenReturn(Collections.emptyList());
        when(clientRegistrationRepository.isPresent()).thenReturn(true);

        // Mock the iterator behavior for forEach
        when(clientRegistrationRepository.get().iterator()).thenReturn(Arrays.asList(googleClientRegistration, githubClientRegistration).iterator());

        AuthConfiguration config = authConfigurationController.getAuthConfiguration();

        assertFalse(config.isLocalStandaloneMode());
        assertTrue(config.getEnabledProviders().isEmpty());
    }
}
