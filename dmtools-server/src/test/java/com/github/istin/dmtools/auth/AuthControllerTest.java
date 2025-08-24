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

import java.util.Collections;
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

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("testadmin@local.test");
        testUser.setName("testadmin");
        testUser.setProvider(AuthProvider.LOCAL);

        // Manually set @Value fields for regular local auth testing
        authController = new AuthController(userService, jwtUtils);
        authController.authConfigProperties = authConfigProperties;
        authController.setLocalAuthEnabled(true);
        authController.setLocalUsername("testuser");
        authController.setLocalPassword("secret123");
        authController.setJwtSecret("testsecret");
        authController.setJwtExpirationMs(86400000);
    }

    @Test
    void testLocalLogin_standaloneMode_success() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn("admin");
        when(authConfigProperties.getAdminPassword()).thenReturn("admin");
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(testUser);
        when(userService.getUserRole(any(User.class))).thenReturn("ADMIN");
        when(jwtUtils.generateJwtTokenCustom(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn("mock-jwt-token");

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "admin");
        loginBody.put("password", "admin");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("mock-jwt-token", responseBody.get("token"));
        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");
        assertEquals("ADMIN", userMap.get("role"));
        verify(response).addCookie(any(Cookie.class));
        verify(userService).createOrUpdateUser("admin@local.test", "admin", "admin", "", "", "en", AuthProvider.LOCAL, "admin");
    }

    @Test
    void testLocalLogin_standaloneMode_invalidCredentials() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn("admin");
        when(authConfigProperties.getAdminPassword()).thenReturn("admin");

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "wrongadmin");
        loginBody.put("password", "wrongpass");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Invalid credentials", responseBody.get("error"));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void testLocalLogin_regularMode_success() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(testUser);
        when(userService.getUserRole(any(User.class))).thenReturn("REGULAR_USER");
        when(jwtUtils.generateJwtTokenCustom(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn("mock-jwt-token");

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "testuser");
        loginBody.put("password", "secret123");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("mock-jwt-token", responseBody.get("token"));
        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");
        assertEquals("REGULAR_USER", userMap.get("role"));
        verify(response).addCookie(any(Cookie.class));
        verify(userService).createOrUpdateUser("testuser@local.test", "testuser", "testuser", "", "", "en", AuthProvider.LOCAL, "testuser");
    }

    @Test
    void testLocalLogin_regularMode_invalidCredentials() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "testuser");
        loginBody.put("password", "wrongpass");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Invalid credentials", responseBody.get("error"));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void testLocalLogin_regularMode_localAuthDisabled() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(false);
        authController.setLocalAuthEnabled(false);

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "testuser");
        loginBody.put("password", "secret123");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Local auth disabled", responseBody.get("error"));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void testLocalLogin_exceptionDuringUserCreation() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn("admin");
        when(authConfigProperties.getAdminPassword()).thenReturn("admin");
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "admin");
        loginBody.put("password", "admin");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("Login failed: DB error", responseBody.get("error"));
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void testLocalLogin_usernameWithEmailFormat() {
        when(authConfigProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authConfigProperties.getAdminUsername()).thenReturn("admin@example.com");
        when(authConfigProperties.getAdminPassword()).thenReturn("admin");
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(testUser);
        when(userService.getUserRole(any(User.class))).thenReturn("ADMIN");
        when(jwtUtils.generateJwtTokenCustom(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn("mock-jwt-token");

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "admin@example.com");
        loginBody.put("password", "admin");

        ResponseEntity<?> responseEntity = authController.localLogin(loginBody, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(userService).createOrUpdateUser("admin@example.com", "admin@example.com", "admin@example.com", "", "", "en", AuthProvider.LOCAL, "admin@example.com");
    }
}