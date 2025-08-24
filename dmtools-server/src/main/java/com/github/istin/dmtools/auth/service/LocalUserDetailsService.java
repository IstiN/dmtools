package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class LocalUserDetailsService implements UserDetailsService {

    private final AuthConfigProperties authConfigProperties;

    public LocalUserDetailsService(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (authConfigProperties.getAdminUsername().equals(username)) {
            return new User(authConfigProperties.getAdminUsername(),
                            authConfigProperties.getAdminPassword(),
                            new ArrayList<>()); // No roles for simplicity, can be extended
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
