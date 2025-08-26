package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.JwtUtils;
import com.github.istin.dmtools.auth.PlaceholderAuthentication;
import com.github.istin.dmtools.auth.dto.OAuthInitiateRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OAuthProxyService
 * Tests all public methods of the OAuth proxy service
 */
@ExtendWith(MockitoExtension.class)
public class OAuthProxyServiceTest {

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private OAuthProxyService oAuthProxyService;

    private OAuthInitiateRequest sampleRequest;
    private ClientRegistration googleClientRegistration;

    @BeforeEach
    void setUp() {
        sampleRequest = new OAuthInitiateRequest(
            "google",
            "https://myapp.com/callback",
            "web",
            "dev"
        );

        // Create a mock ClientRegistration for Google
        googleClientRegistration = ClientRegistration.withRegistrationId("google")
            .clientId("google-client-id")
            .clientSecret("google-client-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("https://dmtools.com/login/oauth2/code/google")
            .scope("openid", "profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName("sub")
            .build();
    }

    // ========== createOAuthState Tests ==========

    @Test
    void createOAuthState_WithValidRequest_ShouldGenerateStateAndStoreData() {
        // Act
        String state = oAuthProxyService.createOAuthState(sampleRequest);

        // Assert
        assertNotNull(state);
        assertTrue(state.startsWith("oauth_proxy_"));
        assertTrue(state.length() > 20); // UUID should make it reasonably long
        
        // Verify state is stored internally (we can test this via the isProxyState method)
        assertTrue(oAuthProxyService.isProxyState(state));
    }

    @Test
    void createOAuthState_WithDifferentProviders_ShouldGenerateUniqueStates() {
        // Arrange
        OAuthInitiateRequest microsoftRequest = new OAuthInitiateRequest(
            "microsoft", "https://myapp.com/callback", "web", "dev"
        );

        // Act
        String googleState = oAuthProxyService.createOAuthState(sampleRequest);
        String microsoftState = oAuthProxyService.createOAuthState(microsoftRequest);

        // Assert
        assertNotNull(googleState);
        assertNotNull(microsoftState);
        assertNotEquals(googleState, microsoftState);
        assertTrue(oAuthProxyService.isProxyState(googleState));
        assertTrue(oAuthProxyService.isProxyState(microsoftState));
    }

    // ========== buildProviderAuthUrl Tests ==========

    @Test
    void buildProviderAuthUrl_WithValidGoogleProvider_ShouldReturnAuthUrl() {
        // Arrange
        String state = "oauth_proxy_test_state";
        when(clientRegistrationRepository.findByRegistrationId("google"))
            .thenReturn(googleClientRegistration);

        // Act
        String authUrl = oAuthProxyService.buildProviderAuthUrl("google", state);

        // Assert
        assertNotNull(authUrl);
        assertTrue(authUrl.contains("https://accounts.google.com/o/oauth2/auth"));
        assertTrue(authUrl.contains("client_id=google-client-id"));
        assertTrue(authUrl.contains("state=" + state));
        assertTrue(authUrl.contains("redirect_uri=https://dmtools.com/login/oauth2/code/google"));
        // Check for scope parameter - can be either URL encoded or not
        assertTrue(authUrl.contains("scope=openid") && authUrl.contains("profile") && authUrl.contains("email"), 
                   "URL should contain all required scopes: " + authUrl);
        assertTrue(authUrl.contains("response_type=code"));
    }

    @Test
    void buildProviderAuthUrl_WithInvalidProvider_ShouldThrowException() {
        // Arrange
        String state = "oauth_proxy_test_state";
        when(clientRegistrationRepository.findByRegistrationId("invalid_provider"))
            .thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> oAuthProxyService.buildProviderAuthUrl("invalid_provider", state)
        );
        assertEquals("Unknown OAuth provider: invalid_provider", exception.getMessage());
    }

    @Test
    void buildProviderAuthUrl_WithNullProvider_ShouldThrowException() {
        // Arrange
        String state = "oauth_proxy_test_state";
        when(clientRegistrationRepository.findByRegistrationId(null))
            .thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> oAuthProxyService.buildProviderAuthUrl(null, state)
        );
        assertEquals("Unknown OAuth provider: null", exception.getMessage());
    }

    // ========== exchangeCodeForToken Tests ==========

    @Test
    void exchangeCodeForToken_WithValidOAuth2Token_ShouldReturnJwtToken() {
        // Arrange
        String tempCode = "temp_12345";
        String state = oAuthProxyService.createOAuthState(sampleRequest);
        String expectedJwtToken = "jwt_token_12345";
        
        // Create OAuth2User
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google_user_123");
        attributes.put("email", "test@google.com");
        attributes.put("name", "Test User");
        
        OAuth2User oAuth2User = new DefaultOAuth2User(
            Collections.singleton(new OAuth2UserAuthority(attributes)),
            attributes,
            "sub"
        );
        
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
            oAuth2User, Collections.emptyList(), "google"
        );
        
        // Store the authentication in the service's internal storage
        @SuppressWarnings("unchecked")
        Map<String, Authentication> tempCodeStorage = 
            (Map<String, Authentication>) ReflectionTestUtils.getField(oAuthProxyService, "tempCodeStorage");
        tempCodeStorage.put(tempCode, authToken);
        
        when(jwtUtils.generateJwtToken("test@google.com", "google_user_123"))
            .thenReturn(expectedJwtToken);

        // Act
        String jwtToken = oAuthProxyService.exchangeCodeForToken(tempCode, state);

        // Assert
        assertEquals(expectedJwtToken, jwtToken);
        verify(userService).createOrUpdateOAuth2User(authToken);
        verify(jwtUtils).generateJwtToken("test@google.com", "google_user_123");
    }

    @Test
    void exchangeCodeForToken_WithPlaceholderAuthentication_ShouldReturnJwtToken() {
        // Arrange
        String tempCode = "temp_12345";
        String state = oAuthProxyService.createOAuthState(sampleRequest);
        String authCode = "auth_code_from_provider";
        String expectedJwtToken = "jwt_token_12345";
        
        PlaceholderAuthentication placeholderAuth = new PlaceholderAuthentication(authCode, "google");
        
        // Store the authentication in the service's internal storage
        @SuppressWarnings("unchecked")
        Map<String, Authentication> tempCodeStorage = 
            (Map<String, Authentication>) ReflectionTestUtils.getField(oAuthProxyService, "tempCodeStorage");
        tempCodeStorage.put(tempCode, placeholderAuth);
        
        String testEmail = "test.user+" + authCode.substring(0, 8) + "@google.proxy";
        String testUserId = "proxy_user_" + authCode.substring(0, 8);
        
        when(jwtUtils.generateJwtToken(testEmail, testUserId))
            .thenReturn(expectedJwtToken);

        // Act
        String jwtToken = oAuthProxyService.exchangeCodeForToken(tempCode, state);

        // Assert
        assertEquals(expectedJwtToken, jwtToken);
        verify(jwtUtils).generateJwtToken(testEmail, testUserId);
    }

    @Test
    void exchangeCodeForToken_WithInvalidState_ShouldThrowException() {
        // Arrange
        String tempCode = "temp_12345";
        String invalidState = "invalid_state";

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> oAuthProxyService.exchangeCodeForToken(tempCode, invalidState)
        );
        assertEquals("Invalid or expired state", exception.getMessage());
    }

    @Test
    void exchangeCodeForToken_WithInvalidTempCode_ShouldThrowException() {
        // Arrange
        String invalidTempCode = "invalid_temp_code";
        String state = oAuthProxyService.createOAuthState(sampleRequest);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> oAuthProxyService.exchangeCodeForToken(invalidTempCode, state)
        );
        assertEquals("Invalid or expired code", exception.getMessage());
    }

    @Test
    void exchangeCodeForToken_WithUserServiceException_ShouldContinueAndReturnToken() {
        // Arrange
        String tempCode = "temp_12345";
        String state = oAuthProxyService.createOAuthState(sampleRequest);
        String expectedJwtToken = "jwt_token_12345";
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google_user_123");
        attributes.put("email", "test@google.com");
        
        OAuth2User oAuth2User = new DefaultOAuth2User(
            Collections.singleton(new OAuth2UserAuthority(attributes)),
            attributes,
            "sub"
        );
        
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
            oAuth2User, Collections.emptyList(), "google"
        );
        
        @SuppressWarnings("unchecked")
        Map<String, Authentication> tempCodeStorage = 
            (Map<String, Authentication>) ReflectionTestUtils.getField(oAuthProxyService, "tempCodeStorage");
        tempCodeStorage.put(tempCode, authToken);
        
        // Mock userService to throw exception
        doThrow(new RuntimeException("Database error")).when(userService).createOrUpdateOAuth2User(authToken);
        when(jwtUtils.generateJwtToken("test@google.com", "google_user_123"))
            .thenReturn(expectedJwtToken);

        // Act
        String jwtToken = oAuthProxyService.exchangeCodeForToken(tempCode, state);

        // Assert - Should still return token despite userService exception
        assertEquals(expectedJwtToken, jwtToken);
        verify(jwtUtils).generateJwtToken("test@google.com", "google_user_123");
    }

    // ========== getSupportedProviders Tests ==========

    @Test
    void getSupportedProviders_ShouldReturnPredefinedProviders() {
        // Act
        List<String> providers = oAuthProxyService.getSupportedProviders();

        // Assert
        assertNotNull(providers);
        assertEquals(3, providers.size());
        assertTrue(providers.contains("google"));
        assertTrue(providers.contains("microsoft"));
        assertTrue(providers.contains("github"));
    }

    // ========== isProxyState Tests ==========

    @Test
    void isProxyState_WithValidProxyState_ShouldReturnTrue() {
        // Arrange
        String proxyState = "oauth_proxy_12345-abcd-efgh";

        // Act & Assert
        assertTrue(oAuthProxyService.isProxyState(proxyState));
    }

    @Test
    void isProxyState_WithInvalidState_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(oAuthProxyService.isProxyState("regular_state_12345"));
        assertFalse(oAuthProxyService.isProxyState("oauth_12345"));
        assertFalse(oAuthProxyService.isProxyState("proxy_oauth_12345"));
    }

    @Test
    void isProxyState_WithNullState_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(oAuthProxyService.isProxyState(null));
    }

    @Test
    void isProxyState_WithEmptyState_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(oAuthProxyService.isProxyState(""));
    }

    // ========== generateTempCodeAndRedirect Tests ==========

    @Test
    void generateTempCodeAndRedirect_WithValidState_ShouldGenerateCodeAndRedirectUrl() {
        // Arrange
        String state = oAuthProxyService.createOAuthState(sampleRequest);
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google_user_123");
        attributes.put("email", "test@google.com");
        
        OAuth2User oAuth2User = new DefaultOAuth2User(
            Collections.singleton(new OAuth2UserAuthority(attributes)),
            attributes,
            "sub"
        );
        
        Authentication authentication = new OAuth2AuthenticationToken(
            oAuth2User, Collections.emptyList(), "google"
        );

        // Act
        String redirectUrl = oAuthProxyService.generateTempCodeAndRedirect(state, authentication);

        // Assert
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith("https://myapp.com/callback"));
        assertTrue(redirectUrl.contains("code="));
        assertTrue(redirectUrl.contains("state=" + state));
    }

    @Test
    void generateTempCodeAndRedirect_WithInvalidState_ShouldThrowException() {
        // Arrange
        String invalidState = "invalid_state";
        Authentication authentication = mock(Authentication.class);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> oAuthProxyService.generateTempCodeAndRedirect(invalidState, authentication)
        );
        assertEquals("Invalid or expired state: invalid_state", exception.getMessage());
    }

    // ========== getClientRedirectUri Tests ==========

    @Test
    void getClientRedirectUri_WithValidProxyState_ShouldReturnRedirectUri() {
        // Arrange
        String state = oAuthProxyService.createOAuthState(sampleRequest);

        // Act
        String redirectUri = oAuthProxyService.getClientRedirectUri(state);

        // Assert
        assertEquals("https://myapp.com/callback", redirectUri);
    }

    @Test
    void getClientRedirectUri_WithInvalidState_ShouldReturnNull() {
        // Act
        String redirectUri = oAuthProxyService.getClientRedirectUri("invalid_state");

        // Assert
        assertNull(redirectUri);
    }

    @Test
    void getClientRedirectUri_WithNonProxyState_ShouldReturnNull() {
        // Act
        String redirectUri = oAuthProxyService.getClientRedirectUri("regular_state");

        // Assert
        assertNull(redirectUri);
    }

    // ========== generateTempCode Tests ==========

    @Test
    void generateTempCode_WithValidState_ShouldGenerateTempCode() {
        // Arrange
        String state = oAuthProxyService.createOAuthState(sampleRequest);
        String authorizationCode = "auth_code_from_provider";

        // Act
        String tempCode = oAuthProxyService.generateTempCode(state, authorizationCode);

        // Assert
        assertNotNull(tempCode);
        assertTrue(tempCode.startsWith("temp_"));
        assertTrue(tempCode.length() > 10);
    }

    @Test
    void generateTempCode_WithInvalidState_ShouldReturnNull() {
        // Arrange
        String authorizationCode = "auth_code_from_provider";

        // Act
        String tempCode = oAuthProxyService.generateTempCode("invalid_state", authorizationCode);

        // Assert
        assertNull(tempCode);
    }

    @Test
    void generateTempCode_WithNonProxyState_ShouldReturnNull() {
        // Arrange
        String authorizationCode = "auth_code_from_provider";

        // Act
        String tempCode = oAuthProxyService.generateTempCode("regular_state", authorizationCode);

        // Assert
        assertNull(tempCode);
    }

    // ========== Integration Tests ==========

    @Test
    void fullOAuthFlow_ShouldWorkEndToEnd() {
        // Arrange
        when(clientRegistrationRepository.findByRegistrationId("google"))
            .thenReturn(googleClientRegistration);
        when(jwtUtils.generateJwtToken(anyString(), anyString()))
            .thenReturn("jwt_token_12345");

        // Step 1: Create OAuth state
        String state = oAuthProxyService.createOAuthState(sampleRequest);
        assertNotNull(state);
        assertTrue(oAuthProxyService.isProxyState(state));

        // Step 2: Build provider auth URL
        String authUrl = oAuthProxyService.buildProviderAuthUrl("google", state);
        assertNotNull(authUrl);
        assertTrue(authUrl.contains(state));

        // Step 3: Generate temp code (simulate OAuth callback)
        String authCode = "authorization_code_from_google";
        String tempCode = oAuthProxyService.generateTempCode(state, authCode);
        assertNotNull(tempCode);

        // Step 4: Exchange temp code for JWT token
        String jwtToken = oAuthProxyService.exchangeCodeForToken(tempCode, state);
        assertEquals("jwt_token_12345", jwtToken);
    }

    @Test
    void stateStorage_ShouldHandleMultipleStatesCorrectly() {
        // Arrange
        OAuthInitiateRequest request1 = new OAuthInitiateRequest("google", "https://app1.com", "web", "dev");
        OAuthInitiateRequest request2 = new OAuthInitiateRequest("microsoft", "https://app2.com", "mobile", "prod");

        // Act
        String state1 = oAuthProxyService.createOAuthState(request1);
        String state2 = oAuthProxyService.createOAuthState(request2);

        // Assert
        assertNotEquals(state1, state2);
        assertEquals("https://app1.com", oAuthProxyService.getClientRedirectUri(state1));
        assertEquals("https://app2.com", oAuthProxyService.getClientRedirectUri(state2));
    }
}
