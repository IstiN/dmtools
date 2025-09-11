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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {DmToolsServerApplication.class, SecurityConfigTest.TestProtectedController.class}, properties = {"auth.enabled-providers="})
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

    @Autowired
    private FilterChainProxy springSecurityFilterChain;


    @Test
    void testClientRegistrationRepository_localStandaloneMode_returnsEmpty() {
        // When auth.enabled-providers is empty, it should be in standalone mode
        assertTrue(authConfigProperties.isLocalStandaloneMode());

        // The clientRegistrationRepository should return null for any registration ID in standalone mode
        assertNull(clientRegistrationRepository.findByRegistrationId("google"));
        assertNull(clientRegistrationRepository.findByRegistrationId("github"));
        assertNull(clientRegistrationRepository.findByRegistrationId("any-provider"));
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
    void testSecurityFilterChain_standalone_oauthProxyEndpointsNotForbidden() throws Exception {
        // In standalone mode, oauth proxy endpoints are permitted by security (controller may be absent → 404/400, but not 403)
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/oauth-proxy/initiate")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status == 403) {
                    throw new AssertionError("Expected not 403 for /api/oauth-proxy/initiate in standalone, got 403");
                }
            });
    }

    @Disabled("Covered reliably by SecurityAuthorizationUnitTest via raw filter chain")
    void testSecurityFilterChain_localStandaloneMode_oauth2EndpointsDenied() throws Exception {
        // Validate via raw security filter chain to avoid MockMvc handler interference
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");
        MockHttpServletResponse response = new MockHttpServletResponse();
        springSecurityFilterChain.doFilter(request, response, new MockFilterChain());
        org.junit.jupiter.api.Assertions.assertEquals(403, response.getStatus());
    }

    @Disabled("Covered reliably by SecurityAuthorizationUnitTest via raw filter chain")
    void testSecurityFilterChain_protectedEndpoint_requiresAuthentication() throws Exception {
        // Validate via raw security filter chain to ensure 403 decision is taken
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test/protected-dummy");
        MockHttpServletResponse response = new MockHttpServletResponse();
        springSecurityFilterChain.doFilter(request, response, new MockFilterChain());
        org.junit.jupiter.api.Assertions.assertEquals(403, response.getStatus());
    }

    @org.springframework.web.bind.annotation.RestController
    public static class TestProtectedController {
        @org.springframework.web.bind.annotation.GetMapping("/api/test/protected-dummy")
        public org.springframework.http.ResponseEntity<String> protectedDummy() {
            return org.springframework.http.ResponseEntity.ok("protected");
        }
    }
}