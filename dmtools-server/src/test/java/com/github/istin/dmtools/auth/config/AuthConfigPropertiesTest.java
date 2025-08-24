package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AuthConfigPropertiesTest {

    @Test
    void testDefaultValues() {
        AuthConfigProperties properties = new AuthConfigProperties();
        assertTrue(properties.getEnabledProviders().isEmpty());
        assertTrue(properties.getPermittedEmailDomains().isEmpty());
        assertEquals("admin", properties.getAdminUsername());
        assertEquals("admin", properties.getAdminPassword());
        assertTrue(properties.isLocalStandaloneMode());
    }

    @Test
    void testEnabledProviders() {
        AuthConfigProperties properties = new AuthConfigProperties();
        properties.setEnabledProviders(Arrays.asList("google", "github"));
        assertEquals(2, properties.getEnabledProviders().size());
        assertFalse(properties.isLocalStandaloneMode());
    }

    @Test
    void testPermittedEmailDomains() {
        AuthConfigProperties properties = new AuthConfigProperties();
        properties.setPermittedEmailDomains(Arrays.asList("example.com", "mycompany.org"));
        assertEquals(2, properties.getPermittedEmailDomains().size());
    }

    @Test
    void testAdminCredentials() {
        AuthConfigProperties properties = new AuthConfigProperties();
        properties.setAdminUsername("testadmin");
        properties.setAdminPassword("testpass");
        assertEquals("testadmin", properties.getAdminUsername());
        assertEquals("testpass", properties.getAdminPassword());
    }

    @Test
    void testLocalStandaloneMode_emptyProviders() {
        AuthConfigProperties properties = new AuthConfigProperties();
        properties.setEnabledProviders(Collections.emptyList());
        assertTrue(properties.isLocalStandaloneMode());
    }

    @Test
    void testLocalStandaloneMode_nullProviders() {
        AuthConfigProperties properties = new AuthConfigProperties();
        properties.setEnabledProviders(null);
        assertTrue(properties.isLocalStandaloneMode());
    }
}
