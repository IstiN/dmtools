package com.github.istin.dmtools.auth.config;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

    private final AuthProperties authProperties;
    private final InMemoryClientRegistrationRepository delegate;

    public DynamicClientRegistrationRepository(AuthProperties authProperties,
                                               @Value("${google.client.id:}") String googleClientId,
                                               @Value("${google.client.secret:}") String googleClientSecret,
                                               @Value("${microsoft.client.id:}") String microsoftClientId,
                                               @Value("${microsoft.client.secret:}") String microsoftClientSecret,
                                               @Value("${github.client.id:}") String githubClientId,
                                               @Value("${github.client.secret:}") String githubClientSecret) {
        this.authProperties = authProperties;
        this.delegate = new InMemoryClientRegistrationRepository(createClientRegistrations(
                googleClientId, googleClientSecret,
                microsoftClientId, microsoftClientSecret,
                githubClientId, githubClientSecret
        ));
    }

    private List<ClientRegistration> createClientRegistrations(
            String googleClientId, String googleClientSecret,
            String microsoftClientId, String microsoftClientSecret,
            String githubClientId, String githubClientSecret) {

        Map<String, ClientRegistration> registrations = new HashMap<>();

        if (authProperties.getEnabledProviders().contains("google") && !googleClientId.isEmpty() && !googleClientSecret.isEmpty()) {
            registrations.put("google", googleClientRegistration(googleClientId, googleClientSecret));
        }
        if (authProperties.getEnabledProviders().contains("microsoft") && !microsoftClientId.isEmpty() && !microsoftClientSecret.isEmpty()) {
            registrations.put("microsoft", microsoftClientRegistration(microsoftClientId, microsoftClientSecret));
        }
        if (authProperties.getEnabledProviders().contains("github") && !githubClientId.isEmpty() && !githubClientSecret.isEmpty()) {
            registrations.put("github", githubClientRegistration(githubClientId, githubClientSecret));
        }

        // If no specific providers are configured, enable all supported providers by default
        if (authProperties.getEnabledProviders().isEmpty()) {
            if (!googleClientId.isEmpty() && !googleClientSecret.isEmpty()) {
                registrations.put("google", googleClientRegistration(googleClientId, googleClientSecret));
            }
            if (!microsoftClientId.isEmpty() && !microsoftClientSecret.isEmpty()) {
                registrations.put("microsoft", microsoftClientRegistration(microsoftClientId, microsoftClientSecret));
            }
            if (!githubClientId.isEmpty() && !githubClientSecret.isEmpty()) {
                registrations.put("github", githubClientRegistration(githubClientId, githubClientSecret));
            }
        }

        return registrations.values().stream().collect(Collectors.toList());
    }

    private ClientRegistration googleClientRegistration(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
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
    }

    private ClientRegistration microsoftClientRegistration(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("microsoft")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
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

    private ClientRegistration githubClientRegistration(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("github")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("read:user", "user:email")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id") // GitHub uses 'id' as the user identifier
                .clientName("GitHub")
                .build();
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return delegate.findByRegistrationId(registrationId);
    }
}
