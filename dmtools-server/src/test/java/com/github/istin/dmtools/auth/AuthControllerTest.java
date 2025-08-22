package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthProperties;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthProperties authProperties;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    private final String JWT_SECRET = "testSecret";
    private final int JWT_EXPIRATION_MS = 3600000;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(userService, jwtUtils, authProperties, authenticationManager);
        // Set @Value fields using reflection or by passing them in constructor if available
        // For simplicity, assuming direct field injection for @Value in tests or setting them up via constructor if possible
        // In a real Spring Boot test, these would be handled by @SpringBootTest or @WebMvcTest
        org.springframework.test.util.ReflectionTestUtils.setField(authController, "jwtSecret", JWT_SECRET);
        org.springframework.test.util.ReflectionTestUtils.setField(authController, "jwtExpirationMs", JWT_EXPIRATION_MS);
    }

    @Test
    void localLogin_success() throws Exception {
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("admin@local.test");
        mockUser.setName("admin");
        mockUser.setProvider(AuthProvider.LOCAL);

        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(mockUser);
        when(jwtUtils.generateJwtTokenCustom(anyString(), anyLong(), anyString(), anyInt()))
                .thenReturn("mock-jwt-token");
        when(userService.getUserRole(any(User.class))).thenReturn("ADMIN");

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "admin");
        loginBody.put("password", "admin");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(200, responseEntity.getStatusCode().value());
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("mock-jwt-token", responseBody.get("token"));
        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");
        assertNotNull(userMap);
        assertEquals("admin@local.test", userMap.get("email"));
        assertEquals("ADMIN", userMap.get("role"));
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void localLogin_localModeDisabled() {
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(false);

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "admin");
        loginBody.put("password", "admin");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(403, responseEntity.getStatusCode().value());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Local auth disabled", responseBody.get("error"));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void localLogin_invalidCredentials() {
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "wronguser");
        loginBody.put("password", "wrongpass");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(401, responseEntity.getStatusCode().value());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Invalid credentials", responseBody.get("error"));
        verify(userService, never()).createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }

    @Test
    void localLogin_exceptionDuringUserCreation() {
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenThrow(new RuntimeException("DB error"));

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "admin");
        loginBody.put("password", "admin");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(500, responseEntity.getStatusCode().value());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Login failed: DB error", responseBody.get("error"));
    }
}