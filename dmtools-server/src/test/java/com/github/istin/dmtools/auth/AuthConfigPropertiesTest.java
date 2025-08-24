package com.github.istin.dmtools.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuthConfigPropertiesTest {

    private AuthConfigProperties authConfigProperties;

    @BeforeEach
    void setUp() {
        authConfigProperties = new AuthConfigProperties();
    }

    @Test
    void testGetEnabledProvidersList_empty() {
        authConfigProperties.setEnabledProviders("");
        assertTrue(authConfigProperties.getEnabledProvidersList().isEmpty());
    }

    @Test
    void testGetEnabledProvidersList_null() {
        authConfigProperties.setEnabledProviders(null);
        assertTrue(authConfigProperties.getEnabledProvidersList().isEmpty());
    }

    @Test
    void testGetEnabledProvidersList_commaSeparated() {
        authConfigProperties.setEnabledProviders("google, microsoft,github");
        List<String> expected = Arrays.asList("google", "microsoft", "github");
        assertEquals(expected, authConfigProperties.getEnabledProvidersList());
    }

    @Test
    void testGetEnabledProvidersList_withSpaces() {
        authConfigProperties.setEnabledProviders(" google ,  microsoft ");
        List<String> expected = Arrays.asList("google", "microsoft");
        assertEquals(expected, authConfigProperties.getEnabledProvidersList());
    }

    @Test
    void testGetPermittedEmailDomainsList_empty() {
        authConfigProperties.setPermittedEmailDomains("");
        assertTrue(authConfigProperties.getPermittedEmailDomainsList().isEmpty());
    }

    @Test
    void testGetPermittedEmailDomainsList_null() {
        authConfigProperties.setPermittedEmailDomains(null);
        assertTrue(authConfigProperties.getPermittedEmailDomainsList().isEmpty());
    }

    @Test
    void testGetPermittedEmailDomainsList_commaSeparated() {
        authConfigProperties.setPermittedEmailDomains("example.com, mycompany.org");
        List<String> expected = Arrays.asList("example.com", "mycompany.org");
        assertEquals(expected, authConfigProperties.getPermittedEmailDomainsList());
    }

    @Test
    void testIsLocalStandaloneMode_true() {
        authConfigProperties.setEnabledProviders("");
        assertTrue(authConfigProperties.isLocalStandaloneMode());

        authConfigProperties.setEnabledProviders(null);
        assertTrue(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    void testIsLocalStandaloneMode_false() {
        authConfigProperties.setEnabledProviders("google");
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    void testAdminCredentials_defaultValues() {
        assertEquals("admin", authConfigProperties.getAdminUsername());
        assertEquals("admin", authConfigProperties.getAdminPassword());
    }

    @Test
    void testAdminCredentials_customValues() {
        authConfigProperties.setAdminUsername("testadmin");
        authConfigProperties.setAdminPassword("testpass");
        assertEquals("testadmin", authConfigProperties.getAdminUsername());
        assertEquals("testpass", authConfigProperties.getAdminPassword());
    }
}
