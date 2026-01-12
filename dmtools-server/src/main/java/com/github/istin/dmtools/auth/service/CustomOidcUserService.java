package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);
    
    @Autowired
    private UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        logger.info("🔍 OIDC User Service - Processing user from provider: {}", registrationId);
        logger.debug("🔍 OIDC User Service - User attributes: {}", oidcUser.getAttributes());

        try {
            // Extract user information based on provider
            String email = extractEmail(oidcUser, registrationId);
            String name = extractName(oidcUser, registrationId);
            String givenName = extractGivenName(oidcUser, registrationId);
            String familyName = extractFamilyName(oidcUser, registrationId);
            String pictureUrl = extractPictureUrl(oidcUser, registrationId);
            String locale = extractLocale(oidcUser, registrationId);
            String providerId = extractProviderId(oidcUser, registrationId);
            
            logger.info("✅ OIDC User Service - Extracted user data: email={}, name={}, picture={}", 
                       email, name, pictureUrl);
            
            // Create or update user in database
            AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
            User user = userService.createOrUpdateUser(
                email, name, givenName, familyName, pictureUrl, locale, authProvider, providerId
            );
            
            logger.info("✅ OIDC User Service - User created/updated in database: {}", user.getId());
            
            return oidcUser;
        } catch (Exception e) {
            logger.error("❌ OIDC User Service - Error processing user: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Failed to process OIDC user");
        }
    }
    
    private String extractEmail(OidcUser user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("email");
            case "microsoft":
                // Microsoft can provide email in different fields
                String email = (String) attributes.get("email");
                if (email == null) {
                    email = (String) attributes.get("mail");
                }
                if (email == null) {
                    email = (String) attributes.get("preferred_username");
                }
                return email;
            case "apple":
                return (String) attributes.get("email");
            default:
                return (String) attributes.get("email");
        }
    }
    
    private String extractName(OidcUser user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("name");
            case "microsoft":
                // Microsoft can provide name in different fields
                String name = (String) attributes.get("name");
                if (name == null) {
                    name = (String) attributes.get("displayName");
                }
                return name;
            case "apple":
                return (String) attributes.get("name"); // Apple may provide name on first login only
            default:
                return (String) attributes.get("name");
        }
    }
    
    private String extractGivenName(OidcUser user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("given_name");
            case "microsoft":
                // Microsoft can provide given name in different fields
                String givenName = (String) attributes.get("given_name");
                if (givenName == null) {
                    givenName = (String) attributes.get("givenName");
                }
                return givenName;
            case "apple":
                return (String) attributes.get("given_name"); // Apple may provide on first login only
            default:
                return (String) attributes.get("given_name");
        }
    }
    
    private String extractFamilyName(OidcUser user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("family_name");
            case "microsoft":
                // Microsoft can provide family name in different fields
                String familyName = (String) attributes.get("family_name");
                if (familyName == null) {
                    familyName = (String) attributes.get("surname");
                }
                return familyName;
            case "apple":
                return (String) attributes.get("family_name"); // Apple may provide on first login only
            default:
                return (String) attributes.get("family_name");
        }
    }
    
    private String extractPictureUrl(OidcUser user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("picture");
            case "microsoft":
                return (String) attributes.get("picture");
            case "apple":
                return null; // Apple does not provide profile pictures
            default:
                return (String) attributes.get("picture");
        }
    }
    
    private String extractLocale(OidcUser user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("locale");
            case "microsoft":
                return (String) attributes.get("preferredLanguage");
            case "apple":
                return null; // Apple does not provide locale information
            default:
                return (String) attributes.get("locale");
        }
    }
    
    private String extractProviderId(OidcUser user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "microsoft":
                // Microsoft uses 'oid' for user ID in OIDC tokens
                String providerId = (String) attributes.get("oid");
                if (providerId == null) {
                    providerId = (String) attributes.get("sub");
                }
                if (providerId == null) {
                    providerId = (String) attributes.get("id");
                }
                return providerId;
            case "apple":
                return (String) attributes.get("sub"); // Apple uses 'sub' as the stable user identifier
            default:
                return (String) attributes.get("id");
        }
    }
} 