package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private AuthConfigProperties authConfigProperties;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(authConfigProperties);
    }

    private OAuth2User createOAuth2User(String email) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "12345");
        attributes.put("name", "Test User");
        if (email != null) {
            attributes.put("email", email);
        }
        return new DefaultOAuth2User(Collections.emptyList(), attributes, "sub");
    }

    @Test
    void testLoadUser_noEmailAttribute_throwsException() {
        OAuth2User oAuth2User = createOAuth2User(null);
        when(oAuth2UserRequest.getAccessToken()).thenReturn(null); // Mocking access token to avoid NPE in super.loadUser

        InternalAuthenticationServiceException thrown = assertThrows(InternalAuthenticationServiceException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });

        assertEquals("OAuth2 user has no email attribute", thrown.getMessage());
    }

    @Test
    void testLoadUser_noDomainRestrictions_success() {
        OAuth2User oAuth2User = createOAuth2User("test@example.com");
        when(oAuth2UserRequest.getAccessToken()).thenReturn(null); // Mocking access token to avoid NPE in super.loadUser
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Collections.emptyList());

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getAttribute("email"));
    }

    @Test
    void testLoadUser_permittedDomain_success() {
        OAuth2User oAuth2User = createOAuth2User("test@example.com");
        when(oAuth2UserRequest.getAccessToken()).thenReturn(null); // Mocking access token to avoid NPE in super.loadUser
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(List.of("example.com", "another.com"));

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getAttribute("email"));
    }

    @Test
    void testLoadUser_nonPermittedDomain_throwsException() {
        OAuth2User oAuth2User = createOAuth2User("test@bad-domain.com");
        when(oAuth2UserRequest.getAccessToken()).thenReturn(null); // Mocking access token to avoid NPE in super.loadUser
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(List.of("example.com", "another.com"));

        InternalAuthenticationServiceException thrown = assertThrows(InternalAuthenticationServiceException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });

        assertEquals("Email domain not permitted", thrown.getMessage());
    }
}
