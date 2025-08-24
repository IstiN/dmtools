package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.AuthConfigProperties;
import com.github.istin.dmtools.auth.model.AuthConfigurationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthConfigurationController.class);

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/config")
    public ResponseEntity<AuthConfigurationResponse> getAuthConfiguration() {
        logger.info("Received request for authentication configuration.");

        boolean localStandaloneMode = authConfigProperties.isLocalStandaloneMode();
        List<String> enabledProviders = new ArrayList<>();

        if (!localStandaloneMode && clientRegistrationRepository != null) {
            Iterable<ClientRegistration> registrations = ((Iterable<ClientRegistration>) clientRegistrationRepository);
            for (ClientRegistration registration : registrations) {
                enabledProviders.add(registration.getRegistrationId());
            }
            logger.info("OAuth2 providers enabled: {}", enabledProviders);
        } else {
            logger.info("Local standalone mode is active or no OAuth2 providers configured.");
        }

        AuthConfigurationResponse response = new AuthConfigurationResponse(localStandaloneMode, enabledProviders);
        return ResponseEntity.ok(response);
    }
}
