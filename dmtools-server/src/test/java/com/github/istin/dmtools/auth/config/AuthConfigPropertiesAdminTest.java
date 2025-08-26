package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootTest(classes = AuthConfigProperties.class, properties = {"auth.admin-username=testadmin", "auth.admin-password=testpass"})
@EnableConfigurationProperties(AuthConfigProperties.class)
class AuthConfigPropertiesAdminTest {

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Test
    void testAdminCredentials_customValues() {
        assertEquals("testadmin", authConfigProperties.getAdminUsername());
        assertEquals("testpass", authConfigProperties.getAdminPassword());
    }
}