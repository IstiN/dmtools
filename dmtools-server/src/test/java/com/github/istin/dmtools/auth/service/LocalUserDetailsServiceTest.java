package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.AuthConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalUserDetailsServiceTest {

    @Mock
    private AuthConfigProperties authConfigProperties;

    private LocalUserDetailsService localUserDetailsService;

    @BeforeEach
    void setUp() {
        localUserDetailsService = new LocalUserDetailsService(authConfigProperties);
    }

    @Test
    void testLoadUserByUsername_adminUser_returnsUserDetails() {
        when(authConfigProperties.getAdminUsername()).thenReturn("admin");
        when(authConfigProperties.getAdminPassword()).thenReturn("password");

        UserDetails userDetails = localUserDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void testLoadUserByUsername_nonAdminUser_throwsUsernameNotFoundException() {
        when(authConfigProperties.getAdminUsername()).thenReturn("admin");

        UsernameNotFoundException thrown = assertThrows(UsernameNotFoundException.class,
                () -> localUserDetailsService.loadUserByUsername("nonadmin"));

        assertEquals("User not found with username: nonadmin", thrown.getMessage());
    }
}
