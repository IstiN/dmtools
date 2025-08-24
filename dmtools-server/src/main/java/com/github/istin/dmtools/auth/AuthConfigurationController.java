package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.AuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthConfigurationController.class);

    private final AuthConfigProperties authConfigProperties;
    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

    public AuthConfigurationController(AuthConfigProperties authConfigProperties, Optional<ClientRegistrationRepository> clientRegistrationRepository) {
        this.authConfigProperties = authConfigProperties;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @GetMapping("/config")
    public AuthConfiguration getAuthConfiguration() {
        List<String> enabledProviders = new ArrayList<>();
        boolean localStandaloneMode = authConfigProperties.isLocalStandaloneModeEnabled();

        if (!localStandaloneMode && clientRegistrationRepository.isPresent()) {
            clientRegistrationRepository.get().forEach(registration -> {
                if (authConfigProperties.getEnabledProviders().contains(registration.getRegistrationId())) {
                    enabledProviders.add(registration.getRegistrationId());
                }
            });
        } else if (!localStandaloneMode && !clientRegistrationRepository.isPresent()) {
            logger.warn("ClientRegistrationRepository is not available, but local standalone mode is not enabled. This might indicate a misconfiguration.");
        }

        logger.info("Auth configuration requested: localStandaloneMode={}, enabledProviders={}", localStandaloneMode, enabledProviders);
        return new AuthConfiguration(localStandaloneMode, enabledProviders);
    }
}
