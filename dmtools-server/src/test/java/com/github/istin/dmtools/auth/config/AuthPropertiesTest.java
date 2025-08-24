package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testSetEnabledProviders_commaSeparated() {
        authProperties.setEnabledProviders("google,microsoft,github");
        assertEquals(Arrays.asList("google", "microsoft", "github"), authProperties.getEnabledProviders());
    }

    @Test
    void testSetEnabledProviders_emptyString() {
        authProperties.setEnabledProviders("");
        assertTrue(authProperties.getEnabledProviders().isEmpty());
    }

    @Test
    void testSetEnabledProviders_null() {
        authProperties.setEnabledProviders(null);
        assertTrue(authProperties.getEnabledProviders().isEmpty());
    }

    @Test
    void testSetEnabledProviders_withSpaces() {
        authProperties.setEnabledProviders(" google , microsoft ");
        assertEquals(Arrays.asList("google", "microsoft"), authProperties.getEnabledProviders());
    }

    @Test
    void testSetPermittedEmailDomains_commaSeparated() {
        authProperties.setPermittedEmailDomains("example.com,mycompany.org");
        assertEquals(Arrays.asList("example.com", "mycompany.org"), authProperties.getPermittedEmailDomains());
    }

    @Test
    void testSetPermittedEmailDomains_emptyString() {
        authProperties.setPermittedEmailDomains("");
        assertTrue(authProperties.getPermittedEmailDomains().isEmpty());
    }

    @Test
    void testSetPermittedEmailDomains_null() {
        authProperties.setPermittedEmailDomains(null);
        assertTrue(authProperties.getPermittedEmailDomains().isEmpty());
    }

    @Test
    void testSetPermittedEmailDomains_withSpaces() {
        authProperties.setPermittedEmailDomains(" example.com , mycompany.org ");
        assertEquals(Arrays.asList("example.com", "mycompany.org"), authProperties.getPermittedEmailDomains());
    }

    @Test
    void testGetAdminUsername_defaultValue() {
        assertEquals("admin", authProperties.getAdminUsername());
    }

    @Test
    void testSetAdminUsername() {
        authProperties.setAdminUsername("newadmin");
        assertEquals("newadmin", authProperties.getAdminUsername());
    }

    @Test
    void testGetAdminPassword_defaultValue() {
        assertEquals("admin", authProperties.getAdminPassword());
    }

    @Test
    void testSetAdminPassword() {
        authProperties.setAdminPassword("newpassword");
        assertEquals("newpassword", authProperties.getAdminPassword());
    }

    @Test
    void testIsLocalStandaloneMode_trueWhenNoProviders() {
        authProperties.setEnabledProviders("");
        assertTrue(authProperties.isLocalStandaloneMode());
    }

    @Test
    void testIsLocalStandaloneMode_falseWhenProvidersExist() {
        authProperties.setEnabledProviders("google");
        assertFalse(authProperties.isLocalStandaloneMode());
    }

    @Test
    void testIsLocalStandaloneMode_trueWhenNullProviders() {
        authProperties.setEnabledProviders(null);
        assertTrue(authProperties.isLocalStandaloneMode());
    }
}
