package com.github.istin.dmtools.auth.provider;

import com.github.istin.dmtools.auth.service.LocalUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalAuthenticationProviderTest {

    @Mock
    private LocalUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private LocalAuthenticationProvider localAuthenticationProvider;

    @BeforeEach
    void setUp() {
        localAuthenticationProvider = new LocalAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Test
    void testAuthenticate_success() {
        String username = "admin";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        UserDetails userDetails = new User(username, encodedPassword, new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, rawPassword);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        Authentication result = localAuthenticationProvider.authenticate(authentication);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(username, result.getName());
        assertEquals(rawPassword, result.getCredentials());
    }

    @Test
    void testAuthenticate_badCredentials() {
        String username = "admin";
        String rawPassword = "wrongpassword";
        String encodedPassword = "encodedPassword";

        UserDetails userDetails = new User(username, encodedPassword, new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, rawPassword);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () -> {
            localAuthenticationProvider.authenticate(authentication);
        });

        assertEquals("Invalid username or password", thrown.getMessage());
    }

    @Test
    void testSupports() {
        assertTrue(localAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(localAuthenticationProvider.supports(Object.class));
    }
}
