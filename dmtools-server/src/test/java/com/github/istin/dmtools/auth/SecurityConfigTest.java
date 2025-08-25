package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.controller.AuthConfigurationController;
import com.github.istin.dmtools.auth.service.CustomOAuth2UserService;
import com.github.istin.dmtools.auth.service.CustomOidcUserService;
import com.github.istin.dmtools.server.DmToolsServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DmToolsServerApplication.class, properties = {"auth.enabled-providers="})
@AutoConfigureMockMvc
class SecurityConfigTest {

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
    void testClientRegistrationRepository_localStandaloneMode_returnsEmpty() {
        // When auth.enabled-providers is empty, it should be in standalone mode
        assertTrue(authConfigProperties.isLocalStandaloneMode());

        // The clientRegistrationRepository bean should be empty
        List<ClientRegistration> registrations = StreamSupport.stream(((InMemoryClientRegistrationRepository) clientRegistrationRepository).spliterator(), false).toList();
        assertTrue(registrations.isEmpty());
    }

    @Test
    void testSecurityFilterChain_localStandaloneMode_accessLocalLogin() throws Exception {
        // In standalone mode, /api/auth/local-login should be accessible
        mockMvc.perform(get("/api/auth/local-login"))
                .andExpect(status().isOk()); // Should be permitted, then AuthController handles logic
    }

    @Test
    void testSecurityFilterChain_localStandaloneMode_accessAuthConfig() throws Exception {
        // In standalone mode, /api/auth/config should be accessible
        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk());
    }

    @Test
    void testSecurityFilterChain_localStandaloneMode_oauth2EndpointsDenied() throws Exception {
        // In standalone mode, OAuth2 authorization endpoint should be denied
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSecurityFilterChain_protectedEndpoint_requiresAuthentication() throws Exception {
        // Test that a protected endpoint requires authentication when not in standalone mode
        // We'll use a hypothetical protected endpoint '/api/protected'
        // In standalone mode, this should be denied if not explicitly permitted.
        // Since the property is {"auth.enabled-providers="}, it's standalone mode.
        // The default behavior for anyRequest().authenticated() should apply.
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isForbidden()); // Expecting forbidden as it's not permitted and not authenticated
    }
}