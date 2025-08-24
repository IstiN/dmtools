package com.github.istin.dmtools.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthConfigurationController.class);

    private final AuthConfigProperties authConfigProperties;

    public AuthConfigurationController(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    @GetMapping("/config")
    public Map<String, Object> getAuthConfig() {
        logger.info("🔍 AuthConfigController - getAuthConfig called.");
        Map<String, Object> config = new HashMap<>();
        config.put("enabledProviders", authConfigProperties.getEnabledProvidersList());
        config.put("localStandaloneMode", authConfigProperties.isLocalStandaloneMode());
        logger.info("✅ AuthConfigController - Returning auth config: {}", config);
        return config;
    }
}
