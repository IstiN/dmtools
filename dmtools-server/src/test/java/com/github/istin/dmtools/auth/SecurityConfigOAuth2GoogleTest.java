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

@SpringBootTest(classes = DmToolsServerApplication.class, properties = {"auth.enabled-providers=google"})
@AutoConfigureMockMvc
class SecurityConfigOAuth2GoogleTest {

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
    void testClientRegistrationRepository_oauth2Mode_withSpecificProviders() {
        // When auth.enabled-providers specifies google, it should not be in standalone mode
        assertFalse(authConfigProperties.isLocalStandaloneMode());

        // The clientRegistrationRepository bean should contain only google
        List<ClientRegistration> registrations = StreamSupport.stream(((InMemoryClientRegistrationRepository) clientRegistrationRepository).spliterator(), false).toList();
        assertEquals(1, registrations.size());
        assertEquals("google", registrations.get(0).getRegistrationId());
    }

    @Test
    void testUserDetailsService_oauth2Mode_nonAdminUser() {
        // In OAuth2 mode, userDetailsService should return a generic user (or throw exception if not found)
        assertFalse(authConfigProperties.isLocalStandaloneMode());

        UserDetails genericUser = userDetailsService.loadUserByUsername("anyuser");
        assertNotNull(genericUser);
        assertEquals("anyuser", genericUser.getUsername());
        assertTrue(passwordEncoder.matches("dummy", genericUser.getPassword())); // Dummy password from SecurityConfig
        assertTrue(genericUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testSecurityFilterChain_oauth2Mode_denyLocalLogin() throws Exception {
        // In OAuth2 mode, /api/auth/local-login should be denied
        mockMvc.perform(get("/api/auth/local-login"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSecurityFilterChain_oauth2Mode_accessAuthConfig() throws Exception {
        // In OAuth2 mode, /api/auth/config should be accessible
        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk());
    }

    @Test
    void testSecurityFilterChain_oauth2Mode_oauth2EndpointsPermitted() throws Exception {
        // In OAuth2 mode, OAuth2 authorization endpoint should be permitted
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().isOk()); // Will redirect to Google login
    }
}