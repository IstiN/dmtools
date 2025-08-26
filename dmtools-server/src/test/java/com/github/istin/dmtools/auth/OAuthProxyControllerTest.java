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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void initiateOAuth_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        OAuthInitiateRequest request = new OAuthInitiateRequest(
            "google", 
            "https://myapp.com/callback", 
            "web", 
            "dev"
        );
        
        String mockState = "oauth_proxy_12345-abcd-efgh";
        String mockAuthUrl = "https://accounts.google.com/oauth/authorize?client_id=123&redirect_uri=...&state=" + mockState;
        
        when(oAuthProxyService.createOAuthState(any(OAuthInitiateRequest.class)))
            .thenReturn(mockState);
        when(oAuthProxyService.buildProviderAuthUrl("google", mockState))
            .thenReturn(mockAuthUrl);

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.auth_url").value(mockAuthUrl))
                .andExpect(jsonPath("$.state").value(mockState))
                .andExpect(jsonPath("$.expires_in").value(900));
    }

    @Test
    void initiateOAuth_WithMissingProvider_ShouldReturnBadRequest() throws Exception {
        // Arrange
        OAuthInitiateRequest request = new OAuthInitiateRequest(
            null, 
            "https://myapp.com/callback", 
            "web", 
            "dev"
        );

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Provider and client_redirect_uri are required"));
    }

    @Test
    void initiateOAuth_WithMissingClientRedirectUri_ShouldReturnBadRequest() throws Exception {
        // Arrange
        OAuthInitiateRequest request = new OAuthInitiateRequest(
            "google", 
            null, 
            "web", 
            "dev"
        );

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Provider and client_redirect_uri are required"));
    }

    @Test
    void initiateOAuth_WithInvalidProvider_ShouldReturnBadRequest() throws Exception {
        // Arrange
        OAuthInitiateRequest request = new OAuthInitiateRequest(
            "invalid_provider", 
            "https://myapp.com/callback", 
            "web", 
            "dev"
        );
        
        when(oAuthProxyService.createOAuthState(any(OAuthInitiateRequest.class)))
            .thenReturn("oauth_proxy_12345");
        when(oAuthProxyService.buildProviderAuthUrl("invalid_provider", "oauth_proxy_12345"))
            .thenThrow(new IllegalArgumentException("Unknown OAuth provider: invalid_provider"));

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_provider"))
                .andExpect(jsonPath("$.message").value("Unknown OAuth provider: invalid_provider"));
    }

    @Test
    void initiateOAuth_WithServiceException_ShouldReturnServerError() throws Exception {
        // Arrange
        OAuthInitiateRequest request = new OAuthInitiateRequest(
            "google", 
            "https://myapp.com/callback", 
            "web", 
            "dev"
        );
        
        when(oAuthProxyService.createOAuthState(any(OAuthInitiateRequest.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("server_error"))
                .andExpect(jsonPath("$.message").value("Failed to initiate OAuth: Database connection failed"));
    }

    // ========== POST /api/oauth-proxy/exchange Tests ==========

    @Test
    void exchangeCode_WithValidRequest_ShouldReturnJwtToken() throws Exception {
        // Arrange
        OAuthExchangeRequest request = new OAuthExchangeRequest(
            "temp_code_12345", 
            "oauth_proxy_12345-abcd-efgh"
        );
        
        String mockJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        
        when(oAuthProxyService.exchangeCodeForToken("temp_code_12345", "oauth_proxy_12345-abcd-efgh"))
            .thenReturn(mockJwtToken);

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").value(mockJwtToken))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600));
    }

    @Test
    void exchangeCode_WithMissingCode_ShouldReturnBadRequest() throws Exception {
        // Arrange
        OAuthExchangeRequest request = new OAuthExchangeRequest(
            null, 
            "oauth_proxy_12345-abcd-efgh"
        );

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Code and state are required"));
    }

    @Test
    void exchangeCode_WithMissingState_ShouldReturnBadRequest() throws Exception {
        // Arrange
        OAuthExchangeRequest request = new OAuthExchangeRequest(
            "temp_code_12345", 
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Code and state are required"));
    }

    @Test
    void exchangeCode_WithInvalidState_ShouldReturnInvalidGrant() throws Exception {
        // Arrange
        OAuthExchangeRequest request = new OAuthExchangeRequest(
            "temp_code_12345", 
            "invalid_state"
        );
        
        when(oAuthProxyService.exchangeCodeForToken("temp_code_12345", "invalid_state"))
            .thenThrow(new IllegalStateException("Invalid or expired state"));

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_grant"))
                .andExpect(jsonPath("$.message").value("Invalid or expired state"));
    }

    @Test
    void exchangeCode_WithInvalidCode_ShouldReturnInvalidGrant() throws Exception {
        // Arrange
        OAuthExchangeRequest request = new OAuthExchangeRequest(
            "invalid_code", 
            "oauth_proxy_12345-abcd-efgh"
        );
        
        when(oAuthProxyService.exchangeCodeForToken("invalid_code", "oauth_proxy_12345-abcd-efgh"))
            .thenThrow(new IllegalStateException("Invalid or expired code"));

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_grant"))
                .andExpect(jsonPath("$.message").value("Invalid or expired code"));
    }

    @Test
    void exchangeCode_WithServiceException_ShouldReturnServerError() throws Exception {
        // Arrange
        OAuthExchangeRequest request = new OAuthExchangeRequest(
            "temp_code_12345", 
            "oauth_proxy_12345-abcd-efgh"
        );
        
        when(oAuthProxyService.exchangeCodeForToken("temp_code_12345", "oauth_proxy_12345-abcd-efgh"))
            .thenThrow(new RuntimeException("JWT signing failed"));

        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("server_error"))
                .andExpect(jsonPath("$.message").value("Failed to exchange code: JWT signing failed"));
    }

    // ========== GET /api/oauth-proxy/providers Tests ==========

    @Test
    void getProviders_ShouldReturnSupportedProviders() throws Exception {
        // Arrange
        List<String> supportedProviders = Arrays.asList("google", "microsoft", "github");
        when(oAuthProxyService.getSupportedProviders()).thenReturn(supportedProviders);

        // Act & Assert
        mockMvc.perform(get("/api/oauth-proxy/providers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
        // Arrange
        List<String> emptyProviders = Arrays.asList();
        when(oAuthProxyService.getSupportedProviders()).thenReturn(emptyProviders);

        // Act & Assert
        mockMvc.perform(get("/api/oauth-proxy/providers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.providers").isArray())
                .andExpect(jsonPath("$.providers").isEmpty())
                .andExpect(jsonPath("$.client_types").isArray())
                .andExpect(jsonPath("$.environments").isArray());
    }

    // ========== Edge Cases and Error Handling Tests ==========

    @Test
    void initiateOAuth_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_request"));
    }

    @Test
    void exchangeCode_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_request"));
    }

    @Test
    void initiateOAuth_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exchangeCode_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/oauth-proxy/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }
}
