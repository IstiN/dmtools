package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnExpression("!'${auth.enabled-providers:}'.trim().isEmpty() && '${auth.enabled-providers:}' != ''")
public class OAuth2ClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ClientConfig.class);
    
    private final AuthConfigProperties authConfigProperties;
    
    public OAuth2ClientConfig(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    @Bean  
    public ClientRegistrationRepository clientRegistrationRepository(@Autowired(required = false) List<ClientRegistration> clientRegistrations) {
        if (authConfigProperties.isLocalStandaloneMode()) {
            logger.warn("⚠️ Local standalone mode enabled. OAuth2 ClientRegistrationRepository disabled.");
            return new InMemoryClientRegistrationRepository(Collections.emptyList());  // Return empty repository
        }

        if (clientRegistrations == null || clientRegistrations.isEmpty()) {
            logger.warn("⚠️ No ClientRegistration beans found. OAuth2 login disabled.");
            return new InMemoryClientRegistrationRepository(Collections.emptyList());  // Return empty repository
        }

        Set<String> enabledProviders = authConfigProperties.getEnabledProvidersAsSet();
        
        List<ClientRegistration> filteredRegistrations = clientRegistrations.stream()
                .filter(reg -> enabledProviders.isEmpty() || enabledProviders.contains(reg.getRegistrationId()))
                .collect(Collectors.toList());

        if (filteredRegistrations.isEmpty()) {
            logger.warn("⚠️ No enabled OAuth2 providers found based on 'auth.enabled-providers'. OAuth2 login disabled.");
            return new InMemoryClientRegistrationRepository(Collections.emptyList());  // Return empty repository
        } else {
            logger.info("🔐 Configured OAuth2 ClientRegistrationRepository with providers: {}",
                    filteredRegistrations.stream().map(ClientRegistration::getRegistrationId).collect(Collectors.joining(", ")));
        }
        return new InMemoryClientRegistrationRepository(filteredRegistrations);
    }

    // Note: ClientRegistration beans are automatically created by Spring Boot OAuth2 auto-configuration
    // from application.properties spring.security.oauth2.client.registration.* properties
    // We only need to filter them based on enabled providers in the clientRegistrationRepository() method above
}
