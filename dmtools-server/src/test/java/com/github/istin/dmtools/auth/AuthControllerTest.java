package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AuthConfigProperties authConfigProperties;
    @Mock
    private HttpServletResponse response;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userService, jwtUtils, authConfigProperties);
        ReflectionTestUtils.setField(authController, "jwtSecret", "testSecret");
        ReflectionTestUtils.setField(authController, "jwtExpirationMs", 3600000);
    }

    @Test
    void localLogin_standaloneMode_success() throws Exception {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn("admin");
        when(authConfigProperties.getAdminPassword()).thenReturn("admin");

        Map<String, String> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "admin");

        User mockUser = new User();
        mockUser.setId("admin-id");
        mockUser.setEmail("admin@local.test");
        mockUser.setName("admin");
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(mockUser);
        when(jwtUtils.generateJwtTokenCustom(anyString(), anyString(), anyString(), anyInt())).thenReturn("mock-jwt-token");
        when(userService.getUserRole(any(User.class))).thenReturn("ADMIN");

        ResponseEntity<?> responseEntity = authController.localLogin(body, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("mock-jwt-token", responseBody.get("token"));
        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");
        assertEquals("admin-id", userMap.get("id"));
        assertEquals("admin@local.test", userMap.get("email"));
        assertEquals("ADMIN", userMap.get("role"));

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("jwt", capturedCookie.getName());
        assertEquals("mock-jwt-token", capturedCookie.getValue());
        assertEquals(3600, capturedCookie.getMaxAge());
    }

    @Test
    void localLogin_standaloneMode_invalidCredentials() throws Exception {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn("admin");
        when(authConfigProperties.getAdminPassword()).thenReturn("admin");

        Map<String, String> body = new HashMap<>();
        body.put("username", "wronguser");
        body.put("password", "wrongpass");

        ResponseEntity<?> responseEntity = authController.localLogin(body, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Invalid credentials", responseBody.get("error"));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void localLogin_notStandaloneMode() throws Exception {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);

        Map<String, String> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "admin");

        ResponseEntity<?> responseEntity = authController.localLogin(body, response);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Local authentication disabled", responseBody.get("error"));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void localLogin_exceptionDuringUserCreation() throws Exception {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn("admin");
        when(authConfigProperties.getAdminPassword()).thenReturn("admin");

        Map<String, String> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "admin");

        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> responseEntity = authController.localLogin(body, response);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Login failed: DB error", responseBody.get("error"));
    }

    @Test
    void localLogin_defaultAdminCredentials() throws Exception {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn(null); // Simulate default
        when(authConfigProperties.getAdminPassword()).thenReturn(null); // Simulate default

        Map<String, String> body = new HashMap<>();
        body.put("username", "admin"); // Default username
        body.put("password", "admin"); // Default password

        User mockUser = new User();
        mockUser.setId("admin-id");
        mockUser.setEmail("admin@local.test");
        mockUser.setName("admin");
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(mockUser);
        when(jwtUtils.generateJwtTokenCustom(anyString(), anyString(), anyString(), anyInt())).thenReturn("mock-jwt-token");
        when(userService.getUserRole(any(User.class))).thenReturn("ADMIN");

        ResponseEntity<?> responseEntity = authController.localLogin(body, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}