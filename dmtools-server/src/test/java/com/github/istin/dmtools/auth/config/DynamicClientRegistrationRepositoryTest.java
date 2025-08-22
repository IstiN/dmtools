package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DynamicClientRegistrationRepositoryTest {

    @Mock
    private AuthProperties authProperties;

    private DynamicClientRegistrationRepository repository;

    private final String GOOGLE_CLIENT_ID = "google-client-id";
    private final String GOOGLE_CLIENT_SECRET = "google-client-secret";
    private final String MICROSOFT_CLIENT_ID = "microsoft-client-id";
    private final String MICROSOFT_CLIENT_SECRET = "microsoft-client-secret";
    private final String GITHUB_CLIENT_ID = "github-client-id";
    private final String GITHUB_CLIENT_SECRET = "github-client-secret";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new DynamicClientRegistrationRepository(authProperties,
                GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,
                MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET,
                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
    }

    @Test
    void findByRegistrationId_googleEnabled() {
        when(authProperties.getEnabledProviders()).thenReturn(Arrays.asList("google"));
        repository = new DynamicClientRegistrationRepository(authProperties,
                GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,
                MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET,
                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
        ClientRegistration google = repository.findByRegistrationId("google");
        assertNotNull(google);
        assertEquals("google", google.getRegistrationId());
    }

    @Test
    void findByRegistrationId_microsoftEnabled() {
        when(authProperties.getEnabledProviders()).thenReturn(Arrays.asList("microsoft"));
        repository = new DynamicClientRegistrationRepository(authProperties,
                GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,
                MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET,
                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
        ClientRegistration microsoft = repository.findByRegistrationId("microsoft");
        assertNotNull(microsoft);
        assertEquals("microsoft", microsoft.getRegistrationId());
    }

    @Test
    void findByRegistrationId_githubEnabled() {
        when(authProperties.getEnabledProviders()).thenReturn(Arrays.asList("github"));
        repository = new DynamicClientRegistrationRepository(authProperties,
                GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,
                MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET,
                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
        ClientRegistration github = repository.findByRegistrationId("github");
        assertNotNull(github);
        assertEquals("github", github.getRegistrationId());
    }

    @Test
    void findByRegistrationId_allEnabledByDefault() {
        when(authProperties.getEnabledProviders()).thenReturn(Collections.emptyList());
        repository = new DynamicClientRegistrationRepository(authProperties,
                GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,
                MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET,
                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
        assertNotNull(repository.findByRegistrationId("google"));
        assertNotNull(repository.findByRegistrationId("microsoft"));
        assertNotNull(repository.findByRegistrationId("github"));
    }

    @Test
    void findByRegistrationId_googleDisabled() {
        when(authProperties.getEnabledProviders()).thenReturn(Arrays.asList("microsoft", "github"));
        repository = new DynamicClientRegistrationRepository(authProperties,
                GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,
                MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET,
                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
        assertNull(repository.findByRegistrationId("google"));
    }

    @Test
    void findByRegistrationId_noClientSecretProvided() {
        when(authProperties.getEnabledProviders()).thenReturn(Arrays.asList("google"));
        repository = new DynamicClientRegistrationRepository(authProperties,
                GOOGLE_CLIENT_ID, "", // Empty secret
                MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET,
                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
        assertNull(repository.findByRegistrationId("google"));
    }

    @Test
    void findByRegistrationId_unknownRegistrationId() {
        when(authProperties.getEnabledProviders()).thenReturn(Arrays.asList("google"));
        repository = new DynamicClientRegistrationRepository(authProperties,
                GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,
                MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET,
                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
        assertNull(repository.findByRegistrationId("unknown"));
    }
}
