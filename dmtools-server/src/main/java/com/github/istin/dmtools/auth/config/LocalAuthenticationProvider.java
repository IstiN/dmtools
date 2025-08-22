package com.github.istin.dmtools.auth.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class LocalAuthenticationProvider implements AuthenticationProvider {

    private final AuthProperties authProperties;

    public LocalAuthenticationProvider(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (authProperties.isLocalStandaloneModeEnabled() &&
                username.equals(authProperties.getAdminUsername()) &&
                password.equals(authProperties.getAdminPassword())) {
            return new UsernamePasswordAuthenticationToken(
                    username,
                    password,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        } else {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
