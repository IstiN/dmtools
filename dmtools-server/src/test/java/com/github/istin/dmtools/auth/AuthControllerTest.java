package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Set default values for local auth
        ReflectionTestUtils.setField(authController, "localAuthEnabled", true);
        ReflectionTestUtils.setField(authController, "localUsername", "testuser");
        ReflectionTestUtils.setField(authController, "localPassword", "secret123");
        ReflectionTestUtils.setField(authController, "jwtSecret", "testSecretKeyMustBeLongEnoughForHmacSha256Algorithm");
        ReflectionTestUtils.setField(authController, "jwtExpirationMs", 3600000);
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
        
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(false, responseBody.get("authenticated"));
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
        
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(true, responseBody.get("authenticated"));
        assertEquals("test@example.com", responseBody.get("email"));
        assertEquals("Test User", responseBody.get("name"));
        assertEquals("https://example.com/avatar.jpg", responseBody.get("picture"));
        assertEquals(AuthProvider.GOOGLE, responseBody.get("provider"));
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
        
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(true, responseBody.get("authenticated"));
        assertEquals("test@example.com", responseBody.get("email"));
        assertEquals("Test User", responseBody.get("name"));
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
        
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Logged out successfully", responseBody.get("message"));
    }

    @Test
    void logout_WithNoSession_ShouldReturnSuccess() {
        // Arrange
        when(request.getSession(false)).thenReturn(null);
        
        // Act
        ResponseEntity<?> response = authController.logout(request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Logged out successfully", responseBody.get("message"));
    }

    @Test
    void localLogin_WithValidCredentials_ShouldReturnToken() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", "testuser",
            "password", "secret123"
        );
        
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
        
        // Act
        ResponseEntity<?> responseEntity = authController.localLogin(loginRequest, response);
        
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertEquals("mock-jwt-token", responseBody.get("token"));
        
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
        Map<String, String> loginRequest = Map.of(
            "username", "testuser",
            "password", "wrong-password"
        );
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Act
        ResponseEntity<?> responseEntity = authController.localLogin(loginRequest, response);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertEquals("Invalid credentials", responseBody.get("error"));
    }

    @Test
    void localLogin_WithLocalAuthDisabled_ShouldReturnForbidden() {
        // Arrange
        ReflectionTestUtils.setField(authController, "localAuthEnabled", false);
        
        Map<String, String> loginRequest = Map.of(
            "username", "testuser",
            "password", "secret123"
        );
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Act
        ResponseEntity<?> responseEntity = authController.localLogin(loginRequest, response);
        
        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertEquals("Local auth disabled", responseBody.get("error"));
    }
} 