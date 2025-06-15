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
} 