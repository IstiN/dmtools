package com.github.istin.dmtools.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class DynamicClientRegistrationRepositoryTest {

    private AuthConfigProperties authConfigProperties;
    private DynamicClientRegistrationRepository repository;

    @BeforeEach
    void setUp() {
        authConfigProperties = Mockito.mock(AuthConfigProperties.class);
        repository = new DynamicClientRegistrationRepository(authConfigProperties);
    }

    @Test
    void testRefreshRegistrations_noProvidersEnabled() {
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Collections.emptyList());
        repository.refreshRegistrations();
        assertFalse(repository.hasRegistrations());
        assertNull(repository.findByRegistrationId("google"));
    }

    @Test
    void testRefreshRegistrations_withProvidersEnabled() {
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Arrays.asList("google", "github"));
        repository.refreshRegistrations();
        assertTrue(repository.hasRegistrations());
        assertNotNull(repository.findByRegistrationId("google"));
        assertNotNull(repository.findByRegistrationId("github"));
        assertNull(repository.findByRegistrationId("microsoft"));
    }

    @Test
    void testFindByRegistrationId_existing() {
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Collections.singletonList("google"));
        repository.refreshRegistrations();
        ClientRegistration googleRegistration = repository.findByRegistrationId("google");
        assertNotNull(googleRegistration);
        assertEquals("google", googleRegistration.getRegistrationId());
    }

    @Test
    void testFindByRegistrationId_nonExisting() {
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Collections.singletonList("google"));
        repository.refreshRegistrations();
        assertNull(repository.findByRegistrationId("microsoft"));
    }

    @Test
    void testFindAll() {
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Arrays.asList("google", "github"));
        repository.refreshRegistrations();
        List<ClientRegistration> allRegistrations = (List<ClientRegistration>) repository.findAll();
        assertEquals(2, allRegistrations.size());
        assertTrue(allRegistrations.stream().anyMatch(r -> r.getRegistrationId().equals("google")));
        assertTrue(allRegistrations.stream().anyMatch(r -> r.getRegistrationId().equals("github")));
    }

    @Test
    void testHasRegistrations_true() {
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Collections.singletonList("google"));
        repository.refreshRegistrations();
        assertTrue(repository.hasRegistrations());
    }

    @Test
    void testHasRegistrations_false() {
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Collections.emptyList());
        repository.refreshRegistrations();
        assertFalse(repository.hasRegistrations());
    }
}
