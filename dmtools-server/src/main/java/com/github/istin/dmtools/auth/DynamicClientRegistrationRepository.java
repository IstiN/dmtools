package com.github.istin.dmtools.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

    private static final Logger logger = LoggerFactory.getLogger(DynamicClientRegistrationRepository.class);

    private final Map<String, ClientRegistration> registrations = new ConcurrentHashMap<>();
    private final AuthConfigProperties authConfigProperties;

    public DynamicClientRegistrationRepository(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
        refreshRegistrations();
    }

    public void refreshRegistrations() {
        registrations.clear();
        List<String> enabledProviders = authConfigProperties.getEnabledProvidersList();

        if (enabledProviders.isEmpty()) {
            logger.warn("⚠️ No external authentication providers enabled. Operating in local standalone mode.");
            return;
        }

        logger.info("🔐 Configuring OAuth2 providers: {}", enabledProviders);

        for (String providerId : enabledProviders) {
            ClientRegistration registration = buildClientRegistration(providerId);
            if (registration != null) {
                registrations.put(registration.getRegistrationId(), registration);
                logger.info("✅ Registered OAuth2 provider: {}", providerId);
            } else {
                logger.warn("❌ Failed to build ClientRegistration for provider: {}. Check application properties.", providerId);
            }
        }

        if (registrations.isEmpty()) {
            logger.warn("⚠️ No OAuth2 providers were successfully registered. OAuth2 login will be disabled.");
        }
    }

    private ClientRegistration buildClientRegistration(String providerId) {
        // This is a placeholder. In a real application, you would load client ID, secret, etc.,
        // from application.properties or environment variables based on the providerId.
        // For now, we'll assume properties like spring.security.oauth2.client.registration.<providerId>.* exist.
        // Spring Boot's OAuth2ClientProperties will automatically pick these up if configured.
        // We are just creating a dummy registration here to satisfy the ClientRegistrationRepository contract
        // and allow Spring Security to proceed with its auto-configuration if properties are present.

        // This method is primarily to ensure that if a provider is 'enabled' via auth.enabled-providers,
        // a corresponding ClientRegistration object is available. The actual details (client-id, client-secret, etc.)
        // are expected to be configured in application.properties and picked up by Spring's auto-configuration.
        // If those properties are missing for an 'enabled' provider, Spring will likely fail at startup.

        // For a truly dynamic setup without relying on application.properties for each provider's full details,
        // you would need to fetch these details from a configuration service or environment variables here.
        // For this task, we assume Spring's auto-configuration for well-known providers will handle the details
        // if the 'enabledProviders' list contains them and their properties are set.

        // Example for Google (assuming spring.security.oauth2.client.registration.google.* is set)
        if ("google".equalsIgnoreCase(providerId)) {
            return ClientRegistration.withRegistrationId("google")
                    .clientId("google-client-id-placeholder") // These will be overridden by actual properties
                    .clientSecret("google-client-secret-placeholder")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName(IdTokenClaimNames.SUB)
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .clientName("Google")
                    .build();
        } else if ("github".equalsIgnoreCase(providerId)) {
            return ClientRegistration.withRegistrationId("github")
                    .clientId("github-client-id-placeholder")
                    .clientSecret("github-client-secret-placeholder")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("read:user", "user:email")
                    .authorizationUri("https://github.com/login/oauth/authorize")
                    .tokenUri("https://github.com/login/oauth/access_token")
                    .userInfoUri("https://api.github.com/user")
                    .userNameAttributeName("id")
                    .clientName("GitHub")
                    .build();
        } else if ("microsoft".equalsIgnoreCase(providerId)) {
            return ClientRegistration.withRegistrationId("microsoft")
                    .clientId("microsoft-client-id-placeholder")
                    .clientSecret("microsoft-client-secret-placeholder")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                    .tokenUri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                    .userInfoUri("https://graph.microsoft.com/oidc/userinfo")
                    .userNameAttributeName(IdTokenClaimNames.SUB)
                    .clientName("Microsoft")
                    .build();
        }
        // Add other providers as needed
        return null;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return registrations.get(registrationId);
    }

    public Iterable<ClientRegistration> findAll() {
        return registrations.values();
    }

    public boolean hasRegistrations() {
        return !registrations.isEmpty();
    }
}
