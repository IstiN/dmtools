package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.dto.*;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private AuthConfigProperties authConfigProperties;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Mock default values for AuthConfigProperties
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn("testuser");
        when(authConfigProperties.getAdminPassword()).thenReturn("secret123");
        
        // Set JWT secret via reflection (this is needed for the private field)
        ReflectionTestUtils.setField(authController, "jwtSecret", "testSecretKeyMustBeLongEnoughForHmacSha256Algorithm");
        ReflectionTestUtils.setField(authController, "jwtExpirationMs", 3600000);
        ReflectionTestUtils.setField(authController, "jwtRefreshExpirationMs", 2592000000L); // 30 days
    }

    @Test
    void initiateLogin_ShouldReturnOAuthSetupMessage() {
        // Act
        ResponseEntity<?> response = authController.initiateLogin("google");
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("OAuth not configured", responseBody.get("error"));
        assertEquals("Please configure OAuth client credentials for google in application.properties", responseBody.get("message"));
        assertEquals("google", responseBody.get("provider"));
    }

    @Test
    void handleCallback_ShouldReturnTokenAndRedirect() {
        // Arrange
        when(jwtUtils.generateJwtToken(anyString(), anyString())).thenReturn("mock-jwt-token");
        
        // Act
        ResponseEntity<?> response = authController.handleCallback("google", "auth-code");
        
        // Assert
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("/?token=mock-jwt-token&login=success", response.getHeaders().getFirst("Location"));
    }

    @Test
    void getCurrentUser_WithNullAuthentication_ShouldReturnNotAuthenticated() {
        // Act
        ResponseEntity<?> response = authController.getCurrentUser(null);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        AuthUserResponse responseBody = (AuthUserResponse) response.getBody();
        assertEquals(false, responseBody.isAuthenticated());
    }

    @Test
    void getCurrentUser_WithUserDetailsAuthentication_ShouldReturnUserInfo() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPictureUrl("https://example.com/avatar.jpg");
        user.setProvider(AuthProvider.GOOGLE);
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        
        // Act
        ResponseEntity<?> response = authController.getCurrentUser(authentication);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        AuthUserResponse responseBody = (AuthUserResponse) response.getBody();
        assertEquals(true, responseBody.isAuthenticated());
        assertEquals("test@example.com", responseBody.getEmail());
        assertEquals("Test User", responseBody.getName());
        assertEquals("https://example.com/avatar.jpg", responseBody.getPictureUrl());
        assertEquals("GOOGLE", responseBody.getProvider());
    }

    @Test
    void getCurrentUser_WithOAuth2Authentication_ShouldReturnUserInfo() {
        // Arrange
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("test@example.com");
        
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPictureUrl("https://example.com/avatar.jpg");
        user.setProvider(AuthProvider.GOOGLE);
        
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        
        // Act
        ResponseEntity<?> response = authController.getCurrentUser(authentication);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        AuthUserResponse responseBody = (AuthUserResponse) response.getBody();
        assertEquals(true, responseBody.isAuthenticated());
        assertEquals("test@example.com", responseBody.getEmail());
        assertEquals("Test User", responseBody.getName());
    }

    @Test
    void logout_WithActiveSession_ShouldInvalidateSession() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getId()).thenReturn("test-session-id");
        
        // Act
        ResponseEntity<?> response = authController.logout(request);
        
        // Assert
        verify(session).invalidate();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        MessageResponse responseBody = (MessageResponse) response.getBody();
        assertEquals("Logged out successfully", responseBody.getMessage());
    }

    @Test
    void logout_WithNoSession_ShouldReturnSuccess() {
        // Arrange
        when(request.getSession(false)).thenReturn(null);
        
        // Act
        ResponseEntity<?> response = authController.logout(request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        MessageResponse responseBody = (MessageResponse) response.getBody();
        assertEquals("Logged out successfully", responseBody.getMessage());
    }

    @Test
    void localLogin_WithValidCredentials_ShouldReturnToken() {
        // Arrange
        LocalLoginRequest loginRequest = new LocalLoginRequest("testuser", "secret123");
        
        User user = new User();
        user.setId("user-id");
        user.setEmail("testuser@local.test");
        user.setName("testuser");
        // Initialize roles to simulate a new user with default role
        user.setRoles(new HashSet<>());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        when(userService.createOrUpdateUser(
            eq("testuser@local.test"),
            eq("testuser"),
            eq("testuser"),
            eq(""),
            eq(""),
            eq("en"),
            eq(AuthProvider.LOCAL),
            eq("testuser")
        )).thenReturn(user);
        
        when(jwtUtils.generateJwtTokenCustom(
            eq("testuser@local.test"),
            eq("user-id"),
            anyString(),
            anyInt()
        )).thenReturn("mock-jwt-token");
        
        when(jwtUtils.generateRefreshToken(
            eq("testuser@local.test"),
            eq("user-id")
        )).thenReturn("mock-refresh-token");
        
        // Act
        ResponseEntity<?> responseEntity = authController.localLogin(loginRequest, response);
        
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        LocalLoginResponse responseBody = (LocalLoginResponse) responseEntity.getBody();
        assertEquals("mock-jwt-token", responseBody.getToken());
        assertEquals("mock-refresh-token", responseBody.getRefreshToken());
        
        // Verify cookie
        Cookie[] cookies = response.getCookies();
        assertEquals(1, cookies.length);
        assertEquals("jwt", cookies[0].getName());
        assertEquals("mock-jwt-token", cookies[0].getValue());
        assertTrue(cookies[0].isHttpOnly());
    }

    @Test
    void localLogin_WithInvalidCredentials_ShouldReturnUnauthorized() {
        // Arrange
        LocalLoginRequest loginRequest = new LocalLoginRequest("testuser", "wrong-password");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Act
        ResponseEntity<?> responseEntity = authController.localLogin(loginRequest, response);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        
        ErrorResponse responseBody = (ErrorResponse) responseEntity.getBody();
        assertEquals("Invalid credentials", responseBody.getError());
    }

    @Test
    void localLogin_WithLocalAuthDisabled_ShouldReturnForbidden() {
        // Arrange
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        
        LocalLoginRequest loginRequest = new LocalLoginRequest("testuser", "secret123");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Act
        ResponseEntity<?> responseEntity = authController.localLogin(loginRequest, response);
        
        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        
        ErrorResponse responseBody = (ErrorResponse) responseEntity.getBody();
        assertEquals("Local auth disabled", responseBody.getError());
    }

    @Test
    void refreshToken_WithValidRefreshToken_ShouldReturnNewTokens() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        User user = new User();
        user.setId("user-id");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setRoles(new HashSet<>());
        
        when(jwtUtils.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getEmailFromRefreshToken(refreshToken)).thenReturn("test@example.com");
        when(jwtUtils.getUserIdFromRefreshToken(refreshToken)).thenReturn("user-id");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateJwtToken("test@example.com", "user-id")).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken("test@example.com", "user-id")).thenReturn("new-refresh-token");
        
        // Act
        ResponseEntity<?> response = authController.refreshToken(request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        RefreshTokenResponse responseBody = (RefreshTokenResponse) response.getBody();
        assertNotNull(responseBody);
        assertEquals("new-access-token", responseBody.getAccessToken());
        assertEquals("new-refresh-token", responseBody.getRefreshToken());
        assertEquals("Bearer", responseBody.getTokenType());
        assertEquals(3600, responseBody.getExpiresIn()); // 3600000ms / 1000
        assertEquals(2592000, responseBody.getRefreshExpiresIn()); // 2592000000ms / 1000
    }

    @Test
    void refreshToken_WithInvalidRefreshToken_ShouldReturnUnauthorized() {
        // Arrange
        String refreshToken = "invalid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        when(jwtUtils.validateRefreshToken(refreshToken)).thenReturn(false);
        
        // Act
        ResponseEntity<?> response = authController.refreshToken(request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertEquals("Invalid or expired refresh token", responseBody.getError());
    }

    @Test
    void refreshToken_WithMissingRefreshToken_ShouldReturnUnauthorized() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest(null);
        
        // Act
        ResponseEntity<?> response = authController.refreshToken(request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertEquals("Refresh token is required", responseBody.getError());
    }

    @Test
    void refreshToken_WithEmptyRefreshToken_ShouldReturnUnauthorized() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("   ");
        
        // Act
        ResponseEntity<?> response = authController.refreshToken(request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertEquals("Refresh token is required", responseBody.getError());
    }

    @Test
    void refreshToken_WithNonExistentUser_ShouldReturnUnauthorized() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        when(jwtUtils.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getEmailFromRefreshToken(refreshToken)).thenReturn("nonexistent@example.com");
        when(jwtUtils.getUserIdFromRefreshToken(refreshToken)).thenReturn("user-id");
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<?> response = authController.refreshToken(request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertEquals("User not found", responseBody.getError());
    }

    @Test
    void refreshToken_WithTokenRotation_ShouldReturnDifferentRefreshToken() {
        // Arrange
        String oldRefreshToken = "old-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);
        
        User user = new User();
        user.setId("user-id");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setRoles(new HashSet<>());
        
        when(jwtUtils.validateRefreshToken(oldRefreshToken)).thenReturn(true);
        when(jwtUtils.getEmailFromRefreshToken(oldRefreshToken)).thenReturn("test@example.com");
        when(jwtUtils.getUserIdFromRefreshToken(oldRefreshToken)).thenReturn("user-id");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateJwtToken("test@example.com", "user-id")).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken("test@example.com", "user-id")).thenReturn("new-refresh-token");
        
        // Act
        ResponseEntity<?> response = authController.refreshToken(request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        RefreshTokenResponse responseBody = (RefreshTokenResponse) response.getBody();
        assertNotNull(responseBody);
        // Verify that new refresh token is different from old one (token rotation)
        assertNotEquals(oldRefreshToken, responseBody.getRefreshToken());
        assertEquals("new-refresh-token", responseBody.getRefreshToken());
    }
} 