package com.github.istin.dmtools.auth.web;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.web.dto.AuthConfigurationDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/config")
public class AuthConfigurationController {

    private final AuthConfigProperties authConfigProperties;

    public AuthConfigurationController(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    @GetMapping
    public AuthConfigurationDto getAuthConfiguration() {
        return new AuthConfigurationDto(
                authConfigProperties.isLocalStandaloneMode(),
                authConfigProperties.getEnabledProviders()
        );
    }
}
