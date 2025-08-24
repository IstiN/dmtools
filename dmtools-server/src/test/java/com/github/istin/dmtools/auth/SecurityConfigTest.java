package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.service.CustomOAuth2UserService;
import com.github.istin.dmtools.auth.service.CustomOidcUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private EnhancedOAuth2AuthenticationSuccessHandler enhancedOAuth2AuthenticationSuccessHandler;
    @Mock
    private AuthDebugFilter authDebugFilter;
    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Mock
    private CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;
    @Mock
    private CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;
    @Mock
    private CustomOAuth2UserService customOAuth2UserService;
    @Mock
    private CustomOidcUserService customOidcUserService;
    @Mock
    private AuthConfigProperties authConfigProperties;

    private List<ClientRegistration> defaultClientRegistrations;

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        // Initialize securityConfig with mocks
        securityConfig = new SecurityConfig(enhancedOAuth2AuthenticationSuccessHandler, authDebugFilter, jwtAuthenticationFilter, customOAuth2AuthenticationFailureHandler, "test");
        // Manually inject mocks that are @Autowired in SecurityConfig
        securityConfig.clientRegistrations = defaultClientRegistrations;
        securityConfig.customOAuth2AuthorizationRequestResolver = customOAuth2AuthorizationRequestResolver;
        securityConfig.customOAuth2UserService = customOAuth2UserService;
        securityConfig.customOidcUserService = customOidcUserService;
        securityConfig.authConfigProperties = authConfigProperties;

        defaultClientRegistrations = Arrays.asList(
                ClientRegistration.withRegistrationId("google").clientId("google-client").clientSecret("google-secret").authorizationUri("http://google.com/auth").tokenUri("http://google.com/token").redirectUri("{baseUrl}/login/oauth2/code/{registrationId}").scope("openid", "profile", "email").build(),
                ClientRegistration.withRegistrationId("microsoft").clientId("microsoft-client").clientSecret("microsoft-secret").authorizationUri("http://microsoft.com/auth").tokenUri("http://microsoft.com/token").redirectUri("{baseUrl}/login/oauth2/code/{registrationId}").scope("openid", "profile", "email").build(),
                ClientRegistration.withRegistrationId("github").clientId("github-client").clientSecret("github-secret").authorizationUri("http://github.com/auth").tokenUri("http://github.com/token").redirectUri("{baseUrl}/login/oauth2/code/{registrationId}").scope("user:email").build()
        );
    }

    @Test
    void testClientRegistrationRepository_localStandaloneMode() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);

        ClientRegistrationRepository repository = securityConfig.clientRegistrationRepository();
        assertNotNull(repository);
        assertNull(repository.findByRegistrationId("google"));
        assertNull(repository.findByRegistrationId("microsoft"));
        assertNull(repository.findByRegistrationId("github"));
    }

    @Test
    void testClientRegistrationRepository_noEnabledProvidersConfigured() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(Collections.emptyList());
        securityConfig.clientRegistrations = defaultClientRegistrations; // Ensure default registrations are available

        ClientRegistrationRepository repository = securityConfig.clientRegistrationRepository();
        assertNotNull(repository);
        assertNotNull(repository.findByRegistrationId("google"));
        assertNotNull(repository.findByRegistrationId("microsoft"));
        assertNotNull(repository.findByRegistrationId("github"));
    }

    @Test
    void testClientRegistrationRepository_specificProvidersEnabled() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(List.of("google", "github"));
        securityConfig.clientRegistrations = defaultClientRegistrations; // Ensure default registrations are available

        ClientRegistrationRepository repository = securityConfig.clientRegistrationRepository();
        assertNotNull(repository);
        assertNotNull(repository.findByRegistrationId("google"));
        assertNull(repository.findByRegistrationId("microsoft"));
        assertNotNull(repository.findByRegistrationId("github"));
    }

    @Test
    void testClientRegistrationRepository_noDefaultRegistrations() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(List.of("google"));
        securityConfig.clientRegistrations = Collections.emptyList(); // No default registrations

        ClientRegistrationRepository repository = securityConfig.clientRegistrationRepository();
        assertNotNull(repository);
        assertNull(repository.findByRegistrationId("google"));
    }

    @Test
    void testClientRegistrationRepository_enabledProvidersNotFoundInDefaults() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(List.of("unknownProvider"));
        securityConfig.clientRegistrations = defaultClientRegistrations; // Ensure default registrations are available

        ClientRegistrationRepository repository = securityConfig.clientRegistrationRepository();
        assertNotNull(repository);
        assertNull(repository.findByRegistrationId("unknownProvider"));
        assertNull(repository.findByRegistrationId("google")); // Ensure no other providers are enabled
    }

    @Test
    void testClientRegistrationRepository_emptyFilteredRegistrations() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authConfigProperties.getEnabledProvidersList()).thenReturn(List.of("nonexistent"));
        securityConfig.clientRegistrations = defaultClientRegistrations;

        ClientRegistrationRepository repository = securityConfig.clientRegistrationRepository();
        assertNotNull(repository);
        assertNull(repository.findByRegistrationId("google"));
        assertNull(repository.findByRegistrationId("microsoft"));
        assertNull(repository.findByRegistrationId("github"));
    }

    // Additional tests for securityFilterChain can be added here, but they typically require @SpringBootTest
    // and are more integration-level. For unit testing, focusing on the ClientRegistrationRepository logic is sufficient.
}
