package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootTest(classes = AuthConfigProperties.class, properties = {"auth.enabled-providers=google", "auth.permitted-email-domains=example.com"})
@EnableConfigurationProperties(AuthConfigProperties.class)
class AuthConfigPropertiesSingleProviderTest {

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Test
    void testGetEnabledProvidersAsSet_withSingleProvider() {
        Set<String> providers = authConfigProperties.getEnabledProvidersAsSet();
        assertNotNull(providers);
        assertEquals(1, providers.size());
        assertTrue(providers.contains("google"));
    }

    @Test
    void testGetPermittedEmailDomainsAsSet_withSingleDomain() {
        Set<String> domains = authConfigProperties.getPermittedEmailDomainsAsSet();
        assertNotNull(domains);
        assertEquals(1, domains.size());
        assertTrue(domains.contains("example.com"));
    }

    @Test
    void testIsLocalStandaloneMode_disabled() {
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }
}
