package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.service.LocalUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({SecurityConfig.class, AuthConfigProperties.class, LocalUserDetailsService.class})
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private EnhancedOAuth2AuthenticationSuccessHandler enhancedOAuth2AuthenticationSuccessHandler;

    @MockBean
    private AuthDebugFilter authDebugFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers="})
    void testLocalStandaloneMode_oauthLoginDisabled() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login")); // Should redirect to login, not OAuth2 provider
    }

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers=google"})
    void testOAuthLoginEnabled_redirectsToProvider() throws Exception {
        // Mock a client registration to be found
        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(
                ClientRegistration.withRegistrationId("google")
                        .clientId("test-client")
                        .clientSecret("test-secret")
                        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                        .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                        .scope("openid", "profile", "email")
                        .userNameAttributeName("name")
                        .clientName("Google")
                        .build()
        );

        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("https://accounts.google.com/o/oauth2/v2/auth**"));
    }

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers="})
    void testLocalLoginEndpoint_permitAllInStandaloneMode() throws Exception {
        mockMvc.perform(get("/api/auth/local-login"))
                .andExpect(status().isOk()); // Should be accessible without authentication
    }

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers=google"})
    void testLocalLoginEndpoint_permitAllInOAuthMode() throws Exception {
        mockMvc.perform(get("/api/auth/local-login"))
                .andExpect(status().isOk()); // Should be accessible without authentication even in OAuth mode
    }

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers="})
    void testAuthConfigurationEndpoint_localStandaloneMode() throws Exception {
        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    String content = mvcResult.getResponse().getContentAsString();
                    System.out.println("Response: " + content);
                    // Assert that localStandaloneMode is true and enabledProviders is empty
                    assert(content.contains("\"localStandaloneMode\":true"));
                    assert(content.contains("\"enabledProviders\":[]"));
                });
    }

    @Test
    @TestPropertySource(properties = {"auth.enabled-providers=google"})
    void testAuthConfigurationEndpoint_oauthMode() throws Exception {
        // Mock a client registration to be found
        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(
                ClientRegistration.withRegistrationId("google")
                        .clientId("test-client")
                        .clientSecret("test-secret")
                        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                        .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                        .scope("openid", "profile", "email")
                        .userNameAttributeName("name")
                        .clientName("Google")
                        .build()
        );

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    String content = mvcResult.getResponse().getContentAsString();
                    System.out.println("Response: " + content);
                    // Assert that localStandaloneMode is false and enabledProviders contains google
                    assert(content.contains("\"localStandaloneMode\":false"));
                    assert(content.contains("\"enabledProviders\":[\"google\"]"));
                });
    }
}
