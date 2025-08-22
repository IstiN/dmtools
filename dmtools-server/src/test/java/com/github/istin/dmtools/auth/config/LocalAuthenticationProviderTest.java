package com.github.istin.dmtools.auth.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class LocalAuthenticationProviderTest {

    @Mock
    private AuthProperties authProperties;

    private LocalAuthenticationProvider localAuthenticationProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        localAuthenticationProvider = new LocalAuthenticationProvider(authProperties);
    }

    @Test
    void authenticate_success() {
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(true);
        when(authProperties.getAdminUsername()).thenReturn("admin");
        when(authProperties.getAdminPassword()).thenReturn("admin");

        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin");
        Authentication result = localAuthenticationProvider.authenticate(authentication);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals("admin", result.getPrincipal());
        assertEquals("admin", result.getCredentials());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void authenticate_invalidCredentials() {
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(true);
        when(authProperties.getAdminUsername()).thenReturn("admin");
        when(authProperties.getAdminPassword()).thenReturn("admin");

        Authentication authentication = new UsernamePasswordAuthenticationToken("wronguser", "wrongpass");
        assertThrows(BadCredentialsException.class, () -> localAuthenticationProvider.authenticate(authentication));
    }

    @Test
    void authenticate_localModeDisabled() {
        when(authProperties.isLocalStandaloneModeEnabled()).thenReturn(false);

        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin");
        assertThrows(BadCredentialsException.class, () -> localAuthenticationProvider.authenticate(authentication));
    }

    @Test
    void supports() {
        assertTrue(localAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(localAuthenticationProvider.supports(Object.class));
    }
}
