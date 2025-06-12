package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createOrUpdateUser(String email, String name, String givenName, 
                                  String familyName, String pictureUrl, String locale, 
                                  AuthProvider provider, String providerId) {
        Optional<User> existingUser = findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user information
            user.setName(name);
            user.setGivenName(givenName);
            user.setFamilyName(familyName);
            user.setPictureUrl(pictureUrl);
            user.setLocale(locale);
            return userRepository.save(user);
        } else {
            // Create new user
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setEmail(email);
            newUser.setEmailVerified(true);
            newUser.setName(name);
            newUser.setGivenName(givenName);
            newUser.setFamilyName(familyName);
            newUser.setPictureUrl(pictureUrl);
            newUser.setLocale(locale);
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            return userRepository.save(newUser);
        }
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
} 