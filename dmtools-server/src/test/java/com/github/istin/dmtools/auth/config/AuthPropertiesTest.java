package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AuthPropertiesTest {

    private AuthProperties authProperties;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
    }

    @Test
    void testDefaultValues() {
        assertTrue(authProperties.getEnabledProviders().isEmpty());
        assertTrue(authProperties.getPermittedEmailDomains().isEmpty());
        assertEquals("admin", authProperties.getAdminUsername());
        assertEquals("admin", authProperties.getAdminPassword());
        assertTrue(authProperties.isLocalStandaloneModeEnabled());
        assertFalse(authProperties.isEmailDomainRestrictionEnabled());
    }

    @Test
    void testEnabledProvidersConfiguration() {
        authProperties.setEnabledProviders(Arrays.asList("google", "github"));
        assertEquals(Arrays.asList("google", "github"), authProperties.getEnabledProviders());
        assertFalse(authProperties.isLocalStandaloneModeEnabled());

        authProperties.setEnabledProviders(Collections.emptyList());
        assertTrue(authProperties.getEnabledProviders().isEmpty());
        assertTrue(authProperties.isLocalStandaloneModeEnabled());

        authProperties.setEnabledProviders(null);
        assertTrue(authProperties.getEnabledProviders().isEmpty());
        assertTrue(authProperties.isLocalStandaloneModeEnabled());
    }

    @Test
    void testPermittedEmailDomainsConfiguration() {
        authProperties.setPermittedEmailDomains(Arrays.asList("example.com", "mycompany.org"));
        assertEquals(Arrays.asList("example.com", "mycompany.org"), authProperties.getPermittedEmailDomains());
        assertTrue(authProperties.isEmailDomainRestrictionEnabled());

        authProperties.setPermittedEmailDomains(Collections.emptyList());
        assertTrue(authProperties.getPermittedEmailDomains().isEmpty());
        assertFalse(authProperties.isEmailDomainRestrictionEnabled());

        authProperties.setPermittedEmailDomains(null);
        assertTrue(authProperties.getPermittedEmailDomains().isEmpty());
        assertFalse(authProperties.isEmailDomainRestrictionEnabled());
    }

    @Test
    void testAdminCredentialsConfiguration() {
        authProperties.setAdminUsername("testadmin");
        authProperties.setAdminPassword("testpass");
        assertEquals("testadmin", authProperties.getAdminUsername());
        assertEquals("testpass", authProperties.getAdminPassword());
    }
}
