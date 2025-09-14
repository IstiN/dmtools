package com.github.istin.dmtools.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.auth.dto.OAuthExchangeRequest;
import com.github.istin.dmtools.auth.dto.OAuthInitiateRequest;
import com.github.istin.dmtools.auth.service.OAuthProxyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for OAuthProxyController
 * Tests all OAuth proxy endpoints: /initiate, /exchange, /providers
 */
@ExtendWith(MockitoExtension.class)
public class OAuthProxyControllerTest {

    @Mock
    private OAuthProxyService oAuthProxyService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private OAuthProxyController oAuthProxyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(oAuthProxyController).build();
        objectMapper = new ObjectMapper();
    }

    // ========== POST /api/oauth-proxy/initiate Tests ==========

    @Test
    void initiateOAuth_WithValidRequest_ShouldReturnAuthUrl() throws Exception {
        // Given
        OAuthInitiateRequest request = new OAuthInitiateRequest();
        request.setProvider("google");
        request.setClientRedirectUri("https://myapp.com/callback");
        request.setClientType("web");
        request.setEnvironment("dev");

        String expectedState = "oauth_proxy_test_state";
        String expectedAuthUrl = "https://accounts.google.com/o/oauth2/auth?state=" + expectedState;

        when(oAuthProxyService.createOAuthState(any(OAuthInitiateRequest.class)))
                .thenReturn(expectedState);
        when(oAuthProxyService.buildProviderAuthUrl("google", expectedState))
                .thenReturn(expectedAuthUrl);

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth_url").value(expectedAuthUrl))
                .andExpect(jsonPath("$.state").value(expectedState))
                .andExpect(jsonPath("$.expires_in").value(900));
    }

    @Test
    void initiateOAuth_WithMissingProvider_ShouldReturnBadRequest() throws Exception {
        // Given
        OAuthInitiateRequest request = new OAuthInitiateRequest();
        request.setClientRedirectUri("https://myapp.com/callback");
        request.setClientType("web");

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Provider and client_redirect_uri are required"));
    }

    @Test
    void initiateOAuth_WithMissingClientRedirectUri_ShouldReturnBadRequest() throws Exception {
        // Given
        OAuthInitiateRequest request = new OAuthInitiateRequest();
        request.setProvider("google");
        request.setClientType("web");

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Provider and client_redirect_uri are required"));
    }

    @Test
    void initiateOAuth_WithInvalidProvider_ShouldReturnBadRequest() throws Exception {
        // Given
        OAuthInitiateRequest request = new OAuthInitiateRequest();
        request.setProvider("invalid_provider");
        request.setClientRedirectUri("https://myapp.com/callback");

        when(oAuthProxyService.createOAuthState(any(OAuthInitiateRequest.class)))
                .thenThrow(new IllegalArgumentException("Unsupported provider: invalid_provider"));

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_provider"))
                .andExpect(jsonPath("$.message").value("Unsupported provider: invalid_provider"));
    }

    @Test
    void initiateOAuth_WithServiceException_ShouldReturnServerError() throws Exception {
        // Given
        OAuthInitiateRequest request = new OAuthInitiateRequest();
        request.setProvider("google");
        request.setClientRedirectUri("https://myapp.com/callback");

        when(oAuthProxyService.createOAuthState(any(OAuthInitiateRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("server_error"))
                .andExpect(jsonPath("$.message").value("Failed to initiate OAuth: Database connection failed"));
    }

    @Test
    void initiateOAuth_WithMobileClientType_ShouldReturnAuthUrl() throws Exception {
        // Given
        OAuthInitiateRequest request = new OAuthInitiateRequest();
        request.setProvider("microsoft");
        request.setClientRedirectUri("myapp://auth/callback");
        request.setClientType("mobile");
        request.setEnvironment("prod");

        String expectedState = "oauth_proxy_mobile_state";
        String expectedAuthUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?state=" + expectedState;

        when(oAuthProxyService.createOAuthState(any(OAuthInitiateRequest.class)))
                .thenReturn(expectedState);
        when(oAuthProxyService.buildProviderAuthUrl("microsoft", expectedState))
                .thenReturn(expectedAuthUrl);

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth_url").value(expectedAuthUrl))
                .andExpect(jsonPath("$.state").value(expectedState));
    }

    // ========== POST /api/oauth-proxy/exchange Tests ==========

    @Test
    void exchangeCode_WithValidRequest_ShouldReturnJwtToken() throws Exception {
        // Given
        OAuthExchangeRequest request = new OAuthExchangeRequest();
        request.setCode("auth_code_12345");
        request.setState("oauth_proxy_test_state");

        String expectedJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

        when(oAuthProxyService.exchangeCodeForToken("auth_code_12345", "oauth_proxy_test_state"))
                .thenReturn(expectedJwtToken);

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(expectedJwtToken))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600));
    }

    @Test
    void exchangeCode_WithMissingCode_ShouldReturnBadRequest() throws Exception {
        // Given
        OAuthExchangeRequest request = new OAuthExchangeRequest();
        request.setState("oauth_proxy_test_state");

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Code and state are required"));
    }

    @Test
    void exchangeCode_WithMissingState_ShouldReturnBadRequest() throws Exception {
        // Given
        OAuthExchangeRequest request = new OAuthExchangeRequest();
        request.setCode("auth_code_12345");

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Code and state are required"));
    }

    @Test
    void exchangeCode_WithInvalidState_ShouldReturnInvalidGrant() throws Exception {
        // Given
        OAuthExchangeRequest request = new OAuthExchangeRequest();
        request.setCode("auth_code_12345");
        request.setState("invalid_state");

        when(oAuthProxyService.exchangeCodeForToken("auth_code_12345", "invalid_state"))
                .thenThrow(new IllegalStateException("Invalid or expired state"));

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"))
                .andExpect(jsonPath("$.message").value("Invalid or expired state"));
    }

    @Test
    void exchangeCode_WithServiceException_ShouldReturnServerError() throws Exception {
        // Given
        OAuthExchangeRequest request = new OAuthExchangeRequest();
        request.setCode("auth_code_12345");
        request.setState("oauth_proxy_test_state");

        when(oAuthProxyService.exchangeCodeForToken("auth_code_12345", "oauth_proxy_test_state"))
                .thenThrow(new RuntimeException("Token generation failed"));

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("server_error"))
                .andExpect(jsonPath("$.message").value("Failed to exchange code: Token generation failed"));
    }

    @Test
    void exchangeCode_WithExpiredCode_ShouldReturnInvalidGrant() throws Exception {
        // Given
        OAuthExchangeRequest request = new OAuthExchangeRequest();
        request.setCode("expired_code");
        request.setState("oauth_proxy_test_state");

        when(oAuthProxyService.exchangeCodeForToken("expired_code", "oauth_proxy_test_state"))
                .thenThrow(new IllegalStateException("Code has expired"));

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"))
                .andExpect(jsonPath("$.message").value("Code has expired"));
    }

    // ========== GET /api/oauth-proxy/providers Tests ==========

    @Test
    void getProviders_ShouldReturnSupportedProviders() throws Exception {
        // Given
        List<String> supportedProviders = Arrays.asList("google", "microsoft", "github");
        when(oAuthProxyService.getSupportedProviders()).thenReturn(supportedProviders);

        // When & Then
        mockMvc.perform(get("/api/oauth-proxy/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providers").isArray())
                .andExpect(jsonPath("$.providers[0]").value("google"))
                .andExpect(jsonPath("$.providers[1]").value("microsoft"))
                .andExpect(jsonPath("$.providers[2]").value("github"))
                .andExpect(jsonPath("$.client_types").isArray())
                .andExpect(jsonPath("$.client_types[0]").value("web"))
                .andExpect(jsonPath("$.client_types[1]").value("mobile"))
                .andExpect(jsonPath("$.client_types[2]").value("desktop"))
                .andExpect(jsonPath("$.environments").isArray())
                .andExpect(jsonPath("$.environments[0]").value("dev"))
                .andExpect(jsonPath("$.environments[1]").value("staging"))
                .andExpect(jsonPath("$.environments[2]").value("prod"));
    }

    @Test
    void getProviders_WithEmptyProvidersList_ShouldReturnEmptyArray() throws Exception {
        // Given
        List<String> emptyProviders = Arrays.asList();
        when(oAuthProxyService.getSupportedProviders()).thenReturn(emptyProviders);

        // When & Then
        mockMvc.perform(get("/api/oauth-proxy/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providers").isArray())
                .andExpect(jsonPath("$.providers").isEmpty())
                .andExpect(jsonPath("$.client_types").isArray())
                .andExpect(jsonPath("$.environments").isArray());
    }

    // ========== Edge Case Tests ==========

    @Test
    void initiateOAuth_WithNullBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"));
    }

    @Test
    void exchangeCode_WithNullBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"));
    }

    @Test
    void initiateOAuth_WithLongRedirectUri_ShouldWorkCorrectly() throws Exception {
        // Given
        OAuthInitiateRequest request = new OAuthInitiateRequest();
        request.setProvider("google");
        request.setClientRedirectUri("https://myverylongdomainname.application.com/auth/oauth/callback/with/very/long/path/structure?param1=value1&param2=value2");

        String expectedState = "oauth_proxy_long_uri_state";
        String expectedAuthUrl = "https://accounts.google.com/o/oauth2/auth?state=" + expectedState;

        when(oAuthProxyService.createOAuthState(any(OAuthInitiateRequest.class)))
                .thenReturn(expectedState);
        when(oAuthProxyService.buildProviderAuthUrl("google", expectedState))
                .thenReturn(expectedAuthUrl);

        // When & Then
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth_url").value(expectedAuthUrl))
                .andExpect(jsonPath("$.state").value(expectedState));
    }
}
