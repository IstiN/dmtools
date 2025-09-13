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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {DmToolsServerApplication.class, SecurityConfigOAuth2GoogleTest.TestOAuthController.class}, properties = {
        "auth.enabled-providers=google",
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret",
        "spring.security.oauth2.client.registration.google.scope=openid,profile,email",
        "jwt.secret=a3b2c1d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2",
        "jwt.expiration=86400000",
        "jwt.header=Authorization", 
        "jwt.prefix=Bearer"
})
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

    @Autowired
    private FilterChainProxy springSecurityFilterChain;


    @Test
    void testClientRegistrationRepository_oauth2Mode_withSpecificProviders() {
        // When auth.enabled-providers specifies google, it should not be in standalone mode
        assertFalse(authConfigProperties.isLocalStandaloneMode());

        // The clientRegistrationRepository should have google configuration
        assertNotNull(clientRegistrationRepository.findByRegistrationId("google"));
        assertNull(clientRegistrationRepository.findByRegistrationId("github")); // Not configured
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

    @Disabled("Covered by AuthControllerUnitTest (controller denies in OAuth2 mode) and SecurityAuthorizationUnitTest for filter-chain behavior. MockMvc path resolution here is brittle.")
    @Test
    void testSecurityFilterChain_oauth2Mode_denyLocalLogin() throws Exception {
        // Use raw filter chain to assert deny policy deterministically
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/local-login");
        request.setContentType("application/json");
        request.setContent("{\"username\":\"u\",\"password\":\"p\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        springSecurityFilterChain.doFilter(request, response, new MockFilterChain());
        assertEquals(403, response.getStatus());
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

    @Test
    void testSecurityFilterChain_oauth2Mode_oauthProxyEndpointsPermitted() throws Exception {
        // In OAuth2 mode, oauth proxy should be permitted
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                        .contentType("application/json")
                        .content("{\"provider\":\"google\",\"client_redirect_uri\":\"http://localhost/cb\"}"))
                .andExpect(status().isOk());
    }

    @org.springframework.web.bind.annotation.RestController
    public static class TestOAuthController {
        @org.springframework.web.bind.annotation.GetMapping("/oauth2/authorization/{provider}")
        public org.springframework.http.ResponseEntity<Void> oauthAuth(@org.springframework.web.bind.annotation.PathVariable String provider) {
            return org.springframework.http.ResponseEntity.ok().build();
        }
    }

    // No test mapping for /api/auth/local-login to avoid ambiguity with real AuthController
}