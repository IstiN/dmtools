package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnBean(ClientRegistrationRepository.class)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    
    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        logger.info("🔍 OAuth2 User Service - Processing user from provider: {}", registrationId);
        logger.debug("🔍 OAuth2 User Service - User attributes: {}", oauth2User.getAttributes());

        try {
            // Extract user information based on provider
            String email = extractEmail(oauth2User, registrationId, userRequest);
            String name = extractName(oauth2User, registrationId);
            String givenName = extractGivenName(oauth2User, registrationId);
            String familyName = extractFamilyName(oauth2User, registrationId);
            String pictureUrl = extractPictureUrl(oauth2User, registrationId);
            String locale = extractLocale(oauth2User, registrationId);
            String providerId = extractProviderId(oauth2User, registrationId);
            
            logger.info("✅ OAuth2 User Service - Extracted user data: email={}, name={}, picture={}", 
                       email, name, pictureUrl);
            
            // Create or update user in database
            AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
            User user = userService.createOrUpdateUser(
                email, name, givenName, familyName, pictureUrl, locale, authProvider, providerId
            );
            
            logger.info("✅ OAuth2 User Service - User created/updated in database: {}", user.getId());
            
            return oauth2User;
        } catch (Exception e) {
            logger.error("❌ OAuth2 User Service - Error processing user: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Failed to process OAuth2 user");
        }
    }
    
    private String extractEmail(OAuth2User user, String provider, OAuth2UserRequest userRequest) {
        Map<String, Object> attributes = user.getAttributes();
        String email = null;
        
        switch (provider.toLowerCase()) {
            case "google":
                email = (String) attributes.get("email");
                break;
            case "github":
                email = (String) attributes.get("email");
                // If email is null, try to fetch it from GitHub's email API
                if (email == null) {
                    email = fetchGitHubUserEmail(userRequest.getAccessToken().getTokenValue());
                }
                break;
            case "microsoft":
                email = (String) attributes.get("mail");
                break;
            default:
                email = (String) attributes.get("email");
        }
        
        return email;
    }
    
    private String fetchGitHubUserEmail(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(
                "https://api.github.com/user/emails", 
                HttpMethod.GET, 
                entity, 
                List.class
            );
            
            if (response.getBody() != null) {
                for (Object emailObj : response.getBody()) {
                    if (emailObj instanceof Map) {
                        Map<String, Object> emailData = (Map<String, Object>) emailObj;
                        Boolean primary = (Boolean) emailData.get("primary");
                        Boolean verified = (Boolean) emailData.get("verified");
                        
                        if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                            String email = (String) emailData.get("email");
                            logger.info("✅ GitHub Email API - Found primary verified email: {}", email);
                            return email;
                        }
                    }
                }
                
                // If no primary verified email, try to get the first verified email
                for (Object emailObj : response.getBody()) {
                    if (emailObj instanceof Map) {
                        Map<String, Object> emailData = (Map<String, Object>) emailObj;
                        Boolean verified = (Boolean) emailData.get("verified");
                        
                        if (Boolean.TRUE.equals(verified)) {
                            String email = (String) emailData.get("email");
                            logger.info("✅ GitHub Email API - Found verified email: {}", email);
                            return email;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("⚠️ GitHub Email API - Failed to fetch user email: {}", e.getMessage());
        }
        
        return null;
    }
    
    private String extractName(OAuth2User user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("name");
            case "github":
                return (String) attributes.get("name");
            case "microsoft":
                return (String) attributes.get("displayName");
            default:
                return (String) attributes.get("name");
        }
    }
    
    private String extractGivenName(OAuth2User user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("given_name");
            case "github":
                return null; // GitHub doesn't provide separate given/family names
            case "microsoft":
                return (String) attributes.get("givenName");
            default:
                return (String) attributes.get("given_name");
        }
    }
    
    private String extractFamilyName(OAuth2User user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("family_name");
            case "github":
                return null; // GitHub doesn't provide separate given/family names
            case "microsoft":
                return (String) attributes.get("surname");
            default:
                return (String) attributes.get("family_name");
        }
    }
    
    private String extractPictureUrl(OAuth2User user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("picture");
            case "github":
                return (String) attributes.get("avatar_url");
            case "microsoft":
                return (String) attributes.get("picture");
            default:
                return (String) attributes.get("picture");
        }
    }
    
    private String extractLocale(OAuth2User user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("locale");
            case "github":
                return null; // GitHub doesn't provide locale
            case "microsoft":
                return (String) attributes.get("preferredLanguage");
            default:
                return (String) attributes.get("locale");
        }
    }
    
    private String extractProviderId(OAuth2User user, String provider) {
        Map<String, Object> attributes = user.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "github":
                Object id = attributes.get("id");
                return id != null ? id.toString() : null;
            case "microsoft":
                return (String) attributes.get("id");
            default:
                return (String) attributes.get("id");
        }
    }
} 