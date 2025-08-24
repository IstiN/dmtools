package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.AuthConfigProperties;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthConfigProperties authConfigProperties;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private ClientRegistration clientRegistration;

    private Map<String, Object> userAttributes;

    @BeforeEach
    void setUp() {
        userAttributes = new HashMap<>();
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(oAuth2User.getAttributes()).thenReturn(userAttributes);
        when(customOAuth2UserService.loadUser(oAuth2UserRequest)).thenCallRealMethod(); // Allow real method to be called for testing
    }

    @Test
    void testLoadUser_google_success_noDomainRestriction() {
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        userAttributes.put("email", "test@example.com");
        userAttributes.put("name", "Test User");
        userAttributes.put("given_name", "Test");
        userAttributes.put("family_name", "User");
        userAttributes.put("picture", "http://example.com/pic.jpg");
        userAttributes.put("locale", "en");
        userAttributes.put("sub", "12345");

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.emptyList());
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new User());

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        assertNotNull(result);
        verify(userService).createOrUpdateUser(
                "test@example.com", "Test User", "Test", "User", "http://example.com/pic.jpg", "en", AuthProvider.GOOGLE, "12345"
        );
    }

    @Test
    void testLoadUser_google_success_withPermittedDomain() {
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        userAttributes.put("email", "test@example.com");
        userAttributes.put("name", "Test User");
        userAttributes.put("given_name", "Test");
        userAttributes.put("family_name", "User");
        userAttributes.put("picture", "http://example.com/pic.jpg");
        userAttributes.put("locale", "en");
        userAttributes.put("sub", "12345");

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(List.of("example.com", "test.org"));
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new User());

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        assertNotNull(result);
        verify(userService).createOrUpdateUser(
                "test@example.com", "Test User", "Test", "User", "http://example.com/pic.jpg", "en", AuthProvider.GOOGLE, "12345"
        );
    }

    @Test
    void testLoadUser_google_failure_nonPermittedDomain() {
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        userAttributes.put("email", "test@another.com");
        userAttributes.put("name", "Test User");

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(List.of("example.com", "test.org"));

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
        assertTrue(thrown.getMessage().contains("Email domain not permitted."));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testLoadUser_google_failure_emailNotFound() {
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        userAttributes.remove("email"); // No email attribute
        userAttributes.put("name", "Test User");

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(List.of("example.com"));

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
        assertTrue(thrown.getMessage().contains("Email not found for authentication."));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testLoadUser_microsoft_success_noDomainRestriction() {
        when(clientRegistration.getRegistrationId()).thenReturn("microsoft");
        userAttributes.put("mail", "test@example.com");
        userAttributes.put("displayName", "Test User MS");
        userAttributes.put("givenName", "TestMS");
        userAttributes.put("surname", "UserMS");
        userAttributes.put("id", "ms-id-123");

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.emptyList());
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new User());

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        assertNotNull(result);
        verify(userService).createOrUpdateUser(
                "test@example.com", "Test User MS", "TestMS", "UserMS", null, null, AuthProvider.MICROSOFT, "ms-id-123"
        );
    }

    @Test
    void testLoadUser_microsoft_success_withPermittedDomain() {
        when(clientRegistration.getRegistrationId()).thenReturn("microsoft");
        userAttributes.put("mail", "test@example.com");
        userAttributes.put("displayName", "Test User MS");
        userAttributes.put("givenName", "TestMS");
        userAttributes.put("surname", "UserMS");
        userAttributes.put("id", "ms-id-123");

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(List.of("example.com"));
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new User());

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        assertNotNull(result);
        verify(userService).createOrUpdateUser(
                "test@example.com", "Test User MS", "TestMS", "UserMS", null, null, AuthProvider.MICROSOFT, "ms-id-123"
        );
    }

    @Test
    void testLoadUser_microsoft_failure_nonPermittedDomain() {
        when(clientRegistration.getRegistrationId()).thenReturn("microsoft");
        userAttributes.put("mail", "test@another.com");
        userAttributes.put("displayName", "Test User MS");

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(List.of("example.com"));

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
        assertTrue(thrown.getMessage().contains("Email domain not permitted."));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testLoadUser_github_success_noDomainRestriction() {
        when(clientRegistration.getRegistrationId()).thenReturn("github");
        userAttributes.put("email", "test@example.com");
        userAttributes.put("name", "Test User GH");
        userAttributes.put("id", 123L);

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.emptyList());
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new User());

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        assertNotNull(result);
        verify(userService).createOrUpdateUser(
                "test@example.com", "Test User GH", null, null, null, null, AuthProvider.GITHUB, "123"
        );
    }

    @Test
    void testLoadUser_github_failure_nonPermittedDomain() {
        when(clientRegistration.getRegistrationId()).thenReturn("github");
        userAttributes.put("email", "test@another.com");
        userAttributes.put("name", "Test User GH");

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(List.of("example.com"));

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
        assertTrue(thrown.getMessage().contains("Email domain not permitted."));
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testLoadUser_exceptionHandling() {
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        userAttributes.put("email", "test@example.com");
        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.emptyList());
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
        assertTrue(thrown.getMessage().contains("Failed to process OAuth2 user from google: DB error"));
    }
}