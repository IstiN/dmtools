package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AuthConfigPropertiesTest {

    private AuthConfigProperties authConfigProperties;

    @BeforeEach
    void setUp() {
        authConfigProperties = new AuthConfigProperties();
    }

    @Test
    void testEnabledProviders_emptyWhenNotSet() {
        assertTrue(authConfigProperties.getEnabledProviders().isEmpty());
        assertTrue(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    void testEnabledProviders_returnsConfiguredProviders() {
        authConfigProperties.setEnabledProviders(Arrays.asList("Google", "github"));
        assertEquals(Arrays.asList("google", "github"), authConfigProperties.getEnabledProviders());
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    void testEnabledProviders_caseInsensitive() {
        authConfigProperties.setEnabledProviders(Collections.singletonList("GOOGLE"));
        assertEquals(Collections.singletonList("google"), authConfigProperties.getEnabledProviders());
    }

    @Test
    void testPermittedEmailDomains_emptyWhenNotSet() {
        assertTrue(authConfigProperties.getPermittedEmailDomains().isEmpty());
    }

    @Test
    void testPermittedEmailDomains_returnsConfiguredDomains() {
        authConfigProperties.setPermittedEmailDomains(Arrays.asList("example.com", "MYCOMPANY.ORG"));
        assertEquals(Arrays.asList("example.com", "mycompany.org"), authConfigProperties.getPermittedEmailDomains());
    }

    @Test
    void testPermittedEmailDomains_caseInsensitive() {
        authConfigProperties.setPermittedEmailDomains(Collections.singletonList("DOMAIN.COM"));
        assertEquals(Collections.singletonList("domain.com"), authConfigProperties.getPermittedEmailDomains());
    }

    @Test
    void testAdminCredentials_defaultNull() {
        assertNull(authConfigProperties.getAdminUsername());
        assertNull(authConfigProperties.getAdminPassword());
    }

    @Test
    void testAdminCredentials_returnsConfigured() {
        authConfigProperties.setAdminUsername("testadmin");
        authConfigProperties.setAdminPassword("testpass");
        assertEquals("testadmin", authConfigProperties.getAdminUsername());
        assertEquals("testpass", authConfigProperties.getAdminPassword());
    }

    @Test
    void testIsLocalStandaloneMode_trueWhenNoProviders() {
        authConfigProperties.setEnabledProviders(Collections.emptyList());
        assertTrue(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    void testIsLocalStandaloneMode_falseWhenProvidersExist() {
        authConfigProperties.setEnabledProviders(Collections.singletonList("google"));
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }
}
