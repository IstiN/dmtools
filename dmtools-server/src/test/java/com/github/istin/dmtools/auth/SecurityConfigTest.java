package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private AuthProperties authProperties;

    

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        // Mock other dependencies of SecurityConfig constructor if needed, or pass null for now if not used in tested methods
        securityConfig = new SecurityConfig(null, null, null, null, authProperties, "test");
    }

    @Test
    void testClientRegistrationRepository_localStandaloneMode() {
        when(authProperties.isLocalStandaloneMode()).thenReturn(true);

        InMemoryClientRegistrationRepository testClientRegistrationRepository = new InMemoryClientRegistrationRepository();
        ClientRegistrationRepository result = securityConfig.clientRegistrationRepository(testClientRegistrationRepository);
        assertTrue(result instanceof InMemoryClientRegistrationRepository);
        assertNull(result.findByRegistrationId("google")); // Should be empty
    }

    @Test
    void testClientRegistrationRepository_noEnabledProviders() {
        when(authProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authProperties.getEnabledProviders()).thenReturn(Collections.emptyList());

        InMemoryClientRegistrationRepository testClientRegistrationRepository = new InMemoryClientRegistrationRepository();
        ClientRegistrationRepository result = securityConfig.clientRegistrationRepository(testClientRegistrationRepository);
        assertEquals(testClientRegistrationRepository, result);
    }

    @Test
    void testClientRegistrationRepository_withEnabledProviders() {
        when(authProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authProperties.getEnabledProviders()).thenReturn(List.of("google", "github"));

        ClientRegistration google = ClientRegistration.withRegistrationId("google").clientId("google-client").clientSecret("google-secret").authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri("http://localhost").scope("openid", "profile", "email").authorizationUri("https://accounts.google.com/o/oauth2/v2/auth").tokenUri("https://www.googleapis.com/oauth2/v4/token").jwkSetUri("https://www.googleapis.com/oauth2/v3/certs").userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo").userNameAttributeName("sub").clientName("Google").build();
        ClientRegistration github = ClientRegistration.withRegistrationId("github").clientId("github-client").clientSecret("github-secret").authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri("http://localhost").scope("user:email").authorizationUri("https://github.com/login/oauth/authorize").tokenUri("https://github.com/login/oauth/access_token").userInfoUri("https://api.github.com/user").userNameAttributeName("id").clientName("GitHub").build();
        ClientRegistration microsoft = ClientRegistration.withRegistrationId("microsoft").clientId("microsoft-client").clientSecret("microsoft-secret").authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri("http://localhost").scope("openid", "profile", "email").authorizationUri("https://login.microsoftonline.com/common/oauth2/v2.0/authorize").tokenUri("https://login.microsoftonline.com/common/oauth2/v2.0/token").jwkSetUri("https://login.microsoftonline.com/common/discovery/v2.0/keys").userInfoUri("https://graph.microsoft.com/oidc/userinfo").userNameAttributeName("sub").clientName("Microsoft").build();

        InMemoryClientRegistrationRepository testClientRegistrationRepository = new InMemoryClientRegistrationRepository(google, github, microsoft);

        ClientRegistrationRepository result = securityConfig.clientRegistrationRepository(testClientRegistrationRepository);
        assertTrue(result instanceof InMemoryClientRegistrationRepository);
        assertNotNull(result.findByRegistrationId("google"));
        assertNotNull(result.findByRegistrationId("github"));
        assertNull(result.findByRegistrationId("microsoft")); // Should be filtered out
    }
}