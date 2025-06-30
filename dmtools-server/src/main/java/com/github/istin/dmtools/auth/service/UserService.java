package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email);
    }

    @Transactional
    public synchronized User createOrUpdateUser(String email, String name, String givenName,
                                   String familyName, String pictureUrl, String locale,
                                   AuthProvider provider, String providerId) {
        logger.info("Attempting to create/update user with email: {} and providerId: {}", email, providerId);

        // Try finding by providerId first if it's not empty, as it's the primary key
        if (providerId != null && !providerId.isEmpty()) {
            Optional<User> existingUserById = userRepository.findById(providerId);
            if (existingUserById.isPresent()) {
                User user = existingUserById.get();
                logger.info("Found existing user by ID: {}. Updating details.", user.getId());
                user.setName(name);
                user.setGivenName(givenName);
                user.setFamilyName(familyName);
                user.setPictureUrl(pictureUrl);
                user.setLocale(locale);
                // Update email if provided and different
                if (email != null && !email.equals(user.getEmail())) {
                    user.setEmail(email);
                }
                return userRepository.save(user);
            }
        }

        // If not found by ID and email is provided, try by email
        if (email != null && !email.isEmpty()) {
            Optional<User> existingUserByEmail = findByEmail(email);
            if (existingUserByEmail.isPresent()) {
                User user = existingUserByEmail.get();
                logger.info("Found existing user by email: {}. Updating details.", user.getEmail());
                user.setName(name);
                user.setGivenName(givenName);
                user.setFamilyName(familyName);
                user.setPictureUrl(pictureUrl);
                user.setLocale(locale);
                // Update providerId if it was missing
                if (providerId != null && !providerId.equals(user.getProviderId())) {
                    user.setProviderId(providerId);
                }
                return userRepository.save(user);
            }
        }

        // If user doesn't exist by ID or email, create a new one
        logger.info("No existing user found. Creating a new user for email: {} (providerId: {})", email, providerId);
        User newUser = new User();
        
        // Use providerId for the entity ID if available, otherwise generate one
        String userId = providerId != null && !providerId.isEmpty() ? providerId : generateUserId(provider, email);
        newUser.setId(userId);
        newUser.setProviderId(providerId);
        
        // Handle email - if null, generate a placeholder or leave null
        if (email != null && !email.isEmpty()) {
            newUser.setEmail(email);
        } else {
            // For providers that don't provide email, we can either:
            // 1. Leave email as null (current approach)
            // 2. Generate a placeholder email
            logger.info("No email provided by OAuth2 provider. User will have null email.");
            newUser.setEmail(null);
        }
        
        newUser.setEmailVerified(email != null); // Only mark as verified if we have an email
        newUser.setName(name);
        newUser.setGivenName(givenName);
        newUser.setFamilyName(familyName);
        newUser.setPictureUrl(pictureUrl);
        newUser.setLocale(locale);
        newUser.setProvider(provider);
        
        return userRepository.save(newUser);
    }
    
    private String generateUserId(AuthProvider provider, String email) {
        if (email != null && !email.isEmpty()) {
            return email;
        }
        // Generate a unique ID based on provider and timestamp
        return provider.name().toLowerCase() + "_user_" + System.currentTimeMillis();
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Create or update user from OAuth2 authentication token
     */
    @Transactional
    public User createOrUpdateOAuth2User(org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauth2Token) {
        org.springframework.security.oauth2.core.user.OAuth2User oauth2User = oauth2Token.getPrincipal();
        String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
        
        // Extract user information based on provider
        String email = extractEmailFromOAuth2User(oauth2User, registrationId);
        String name = extractNameFromOAuth2User(oauth2User, registrationId);
        String givenName = extractGivenNameFromOAuth2User(oauth2User, registrationId);
        String familyName = extractFamilyNameFromOAuth2User(oauth2User, registrationId);
        String pictureUrl = extractPictureUrlFromOAuth2User(oauth2User, registrationId);
        String locale = extractLocaleFromOAuth2User(oauth2User, registrationId);
        String providerId = extractProviderIdFromOAuth2User(oauth2User, registrationId);
        
        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
        
        return createOrUpdateUser(email, name, givenName, familyName, pictureUrl, locale, authProvider, providerId);
    }
    
    private String extractEmailFromOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("email");
            case "microsoft":
                String email = user.getAttribute("mail");
                return email != null ? email : user.getAttribute("userPrincipalName");
            case "github":
                return user.getAttribute("email");
            default:
                return user.getAttribute("email");
        }
    }
    
    private String extractNameFromOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("name");
            case "microsoft":
                return user.getAttribute("displayName");
            case "github":
                return user.getAttribute("name");
            default:
                return user.getAttribute("name");
        }
    }
    
    private String extractGivenNameFromOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("given_name");
            case "microsoft":
                return user.getAttribute("givenName");
            case "github":
                return null; // GitHub doesn't provide given_name
            default:
                return user.getAttribute("given_name");
        }
    }
    
    private String extractFamilyNameFromOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("family_name");
            case "microsoft":
                return user.getAttribute("surname");
            case "github":
                return null; // GitHub doesn't provide family_name
            default:
                return user.getAttribute("family_name");
        }
    }
    
    private String extractPictureUrlFromOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("picture");
            case "microsoft":
                return null; // Microsoft Graph API has different endpoint for photo
            case "github":
                return user.getAttribute("avatar_url");
            default:
                return user.getAttribute("picture");
        }
    }
    
    private String extractLocaleFromOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("locale");
            case "microsoft":
                return user.getAttribute("preferredLanguage");
            case "github":
                return null; // GitHub doesn't provide locale
            default:
                return user.getAttribute("locale");
        }
    }
    
    private String extractProviderIdFromOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("sub");
            case "microsoft":
                return user.getAttribute("id");
            case "github":
                Object id = user.getAttribute("id");
                return id != null ? id.toString() : null;
            default:
                return user.getAttribute("id");
        }
    }
} 