package com.github.istin.dmtools.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.auth.config.AuthProperties;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({AuthProperties.class, JwtUtils.class}) // Import AuthProperties and JwtUtils for context
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthProperties authProperties; // Mock AuthProperties to control its behavior

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("admin@local.test");
        testUser.setName("admin");
        testUser.setProvider(AuthProvider.LOCAL);

        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(testUser);
        when(userService.getUserRole(any(User.class))).thenReturn("ADMIN");
        when(jwtUtils.generateJwtTokenCustom(anyString(), anyString(), anyString(), anyInt())).thenReturn("mock-jwt-token");
    }

    @Test
    void testGetAuthConfig_localStandaloneMode() throws Exception {
        when(authProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authProperties.getEnabledProviders()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localStandaloneMode").value(true))
                .andExpect(jsonPath("$.enabledProviders").isEmpty());
    }

    @Test
    void testGetAuthConfig_externalProvidersEnabled() throws Exception {
        when(authProperties.isLocalStandaloneMode()).thenReturn(false);
        when(authProperties.getEnabledProviders()).thenReturn(List.of("google", "github"));

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localStandaloneMode").value(false))
                .andExpect(jsonPath("$.enabledProviders[0]").value("google"))
                .andExpect(jsonPath("$.enabledProviders[1]").value("github"));
    }

    @Test
    void testLocalLogin_success() throws Exception {
        when(authProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authProperties.getAdminUsername()).thenReturn("admin");
        when(authProperties.getAdminPassword()).thenReturn("admin");

        Map<String, String> loginRequest = Map.of("username", "admin", "password", "admin");

        mockMvc.perform(post("/api/auth/local-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.user.email").value("admin@local.test"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void testLocalLogin_invalidCredentials() throws Exception {
        when(authProperties.isLocalStandaloneMode()).thenReturn(true);
        when(authProperties.getAdminUsername()).thenReturn("admin");
        when(authProperties.getAdminPassword()).thenReturn("admin");

        Map<String, String> loginRequest = Map.of("username", "admin", "password", "wrong");

        mockMvc.perform(post("/api/auth/local-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void testLocalLogin_localAuthDisabled() throws Exception {
        when(authProperties.isLocalStandaloneMode()).thenReturn(false);

        Map<String, String> loginRequest = Map.of("username", "admin", "password", "admin");

        mockMvc.perform(post("/api/auth/local-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Local auth disabled"));
    }
}
