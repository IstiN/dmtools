package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.service.CustomOAuth2UserService;
import com.github.istin.dmtools.auth.service.CustomOidcUserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {SecurityConfig.class, AuthConfigProperties.class})
@TestPropertySource(properties = {
    "auth.enabled-providers=",
    "auth.permitted-email-domains=",
    "auth.local.username=admin",
    "auth.local.password=admin"
})
public class SecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AuthConfigProperties authConfigProperties;

    // Mock other dependencies of SecurityConfig if they are not auto-configured
    @MockBean
    private EnhancedOAuth2AuthenticationSuccessHandler enhancedOAuth2AuthenticationSuccessHandler;
    @MockBean
    private AuthDebugFilter authDebugFilter;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;
    @MockBean
    private CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;
    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;
    @MockBean
    private CustomOidcUserService customOidcUserService;

    @Test
    void testClientRegistrationRepository_localStandaloneMode() {
        assertTrue(authConfigProperties.isLocalStandaloneMode());
        ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
        assertNotNull(repository);
        assertFalse(!((List<ClientRegistration>) ((DynamicClientRegistrationRepository) repository).findAll()).isEmpty(), "ClientRegistrationRepository should be empty in local standalone mode");
    }

    // Nested class to test OAuth2 enabled scenario
    @Nested
    @SpringBootTest(classes = {SecurityConfig.class, AuthConfigProperties.class})
    @TestPropertySource(properties = {
        "auth.enabled-providers=google,github",
        "auth.permitted-email-domains=",
        "auth.local.username=admin",
        "auth.local.password=admin"
    })
    class OAuth2EnabledTests {

        @Autowired
        private ApplicationContext context;

        @Autowired
        private AuthConfigProperties authConfigProperties;

        @MockBean
        private EnhancedOAuth2AuthenticationSuccessHandler enhancedOAuth2AuthenticationSuccessHandler;
        @MockBean
        private AuthDebugFilter authDebugFilter;
        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;
        @MockBean
        private CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;
        @MockBean
        private CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;
        @MockBean
        private CustomOAuth2UserService customOAuth2UserService;
        @MockBean
        private CustomOidcUserService customOidcUserService;

        @Test
        void testClientRegistrationRepository_withEnabledProviders() {
            assertFalse(authConfigProperties.isLocalStandaloneMode());
            ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
            assertNotNull(repository);
            assertTrue(!((List<ClientRegistration>) ((DynamicClientRegistrationRepository) repository).findAll()).isEmpty(), "ClientRegistrationRepository should not be empty");
            assertNotNull(repository.findByRegistrationId("google"));
            assertNotNull(repository.findByRegistrationId("github"));
            assertNull(repository.findByRegistrationId("microsoft"));
        }

        @Test
        void testSecurityFilterChain_oauth2Enabled() {
            assertFalse(authConfigProperties.isLocalStandaloneMode());
            assertTrue(context.containsBean("clientRegistrationRepository"));
        }
    }

    @Test
    void testSecurityFilterChain_oauth2Disabled() {
        assertTrue(authConfigProperties.isLocalStandaloneMode());
        // Similar to the above, check if context loads and ClientRegistrationRepository is empty.
        // The absence of OAuth2LoginConfigurer in the HttpSecurity setup is implicitly tested
        // by the fact that no ClientRegistrationRepository bean with actual registrations is available.
        ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
        assertFalse(!((List<ClientRegistration>) ((DynamicClientRegistrationRepository) repository).findAll()).isEmpty());
    }
}
