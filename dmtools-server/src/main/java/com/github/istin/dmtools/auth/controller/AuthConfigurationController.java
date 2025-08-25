package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/auth")
public class AuthConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthConfigurationController.class);

    private final AuthConfigProperties authConfigProperties;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    public AuthConfigurationController(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAuthConfiguration() {
        Map<String, Object> config = new HashMap<>();

        if (authConfigProperties.isLocalStandaloneMode()) {
            config.put("authenticationMode", "standalone");
            config.put("enabledProviders", List.of());
            logger.info("AuthConfigurationController: Local standalone mode enabled.");
        } else {
            config.put("authenticationMode", "oauth2");
            List<String> enabledProviders = List.of();
            if (clientRegistrationRepository != null) {
                enabledProviders = StreamSupport.stream(((Iterable<ClientRegistration>) clientRegistrationRepository).spliterator(), false)
                        .map(ClientRegistration::getRegistrationId)
                        .collect(Collectors.toList());
            }
            config.put("enabledProviders", enabledProviders);
            logger.info("AuthConfigurationController: OAuth2 mode enabled with providers: {}", enabledProviders);
        }
        return ResponseEntity.ok(config);
    }
}
