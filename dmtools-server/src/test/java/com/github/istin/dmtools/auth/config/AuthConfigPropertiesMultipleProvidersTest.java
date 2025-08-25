package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootTest(classes = AuthConfigProperties.class, properties = {"auth.enabled-providers=google,github", "auth.permitted-email-domains=example.com,test.org"})
@EnableConfigurationProperties(AuthConfigProperties.class)
class AuthConfigPropertiesMultipleProvidersTest {

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Test
    void testGetEnabledProvidersAsSet_withMultipleProviders() {
        Set<String> providers = authConfigProperties.getEnabledProvidersAsSet();
        assertNotNull(providers);
        assertEquals(2, providers.size());
        assertTrue(providers.contains("google"));
        assertTrue(providers.contains("github"));
    }

    @Test
    void testGetPermittedEmailDomainsAsSet_withMultipleDomains() {
        Set<String> domains = authConfigProperties.getPermittedEmailDomainsAsSet();
        assertNotNull(domains);
        assertEquals(2, domains.size());
        assertTrue(domains.contains("example.com"));
        assertTrue(domains.contains("test.org"));
    }

    @Test
    void testIsLocalStandaloneMode_disabled() {
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }
}
