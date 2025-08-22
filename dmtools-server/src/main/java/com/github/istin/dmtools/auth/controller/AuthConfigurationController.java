package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.config.AuthProperties;
import com.github.istin.dmtools.auth.dto.AuthConfigurationDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthConfigurationController {

    private final AuthProperties authProperties;

    public AuthConfigurationController(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @GetMapping("/config")
    public AuthConfigurationDto getAuthConfiguration() {
        List<String> enabledProviders = authProperties.getEnabledProviders();
        boolean localStandaloneModeEnabled = authProperties.isLocalStandaloneModeEnabled();
        return new AuthConfigurationDto(enabledProviders, localStandaloneModeEnabled);
    }
}
