package com.github.istin.dmtools.auth.security;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceImplTest {

    @Mock
    private AuthConfigProperties authConfigProperties;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2UserMock; // Mock for the OAuth2User returned by super.loadUser()

    @Spy // Spy on the actual service to mock super.loadUser()
    private CustomOAuth2UserServiceImpl customOAuth2UserServiceSpy;

    @BeforeEach
    void setUp() {
        // Initialize the spy with the mocked AuthConfigProperties
        customOAuth2UserServiceSpy = new CustomOAuth2UserServiceImpl(authConfigProperties);
    }

    @Test
    void loadUser_noEmailDomainRestriction() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Collections.emptyList());
        // Mock the behavior of the super.loadUser() call
        doReturn(oAuth2UserMock).when(customOAuth2UserServiceSpy).loadUser(oAuth2UserRequest);

        OAuth2User result = customOAuth2UserServiceSpy.loadUser(oAuth2UserRequest);
        assertEquals(oAuth2UserMock, result);
        verify(oAuth2UserMock, never()).getAttribute(anyString()); // No email check if no restrictions
    }

    @Test
    void loadUser_emailDomainPermitted() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com", "test.org"));
        when(oAuth2UserMock.getAttribute("email")).thenReturn("user@example.com");
        doReturn(oAuth2UserMock).when(customOAuth2UserServiceSpy).loadUser(oAuth2UserRequest);

        OAuth2User result = customOAuth2UserServiceSpy.loadUser(oAuth2UserRequest);
        assertEquals(oAuth2UserMock, result);
        verify(oAuth2UserMock, times(1)).getAttribute("email");
    }

    @Test
    void loadUser_emailDomainNotPermitted() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com"));
        when(oAuth2UserMock.getAttribute("email")).thenReturn("user@forbidden.com");
        doReturn(oAuth2UserMock).when(customOAuth2UserServiceSpy).loadUser(oAuth2UserRequest);

        InternalAuthenticationServiceException exception = assertThrows(InternalAuthenticationServiceException.class,
                () -> customOAuth2UserServiceSpy.loadUser(oAuth2UserRequest));
        assertTrue(exception.getMessage().contains("Email domain not permitted"));
        verify(oAuth2UserMock, times(1)).getAttribute("email");
    }

    @Test
    void loadUser_noEmailProvided() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com"));
        when(oAuth2UserMock.getAttribute("email")).thenReturn(null); // Simulate no email from provider
        doReturn(oAuth2UserMock).when(customOAuth2UserServiceSpy).loadUser(oAuth2UserRequest);

        InternalAuthenticationServiceException exception = assertThrows(InternalAuthenticationServiceException.class,
                () -> customOAuth2UserServiceSpy.loadUser(oAuth2UserRequest));
        assertTrue(exception.getMessage().contains("OAuth2 provider did not provide email address"));
        verify(oAuth2UserMock, times(1)).getAttribute("email");
    }
}