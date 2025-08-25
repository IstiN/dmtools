package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Default test case - tests defaults with no properties set
@SpringBootTest(classes = AuthConfigProperties.class)
class AuthConfigPropertiesTest {

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Test
    void testGetEnabledProvidersAsSet_withEmptyString() {
        Set<String> providers = authConfigProperties.getEnabledProvidersAsSet();
        assertNotNull(providers);
        assertTrue(providers.isEmpty());
    }

    @Test
    void testGetPermittedEmailDomainsAsSet_withEmptyString() {
        Set<String> domains = authConfigProperties.getPermittedEmailDomainsAsSet();
        assertNotNull(domains);
        assertTrue(domains.isEmpty());
    }

    @Test
    void testIsLocalStandaloneMode_enabled() {
        assertTrue(authConfigProperties.isLocalStandaloneMode());
    }

    @Test
    void testAdminCredentials_defaultValues() {
        assertEquals("admin", authConfigProperties.getAdminUsername());
        assertEquals("admin", authConfigProperties.getAdminPassword());
    }
}
