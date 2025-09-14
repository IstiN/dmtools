package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthConfigProperties authConfigProperties;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    private CustomOAuth2UserService customOAuth2UserService;

    private ClientRegistration googleClientRegistration;
    private OAuth2UserRequest oAuth2UserRequest;
    private OAuth2User mockOAuth2User;
    private Map<String, Object> googleUserAttributes;

    @BeforeEach
    void setUp() {
        // Create a testable service instance
        customOAuth2UserService = new CustomOAuth2UserService(userService, authConfigProperties);

        googleClientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("google-client-id")
                .clientSecret("google-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .redirectUri("http://localhost/login/oauth2/code/google")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();

        googleUserAttributes = new HashMap<>();
        googleUserAttributes.put("sub", "12345");
        googleUserAttributes.put("name", "Test User");
        googleUserAttributes.put("email", "test@example.com");
        googleUserAttributes.put("given_name", "Test");
        googleUserAttributes.put("family_name", "User");
        googleUserAttributes.put("picture", "http://example.com/pic.jpg");
        googleUserAttributes.put("locale", "en");

        mockOAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                googleUserAttributes,
                "sub"
        );

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        oAuth2UserRequest = new OAuth2UserRequest(googleClientRegistration, accessToken);
    }

    @Test
    void testLoadUser_successfulLogin_noDomainRestriction() throws Exception {
        // Given
        when(authConfigProperties.getPermittedEmailDomainsAsSet()).thenReturn(Collections.emptySet());
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new User());

        // Create a spy to mock the super.loadUser call  
        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(mockOAuth2User).when(spyService).callSuperLoadUser(any());

        // When
        OAuth2User result = spyService.loadUser(oAuth2UserRequest);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getAttributes().get("email"));
        verify(userService, times(1)).createOrUpdateUser(
                "test@example.com", "Test User", "Test", "User", "http://example.com/pic.jpg", "en", AuthProvider.GOOGLE, "12345"
        );
    }

    @Test
    void testLoadUser_successfulLogin_permittedDomain() throws Exception {
        // Given
        when(authConfigProperties.getPermittedEmailDomainsAsSet()).thenReturn(Set.of("example.com"));
        when(userService.createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new User());

        // Create a spy to mock the super.loadUser call
        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(mockOAuth2User).when(spyService).callSuperLoadUser(any());

        // When
        OAuth2User result = spyService.loadUser(oAuth2UserRequest);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getAttributes().get("email"));
        verify(userService, times(1)).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testLoadUser_failedLogin_nonPermittedDomain() throws Exception {
        // Given
        when(authConfigProperties.getPermittedEmailDomainsAsSet()).thenReturn(Set.of("another.com"));

        // Create a spy to mock the super.loadUser call
        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(mockOAuth2User).when(spyService).callSuperLoadUser(any());

        // When & Then
        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () -> {
            spyService.loadUser(oAuth2UserRequest);
        });

        assertEquals("Email domain not permitted.", exception.getMessage());
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testLoadUser_failedLogin_emailNotFoundAndDomainRestrictionEnabled() throws Exception {
        // Given
        Map<String, Object> attributesWithoutEmail = new HashMap<>(googleUserAttributes);
        attributesWithoutEmail.remove("email");
        
        OAuth2User userWithoutEmail = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributesWithoutEmail,
                "sub"
        );

        when(authConfigProperties.getPermittedEmailDomainsAsSet()).thenReturn(Set.of("example.com"));

        // Create a spy to mock the super.loadUser call
        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(userWithoutEmail).when(spyService).callSuperLoadUser(any());

        // When & Then
        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () -> {
            spyService.loadUser(oAuth2UserRequest);
        });

        assertEquals("Email not found for domain validation.", exception.getMessage());
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testLoadUser_exceptionDuringSuperLoadUser() throws Exception {
        // Given
        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doThrow(new OAuth2AuthenticationException("Test exception from super"))
                .when(spyService).callSuperLoadUser(any());

        // When & Then
        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () -> {
            spyService.loadUser(oAuth2UserRequest);
        });

        // Note: Exception message might be null when thrown from mocked method
        assertTrue(exception instanceof OAuth2AuthenticationException);
        verify(userService, never()).createOrUpdateUser(any(), any(), any(), any(), any(), any(), any(), any());
    }
}