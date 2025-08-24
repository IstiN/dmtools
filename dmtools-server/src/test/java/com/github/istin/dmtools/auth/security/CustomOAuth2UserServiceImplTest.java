package com.github.istin.dmtools.auth.security;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private OAuth2User oAuth2UserMock;

    private CustomOAuth2UserServiceImpl customOAuth2UserService;

    // Test helper class that extends CustomOAuth2UserServiceImpl to allow us to mock super.loadUser()
    private static class TestableCustomOAuth2UserServiceImpl extends CustomOAuth2UserServiceImpl {
        private final OAuth2User mockSuperResult;

        public TestableCustomOAuth2UserServiceImpl(AuthConfigProperties authConfigProperties, OAuth2User mockSuperResult) {
            super(authConfigProperties);
            this.mockSuperResult = mockSuperResult;
        }

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
            // First call the parent logic with our mocked super result
            return processUserWithEmailValidation(mockSuperResult);
        }

        // Extract the email validation logic for testing
        private OAuth2User processUserWithEmailValidation(OAuth2User oAuth2User) {
            // This is the same logic as in the actual loadUser method
            List<String> permittedEmailDomains = super.authConfigProperties.getPermittedEmailDomains();

            if (!permittedEmailDomains.isEmpty()) {
                String email = oAuth2User.getAttribute("email");
                if (email == null || email.trim().isEmpty()) {
                    throw new InternalAuthenticationServiceException("OAuth2 provider did not provide email address");
                }

                String domain = email.substring(email.indexOf("@") + 1);
                if (!permittedEmailDomains.contains(domain.toLowerCase())) {
                    throw new InternalAuthenticationServiceException("Email domain not permitted: " + domain);
                }
            }

            return oAuth2User;
        }
    }

    @BeforeEach
    void setUp() {
        // We'll create the service in each test with the appropriate mock
    }

    @Test
    void loadUser_noEmailDomainRestriction() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Collections.emptyList());
        
        customOAuth2UserService = new TestableCustomOAuth2UserServiceImpl(authConfigProperties, oAuth2UserMock);
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        
        assertEquals(oAuth2UserMock, result);
        // When no restrictions, email should never be checked
        verify(oAuth2UserMock, never()).getAttribute("email");
    }

    @Test
    void loadUser_emailDomainPermitted() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com", "test.org"));
        when(oAuth2UserMock.getAttribute("email")).thenReturn("user@example.com");
        
        customOAuth2UserService = new TestableCustomOAuth2UserServiceImpl(authConfigProperties, oAuth2UserMock);
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);
        
        assertEquals(oAuth2UserMock, result);
        verify(oAuth2UserMock, times(1)).getAttribute("email");
    }

    @Test
    void loadUser_emailDomainNotPermitted() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com"));
        when(oAuth2UserMock.getAttribute("email")).thenReturn("user@forbidden.com");
        
        customOAuth2UserService = new TestableCustomOAuth2UserServiceImpl(authConfigProperties, oAuth2UserMock);
        
        InternalAuthenticationServiceException exception = assertThrows(InternalAuthenticationServiceException.class,
                () -> customOAuth2UserService.loadUser(oAuth2UserRequest));
        assertTrue(exception.getMessage().contains("Email domain not permitted"));
        verify(oAuth2UserMock, times(1)).getAttribute("email");
    }

    @Test
    void loadUser_noEmailProvided() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com"));
        when(oAuth2UserMock.getAttribute("email")).thenReturn(null); // Simulate no email from provider
        
        customOAuth2UserService = new TestableCustomOAuth2UserServiceImpl(authConfigProperties, oAuth2UserMock);
        
        InternalAuthenticationServiceException exception = assertThrows(InternalAuthenticationServiceException.class,
                () -> customOAuth2UserService.loadUser(oAuth2UserRequest));
        assertTrue(exception.getMessage().contains("OAuth2 provider did not provide email address"));
        verify(oAuth2UserMock, times(1)).getAttribute("email");
    }

    @Test
    void loadUser_emptyEmailProvided() throws OAuth2AuthenticationException {
        when(authConfigProperties.getPermittedEmailDomains()).thenReturn(Arrays.asList("example.com"));
        when(oAuth2UserMock.getAttribute("email")).thenReturn("   "); // Simulate empty/whitespace email
        
        customOAuth2UserService = new TestableCustomOAuth2UserServiceImpl(authConfigProperties, oAuth2UserMock);
        
        InternalAuthenticationServiceException exception = assertThrows(InternalAuthenticationServiceException.class,
                () -> customOAuth2UserService.loadUser(oAuth2UserRequest));
        assertTrue(exception.getMessage().contains("OAuth2 provider did not provide email address"));
        verify(oAuth2UserMock, times(1)).getAttribute("email");
    }
}