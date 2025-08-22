package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.config.AuthProperties;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private OAuth2UserRequest createOAuth2UserRequest(String registrationId, String email, String name) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationUri("auth-uri")
                .tokenUri("token-uri")
                .redirectUri("redirect-uri")
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("openid", "email", "profile")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", Instant.now(), Instant.now().plusSeconds(3600));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("sub", "providerId123"); // For Google
        attributes.put("id", "providerId123"); // For GitHub/Microsoft
        attributes.put("displayName", name); // For Microsoft
        attributes.put("given_name", "Given");
        attributes.put("family_name", "Family");
        attributes.put("picture", "http://example.com/pic.jpg");
        attributes.put("avatar_url", "http://example.com/avatar.jpg");
        attributes.put("locale", "en");
        attributes.put("preferredLanguage", "en");

        OAuth2User oauth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "name");

        return new OAuth2UserRequest(clientRegistration, accessToken, oauth2User.getAttributes());
    }

    @Test
    void loadUser_emailDomainPermitted() {
        when(authProperties.isEmailDomainRestrictionEnabled()).thenReturn(true);
        when(authProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com"));
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(new User());

        OAuth2UserRequest userRequest = createOAuth2UserRequest("google", "test@example.com", "Test User");
        assertDoesNotThrow(() -> customOAuth2UserService.loadUser(userRequest));
        verify(userService, times(1)).createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }

    @Test
    void loadUser_emailDomainNotPermitted() {
        when(authProperties.isEmailDomainRestrictionEnabled()).thenReturn(true);
        when(authProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("allowed.com"));

        OAuth2UserRequest userRequest = createOAuth2UserRequest("google", "test@example.com", "Test User");
        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () -> customOAuth2UserService.loadUser(userRequest));
        assertEquals("Email domain not permitted.", exception.getError().getDescription());
        verify(userService, never()).createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }

    @Test
    void loadUser_emailDomainRestrictionDisabled() {
        when(authProperties.isEmailDomainRestrictionEnabled()).thenReturn(false);
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(new User());

        OAuth2UserRequest userRequest = createOAuth2UserRequest("google", "test@anydomain.com", "Test User");
        assertDoesNotThrow(() -> customOAuth2UserService.loadUser(userRequest));
        verify(userService, times(1)).createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }

    @Test
    void loadUser_emailNotFoundAndRestrictionEnabled() {
        when(authProperties.isEmailDomainRestrictionEnabled()).thenReturn(true);
        when(authProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com"));

        OAuth2UserRequest userRequest = createOAuth2UserRequest("google", null, "Test User"); // No email
        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () -> customOAuth2UserService.loadUser(userRequest));
        assertEquals("Email not found for domain restriction.", exception.getError().getDescription());
        verify(userService, never()).createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }

    @Test
    void loadUser_emailNotFoundAndRestrictionDisabled() {
        when(authProperties.isEmailDomainRestrictionEnabled()).thenReturn(false);
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(new User());

        OAuth2UserRequest userRequest = createOAuth2UserRequest("google", null, "Test User"); // No email
        assertDoesNotThrow(() -> customOAuth2UserService.loadUser(userRequest));
        verify(userService, times(1)).createOrUpdateUser(eq(null), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }
}
