package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Configuration
public class OAuth2ClientConfig {

    /**
     * Empty implementation of ClientRegistrationRepository for standalone mode
     * when no OAuth2 registrations are needed
     */
    private static class EmptyClientRegistrationRepository implements ClientRegistrationRepository {
        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return null;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ClientConfig.class);

    private final AuthConfigProperties authConfigProperties;
    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final AppleSecretGenerator appleSecretGenerator;
    
    public OAuth2ClientConfig(AuthConfigProperties authConfigProperties,
                             @Autowired(required = false) OAuth2ClientProperties oAuth2ClientProperties,
                             AppleSecretGenerator appleSecretGenerator) {
        this.authConfigProperties = authConfigProperties;
        this.oAuth2ClientProperties = oAuth2ClientProperties;
        this.appleSecretGenerator = appleSecretGenerator;
    }

    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        if (authConfigProperties.isLocalStandaloneMode() || oAuth2ClientProperties == null) {
            logger.warn("⚠️ Local standalone mode enabled. OAuth2 ClientRegistrationRepository disabled.");
            return new EmptyClientRegistrationRepository();
        }

        // Create ClientRegistration objects from properties
        List<ClientRegistration> registrations = createClientRegistrations();

        if (registrations.isEmpty()) {
            logger.warn("⚠️ No OAuth2 client registrations found in properties. OAuth2 login disabled.");
            return new EmptyClientRegistrationRepository();
        }

        Set<String> enabledProviders = authConfigProperties.getEnabledProvidersAsSet();
        
        List<ClientRegistration> filteredRegistrations = registrations.stream()
                .filter(reg -> enabledProviders.isEmpty() || enabledProviders.contains(reg.getRegistrationId()))
                .collect(Collectors.toList());

        if (filteredRegistrations.isEmpty()) {
            logger.warn("⚠️ No enabled OAuth2 providers found based on 'auth.enabled-providers'. OAuth2 login disabled.");
            return new EmptyClientRegistrationRepository();
        } else {
            logger.info("🔐 Configured OAuth2 ClientRegistrationRepository with providers: {}",
                    filteredRegistrations.stream().map(ClientRegistration::getRegistrationId).collect(Collectors.joining(", ")));
            return new InMemoryClientRegistrationRepository(filteredRegistrations);
        }
    }

    private List<ClientRegistration> createClientRegistrations() {
        List<ClientRegistration> registrations = new ArrayList<>();
        
        if (oAuth2ClientProperties.getRegistration() == null || oAuth2ClientProperties.getRegistration().isEmpty()) {
            logger.debug("🔧 No OAuth2 client registrations found in properties");
            return registrations;
        }
        
        for (String registrationId : oAuth2ClientProperties.getRegistration().keySet()) {
            OAuth2ClientProperties.Registration registration = oAuth2ClientProperties.getRegistration().get(registrationId);
            OAuth2ClientProperties.Provider provider = oAuth2ClientProperties.getProvider() != null ? 
                    oAuth2ClientProperties.getProvider().get(registrationId) : null;
            
            if (registration == null || registration.getClientId() == null || "placeholder".equals(registration.getClientId())) {
                logger.debug("🔧 Skipping OAuth2 registration '{}' - client ID is placeholder or null", registrationId);
                continue;
            }
            
            try {
                // Handle Apple special case - don't set client secret in registration
                // Apple uses dynamic JWT client secret only during token exchange
                String clientSecret = registration.getClientSecret();
                ClientAuthenticationMethod authMethod = ClientAuthenticationMethod.CLIENT_SECRET_BASIC;

                if ("apple".equalsIgnoreCase(registrationId)) {
                    // For Apple, don't set client_secret in registration - it will be generated dynamically during token exchange
                    clientSecret = "";
                    authMethod = ClientAuthenticationMethod.NONE; // Apple doesn't use client auth in authorization request
                    logger.info("🍎 Apple provider configured without client secret (will be generated dynamically)");
                } else if (clientSecret == null) {
                    clientSecret = "";
                }

                ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(registrationId)
                        .clientId(registration.getClientId())
                        .clientSecret(clientSecret)
                        .clientAuthenticationMethod(authMethod)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri(registration.getRedirectUri() != null ? registration.getRedirectUri() : "http://localhost:8080/login/oauth2/code/" + registrationId);
                
                // Add scopes if they exist
                if (registration.getScope() != null && !registration.getScope().isEmpty()) {
                    builder.scope(registration.getScope());
                }

                // Configure provider-specific settings
                if (provider != null) {
                    builder.authorizationUri(provider.getAuthorizationUri())
                           .tokenUri(provider.getTokenUri())
                           .userInfoUri(provider.getUserInfoUri())
                           .userNameAttributeName(provider.getUserNameAttribute());
                    
                    if (provider.getJwkSetUri() != null) {
                        builder.jwkSetUri(provider.getJwkSetUri());
                    }
                } else {
                    // Use common provider defaults
                    configureCommonProvider(builder, registrationId);
                }

                registrations.add(builder.build());
                logger.info("✅ Created OAuth2 ClientRegistration for provider: {}", registrationId);
                
            } catch (Exception e) {
                logger.error("❌ Failed to create OAuth2 ClientRegistration for provider '{}': {}", registrationId, e.getMessage());
            }
        }
        
        return registrations;
    }

    private void configureCommonProvider(ClientRegistration.Builder builder, String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "google":
                builder.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                       .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                       .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                       .userNameAttributeName(IdTokenClaimNames.SUB)
                       .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs");
                break;
            case "github":
                builder.authorizationUri("https://github.com/login/oauth/authorize")
                       .tokenUri("https://github.com/login/oauth/access_token")
                       .userInfoUri("https://api.github.com/user")
                       .userNameAttributeName("id");
                break;
            case "microsoft":
                builder.authorizationUri("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                       .tokenUri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                       .userInfoUri("https://graph.microsoft.com/v1.0/me")
                       .userNameAttributeName("id")
                       .jwkSetUri("https://login.microsoftonline.com/common/discovery/v2.0/keys");
                break;
            case "apple":
                builder.authorizationUri("https://appleid.apple.com/auth/authorize")
                       .tokenUri("https://appleid.apple.com/auth/token")
                       .jwkSetUri("https://appleid.apple.com/auth/keys")
                       .userNameAttributeName(IdTokenClaimNames.SUB);
                break;
            default:
                logger.warn("⚠️ Unknown OAuth2 provider '{}' - using generic settings", registrationId);
        }
    }
}
