package com.github.istin.dmtools.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AuthConfigProperties.class)
@EnableConfigurationProperties
public class AuthConfigPropertiesTest {

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers=google,github"})
    void testEnabledProviders_configured() {
        authConfigProperties.setEnabledProviders(Arrays.asList("google", "github"));
        assertFalse(authConfigProperties.isLocalStandaloneModeEnabled());
        assertEquals(Arrays.asList("google", "github"), authConfigProperties.getEnabledProviders());
    }

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers="})
    void testEnabledProviders_empty() {
        authConfigProperties.setEnabledProviders(Collections.emptyList());
        assertTrue(authConfigProperties.isLocalStandaloneModeEnabled());
        assertTrue(authConfigProperties.getEnabledProviders().isEmpty());
    }

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers=null"})
    void testEnabledProviders_null() {
        authConfigProperties.setEnabledProviders(null);
        assertTrue(authConfigProperties.isLocalStandaloneModeEnabled());
        assertNull(authConfigProperties.getEnabledProviders());
    }

    @Test
    @TestPropertySource(properties = {"auth.permitted-email-domains=example.com,test.org"})
    void testPermittedEmailDomains_configured() {
        authConfigProperties.setPermittedEmailDomains(Arrays.asList("example.com", "test.org"));
        assertTrue(authConfigProperties.isEmailDomainRestricted());
        assertEquals(Arrays.asList("example.com", "test.org"), authConfigProperties.getPermittedEmailDomains());
    }

    @Test
    @TestPropertySource(properties = {"auth.permitted-email-domains="})
    void testPermittedEmailDomains_empty() {
        authConfigProperties.setPermittedEmailDomains(Collections.emptyList());
        assertFalse(authConfigProperties.isEmailDomainRestricted());
        assertTrue(authConfigProperties.getPermittedEmailDomains().isEmpty());
    }

    @Test
    @TestPropertySource(properties = {"auth.permitted-email-domains=null"})
    void testPermittedEmailDomains_null() {
        authConfigProperties.setPermittedEmailDomains(null);
        assertFalse(authConfigProperties.isEmailDomainRestricted());
        assertNull(authConfigProperties.getPermittedEmailDomains());
    }

    @Test
    @TestPropertySource(properties = {"auth.admin-username=testadmin", "auth.admin-password=testpass"})
    void testAdminCredentials_configured() {
        authConfigProperties.setAdminUsername("testadmin");
        authConfigProperties.setAdminPassword("testpass");
        assertEquals("testadmin", authConfigProperties.getAdminUsername());
        assertEquals("testpass", authConfigProperties.getAdminPassword());
    }

    @Test
    void testAdminCredentials_defaults() {
        // Ensure defaults are 'admin'/'admin' when not explicitly set
        assertEquals("admin", authConfigProperties.getAdminUsername());
        assertEquals("admin", authConfigProperties.getAdminPassword());
    }
}
