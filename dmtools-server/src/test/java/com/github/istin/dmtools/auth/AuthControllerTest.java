package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthConfigProperties authConfigProperties;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthController authController;

    private static final String TEST_USERNAME = "testadmin";
    private static final String TEST_PASSWORD = "testpass";
    private static final String TEST_JWT = "mocked-jwt-token";
    private static final int JWT_EXPIRATION_MS = 86400000;

    @BeforeEach
    void setUp() {
        when(authConfigProperties.getAdminUsername()).thenReturn(TEST_USERNAME);
        when(authConfigProperties.getAdminPassword()).thenReturn(TEST_PASSWORD);
        when(jwtUtils.generateJwtTokenCustom(anyString(), anyString(), anyString(), anyInt())).thenReturn(TEST_JWT);
        // Set the jwtExpirationMs for the controller
        // ReflectionTestUtils.setField(authController, "jwtExpirationMs", JWT_EXPIRATION_MS);
    }

    @Test
    void testLocalLogin_successInStandaloneMode() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        User mockUser = new User();
        mockUser.setId("user-id-123");
        mockUser.setEmail(TEST_USERNAME + "@local.test");
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(mockUser);
        when(userService.getUserRole(any(User.class))).thenReturn("ADMIN");

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", TEST_USERNAME);
        loginBody.put("password", TEST_PASSWORD);

        ResponseEntity<?> response = authController.localLogin(loginBody, httpServletResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(TEST_JWT, responseBody.get("token"));
        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");
        assertNotNull(userMap);
        assertEquals("user-id-123", userMap.get("id"));
        assertEquals("ADMIN", userMap.get("role"));
        verify(httpServletResponse).addCookie(any(Cookie.class));
    }

    @Test
    void testLocalLogin_invalidCredentialsInStandaloneMode() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", TEST_USERNAME);
        loginBody.put("password", "wrongpass");

        ResponseEntity<?> response = authController.localLogin(loginBody, httpServletResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Invalid credentials", responseBody.get("error"));
    }

    @Test
    void testLocalLogin_notInStandaloneMode() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", TEST_USERNAME);
        loginBody.put("password", TEST_PASSWORD);

        ResponseEntity<?> response = authController.localLogin(loginBody, httpServletResponse);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Local authentication is disabled.", responseBody.get("error"));
    }

    @Test
    void testLocalLogin_userServiceThrowsException() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenThrow(new RuntimeException("DB error"));

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", TEST_USERNAME);
        loginBody.put("password", TEST_PASSWORD);

        ResponseEntity<?> response = authController.localLogin(loginBody, httpServletResponse);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Login failed: DB error", responseBody.get("error"));
    }
}