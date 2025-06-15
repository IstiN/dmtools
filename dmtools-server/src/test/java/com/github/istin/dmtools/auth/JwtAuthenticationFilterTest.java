package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String testSecret = "testSecretKeyMustBeLongEnoughForHmacSha256Algorithm";
    private final String testToken = "test.jwt.token";
    private final String testEmail = "test@example.com";
    private final String testUserId = "user123";

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtils, userService, testSecret);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_WithValidJwtInHeader_ShouldAuthenticate() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = new User();
        user.setId(testUserId);
        user.setEmail(testEmail);
        user.setName("Test User");
        user.setProvider(AuthProvider.GOOGLE);

        when(jwtUtils.validateJwtTokenCustom(testToken, testSecret)).thenReturn(true);
        when(jwtUtils.getEmailFromJwtTokenCustom(testToken, testSecret)).thenReturn(testEmail);
        when(jwtUtils.getUserIdFromJwtTokenCustom(testToken, testSecret)).thenReturn(testUserId);
        when(userService.findByEmail(testEmail)).thenReturn(Optional.of(user));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        
        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(securityContext).setAuthentication(authCaptor.capture());
        
        Authentication authentication = authCaptor.getValue();
        assertNotNull(authentication);
        assertEquals(testEmail, authentication.getName());
    }

    @Test
    void doFilterInternal_WithValidJwtInCookie_ShouldAuthenticate() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwt", testToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = new User();
        user.setId(testUserId);
        user.setEmail(testEmail);
        user.setName("Test User");
        user.setProvider(AuthProvider.GOOGLE);

        when(jwtUtils.validateJwtTokenCustom(testToken, testSecret)).thenReturn(true);
        when(jwtUtils.getEmailFromJwtTokenCustom(testToken, testSecret)).thenReturn(testEmail);
        when(jwtUtils.getUserIdFromJwtTokenCustom(testToken, testSecret)).thenReturn(testUserId);
        when(userService.findByEmail(testEmail)).thenReturn(Optional.of(user));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        
        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(securityContext).setAuthentication(authCaptor.capture());
        
        Authentication authentication = authCaptor.getValue();
        assertNotNull(authentication);
        assertEquals(testEmail, authentication.getName());
    }

    @Test
    void doFilterInternal_WithInvalidJwt_ShouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateJwtTokenCustom(testToken, testSecret)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_WithNoJwt_ShouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_WithValidJwtButUserNotFound_ShouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateJwtTokenCustom(testToken, testSecret)).thenReturn(true);
        when(jwtUtils.getEmailFromJwtTokenCustom(testToken, testSecret)).thenReturn(testEmail);
        when(jwtUtils.getUserIdFromJwtTokenCustom(testToken, testSecret)).thenReturn(testUserId);
        when(userService.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_WithException_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateJwtTokenCustom(testToken, testSecret)).thenThrow(new RuntimeException("Test exception"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }
} 