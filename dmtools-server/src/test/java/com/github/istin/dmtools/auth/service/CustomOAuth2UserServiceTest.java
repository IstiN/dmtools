package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.config.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private AuthProperties authProperties;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2User;

    @Spy
    private CustomOAuth2UserService customOAuth2UserService = new CustomOAuth2UserService(authProperties);

    @BeforeEach
    void setUp() {
        // Mock the super.loadUser call to return our mocked OAuth2User
        doReturn(oAuth2User).when(customOAuth2UserService).loadUser(oAuth2UserRequest);
    }

    @Test
    void testLoadUser_permittedDomain() {
        when(authProperties.getPermittedEmailDomains()).thenReturn(List.of("example.com"));
        when(oAuth2User.getAttribute("email")).thenReturn("user@example.com");

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        assertEquals(oAuth2User, result);
    }

    @Test
    void testLoadUser_nonPermittedDomain_throwsException() {
        when(authProperties.getPermittedEmailDomains()).thenReturn(List.of("example.com"));
        when(oAuth2User.getAttribute("email")).thenReturn("user@other.com");

        InternalAuthenticationServiceException exception = assertThrows(InternalAuthenticationServiceException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
        assertTrue(exception.getMessage().contains("Email domain not permitted"));
    }

    @Test
    void testLoadUser_emailNotFound_throwsException() {
        when(authProperties.getPermittedEmailDomains()).thenReturn(List.of("example.com"));
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        InternalAuthenticationServiceException exception = assertThrows(InternalAuthenticationServiceException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
        assertTrue(exception.getMessage().contains("Email not found from OAuth2 provider"));
    }

    @Test
    void testLoadUser_emptyPermittedDomains_allowsAny() {
        when(authProperties.getPermittedEmailDomains()).thenReturn(Collections.emptyList());
        when(oAuth2User.getAttribute("email")).thenReturn("user@any.com");

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        assertEquals(oAuth2User, result);
    }
}