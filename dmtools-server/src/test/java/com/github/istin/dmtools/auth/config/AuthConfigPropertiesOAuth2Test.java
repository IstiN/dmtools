package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootTest(classes = AuthConfigProperties.class, properties = {"auth.enabled-providers=google"})
@EnableConfigurationProperties(AuthConfigProperties.class)
class AuthConfigPropertiesOAuth2Test {

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Test
    void testIsLocalStandaloneMode_disabled() {
        assertFalse(authConfigProperties.isLocalStandaloneMode());
    }
}