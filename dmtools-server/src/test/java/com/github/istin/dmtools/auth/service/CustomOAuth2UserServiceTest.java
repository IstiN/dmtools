package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.AuthConfigProperties;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthConfigProperties authConfigProperties;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private ClientRegistration clientRegistration;
    private OAuth2UserRequest userRequest;
    private OAuth2User oauth2User;

    @BeforeEach
    void setUp() {
        clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationUri("auth-uri")
                .tokenUri("token-uri")
                .userInfoUri("user-info-uri")
                .redirectUri("redirect-uri")
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("openid", "email")
                .userNameAttributeName("sub")
                .build();

        userRequest = new OAuth2UserRequest(clientRegistration, Mockito.mock(org.springframework.security.oauth2.core.OAuth2AccessToken.class));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "12345");
        attributes.put("name", "Test User");
        attributes.put("email", "test@example.com");
        oauth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "sub");

        // Mock the super.loadUser call to return our mock oauth2User
        lenient().when(customOAuth2UserService.loadUser(any(OAuth2UserRequest.class))).thenReturn(oauth2User);
    }

    @Test
    void testLoadUser_permittedDomain_success() {
        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.singletonList("example.com"));
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(new User());

        OAuth2User loadedUser = customOAuth2UserService.loadUser(userRequest);
        assertNotNull(loadedUser);
        assertEquals("test@example.com", loadedUser.getAttribute("email"));
        verify(userService).createOrUpdateUser(eq("test@example.com"), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }

    @Test
    void testLoadUser_nonPermittedDomain_throwsException() {
        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.singletonList("another.com"));

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(userRequest);
        });
        assertTrue(thrown.getMessage().contains("Email domain not permitted."));
    }

    @Test
    void testLoadUser_emptyPermittedDomains_success() {
        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.emptyList());
        when(userService.createOrUpdateUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(new User());

        OAuth2User loadedUser = customOAuth2UserService.loadUser(userRequest);
        assertNotNull(loadedUser);
        assertEquals("test@example.com", loadedUser.getAttribute("email"));
        verify(userService).createOrUpdateUser(eq("test@example.com"), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }

    @Test
    void testLoadUser_emailNotFound_throwsExceptionWhenDomainsRestricted() {
        Map<String, Object> attributesWithoutEmail = new HashMap<>();
        attributesWithoutEmail.put("sub", "12345");
        attributesWithoutEmail.put("name", "Test User");
        oauth2User = new DefaultOAuth2User(Collections.emptyList(), attributesWithoutEmail, "sub");
        lenient().when(customOAuth2UserService.loadUser(any(OAuth2UserRequest.class))).thenReturn(oauth2User);

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.singletonList("example.com"));

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(userRequest);
        });
        assertTrue(thrown.getMessage().contains("Email not found for domain restriction."));
    }

    @Test
    void testLoadUser_emailNotFound_successWhenNoDomainRestriction() {
        Map<String, Object> attributesWithoutEmail = new HashMap<>();
        attributesWithoutEmail.put("sub", "12345");
        attributesWithoutEmail.put("name", "Test User");
        oauth2User = new DefaultOAuth2User(Collections.emptyList(), attributesWithoutEmail, "sub");
        lenient().when(customOAuth2UserService.loadUser(any(OAuth2UserRequest.class))).thenReturn(oauth2User);

        when(authConfigProperties.getPermittedEmailDomainsList()).thenReturn(Collections.emptyList());
        when(userService.createOrUpdateUser(isNull(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString()))
                .thenReturn(new User());

        OAuth2User loadedUser = customOAuth2UserService.loadUser(userRequest);
        assertNotNull(loadedUser);
        assertNull(loadedUser.getAttribute("email")); // Email is null as expected
        verify(userService).createOrUpdateUser(isNull(), anyString(), anyString(), anyString(), anyString(), anyString(), any(AuthProvider.class), anyString());
    }
}
