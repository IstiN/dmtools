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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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

    private OAuth2UserRequest mockUserRequest;
    private ClientRegistration mockClientRegistration;

    @BeforeEach
    void setUp() {
        mockClientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationUri("http://test-auth")
                .tokenUri("http://test-token")
                .redirectUri("http://test-redirect")
                .scope("email")
                .userNameAttributeName("sub")
                .build();

        mockUserRequest = new OAuth2UserRequest(mockClientRegistration, null);
    }

    private OAuth2User createMockOAuth2User(String email, String name, String providerId) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("sub", providerId);
        return new DefaultOAuth2User(Collections.emptyList(), attributes, "sub");
    }

    @Test
    void testLoadUser_permittedEmailDomain_success() {
        when(authConfigProperties.isEmailDomainRestricted()).thenReturn(true);
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(List.of("example.com"));

        OAuth2User mockOAuth2User = createMockOAuth2User("test@example.com", "Test User", "12345");
        // Mock the super.loadUser call
        customOAuth2UserService = new CustomOAuth2UserService() { // Anonymous class to mock super
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                return mockOAuth2User;
            }
        };
        customOAuth2UserService.userService = userService; // Manually inject mock
        customOAuth2UserService.authConfigProperties = authConfigProperties; // Manually inject mock

        when(userService.createOrUpdateUser(anyString(), anyString(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(new User());

        OAuth2User result = customOAuth2UserService.loadUser(mockUserRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getAttributes().get("email"));
        verify(userService).createOrUpdateUser(anyString(), anyString(), any(), any(), any(), any(), any(), anyString());
    }

    @Test
    void testLoadUser_nonPermittedEmailDomain_throwsException() {
        when(authConfigProperties.isEmailDomainRestricted()).thenReturn(true);
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(List.of("example.com"));

        OAuth2User mockOAuth2User = createMockOAuth2User("test@nonpermitted.com", "Test User", "12345");
        // Mock the super.loadUser call
        customOAuth2UserService = new CustomOAuth2UserService() { // Anonymous class to mock super
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                return mockOAuth2User;
            }
        };
        customOAuth2UserService.userService = userService; // Manually inject mock
        customOAuth2UserService.authConfigProperties = authConfigProperties; // Manually inject mock

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.loadUser(mockUserRequest));

        assertTrue(thrown.getMessage().contains("Email domain not permitted."));
        verify(userService, never()).createOrUpdateUser(anyString(), anyString(), any(), any(), any(), any(), any(), anyString());
    }

    @Test
    void testLoadUser_emailDomainRestrictionDisabled_success() {
        when(authConfigProperties.isEmailDomainRestricted()).thenReturn(false);

        OAuth2User mockOAuth2User = createMockOAuth2User("test@anydomain.com", "Test User", "12345");
        // Mock the super.loadUser call
        customOAuth2UserService = new CustomOAuth2UserService() { // Anonymous class to mock super
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                return mockOAuth2User;
            }
        };
        customOAuth2UserService.userService = userService; // Manually inject mock
        customOAuth2UserService.authConfigProperties = authConfigProperties; // Manually inject mock

        when(userService.createOrUpdateUser(anyString(), anyString(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(new User());

        OAuth2User result = customOAuth2UserService.loadUser(mockUserRequest);

        assertNotNull(result);
        assertEquals("test@anydomain.com", result.getAttributes().get("email"));
        verify(userService).createOrUpdateUser(anyString(), anyString(), any(), any(), any(), any(), any(), anyString());
    }

    @Test
    void testLoadUser_emailNullAndDomainRestrictionEnabled_throwsException() {
        when(authConfigProperties.isEmailDomainRestricted()).thenReturn(true);
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(List.of("example.com"));

        OAuth2User mockOAuth2User = createMockOAuth2User(null, "Test User", "12345");
        // Mock the super.loadUser call
        customOAuth2UserService = new CustomOAuth2UserService() { // Anonymous class to mock super
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                return mockOAuth2User;
            }
        };
        customOAuth2UserService.userService = userService; // Manually inject mock
        customOAuth2UserService.authConfigProperties = authConfigProperties; // Manually inject mock

        OAuth2AuthenticationException thrown = assertThrows(OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.loadUser(mockUserRequest));

        assertTrue(thrown.getMessage().contains("Email not found for authentication."));
        verify(userService, never()).createOrUpdateUser(anyString(), anyString(), any(), any(), any(), any(), any(), anyString());
    }
}
