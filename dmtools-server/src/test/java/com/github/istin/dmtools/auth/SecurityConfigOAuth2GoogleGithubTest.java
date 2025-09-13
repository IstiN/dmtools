package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.controller.AuthConfigurationController;
import com.github.istin.dmtools.auth.service.CustomOAuth2UserService;
import com.github.istin.dmtools.auth.service.CustomOidcUserService;
import com.github.istin.dmtools.server.DmToolsServerApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DmToolsServerApplication.class, properties = {
        "auth.enabled-providers=google,github",
        "spring.security.oauth2.client.registration.google.client-id=test-google-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-google-client-secret",
        "spring.security.oauth2.client.registration.google.scope=openid,profile,email",
        "spring.security.oauth2.client.registration.github.client-id=test-github-client-id",
        "spring.security.oauth2.client.registration.github.client-secret=test-github-client-secret",
        "spring.security.oauth2.client.registration.github.scope=read:user,user:email",
        "jwt.secret=a3b2c1d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2"
})
@AutoConfigureMockMvc
class SecurityConfigOAuth2GoogleGithubTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void testClientRegistrationRepository_oauth2Mode_withAllProviders() {
        // When auth.enabled-providers specifies google,github, it should not be in standalone mode
        assertFalse(authConfigProperties.isLocalStandaloneMode());

        // The clientRegistrationRepository should have both google and github configurations
        assertNotNull(clientRegistrationRepository.findByRegistrationId("google"));
        assertNotNull(clientRegistrationRepository.findByRegistrationId("github"));
        assertNull(clientRegistrationRepository.findByRegistrationId("microsoft")); // Not configured
    }
}