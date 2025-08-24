package com.github.istin.dmtools.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AuthConfigProperties.class)
class AuthConfigPropertiesTest {

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Test
    void testDefaultProperties() {
        assertTrue(authConfigProperties.getEnabledProvidersList().isEmpty());
        assertTrue(authConfigProperties.getPermittedEmailDomainsList().isEmpty());
        assertEquals("admin", authConfigProperties.getAdminUsername());
        assertEquals("admin", authConfigProperties.getAdminPassword());
        assertTrue(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    @TestPropertySource(properties = {
            "auth.enabled-providers=google,microsoft",
            "auth.permitted-email-domains=example.com,test.org",
            "auth.admin-username=testadmin",
            "auth.admin-password=testpass"
    })
    void testCustomProperties() {
        List<String> enabledProviders = authConfigProperties.getEnabledProvidersList();
        assertFalse(enabledProviders.isEmpty());
        assertEquals(2, enabledProviders.size());
        assertTrue(enabledProviders.contains("google"));
        assertTrue(enabledProviders.contains("microsoft"));

        List<String> permittedDomains = authConfigProperties.getPermittedEmailDomainsList();
        assertFalse(permittedDomains.isEmpty());
        assertEquals(2, permittedDomains.size());
        assertTrue(permittedDomains.contains("example.com"));
        assertTrue(permittedDomains.contains("test.org"));

        assertEquals("testadmin", authConfigProperties.getAdminUsername());
        assertEquals("testpass", authConfigProperties.getAdminPassword());
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    @TestPropertySource(properties = {
            "auth.enabled-providers=",
            "auth.permitted-email-domains="
    })
    void testEmptyProperties() {
        assertTrue(authConfigProperties.getEnabledProvidersList().isEmpty());
        assertTrue(authConfigProperties.getPermittedEmailDomainsList().isEmpty());
        assertTrue(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    @TestPropertySource(properties = {
            "auth.enabled-providers=  google ,  microsoft  ",
            "auth.permitted-email-domains=  example.com ,  test.org  "
    })
    void testTrimmedProperties() {
        List<String> enabledProviders = authConfigProperties.getEnabledProvidersList();
        assertEquals(2, enabledProviders.size());
        assertTrue(enabledProviders.contains("google"));
        assertTrue(enabledProviders.contains("microsoft"));

        List<String> permittedDomains = authConfigProperties.getPermittedEmailDomainsList();
        assertEquals(2, permittedDomains.size());
        assertTrue(permittedDomains.contains("example.com"));
        assertTrue(permittedDomains.contains("test.org"));
    }

    @Test
    @TestPropertySource(properties = {
            "auth.enabled-providers=google,,microsoft",
            "auth.permitted-email-domains=example.com,,test.org"
    })
    void testPropertiesWithEmptyEntries() {
        List<String> enabledProviders = authConfigProperties.getEnabledProvidersList();
        assertEquals(2, enabledProviders.size());
        assertTrue(enabledProviders.contains("google"));
        assertTrue(enabledProviders.contains("microsoft"));

        List<String> permittedDomains = authConfigProperties.getPermittedEmailDomainsList();
        assertEquals(2, permittedDomains.size());
        assertTrue(permittedDomains.contains("example.com"));
        assertTrue(permittedDomains.contains("test.org"));
    }

    @Test
    @TestPropertySource(properties = {
            "auth.enabled-providers=google",
            "auth.permitted-email-domains=example.com"
    })
    void testSingleProviderAndDomain() {
        List<String> enabledProviders = authConfigProperties.getEnabledProvidersList();
        assertEquals(1, enabledProviders.size());
        assertTrue(enabledProviders.contains("google"));

        List<String> permittedDomains = authConfigProperties.getPermittedEmailDomainsList();
        assertEquals(1, permittedDomains.size());
        assertTrue(permittedDomains.contains("example.com"));
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    @TestPropertySource(properties = {
            "auth.enabled-providers=google"
    })
    void testNotLocalStandaloneModeWhenProvidersExist() {
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    @TestPropertySource(properties = {
            "auth.enabled-providers="
    })
    void testLocalStandaloneModeWhenProvidersEmpty() {
        assertTrue(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    @TestPropertySource(properties = {
            "auth.enabled-providers= "
    })
    void testLocalStandaloneModeWhenProvidersBlank() {
        assertTrue(authConfigProperties.isLocalStandaloneMode());
    }
}
