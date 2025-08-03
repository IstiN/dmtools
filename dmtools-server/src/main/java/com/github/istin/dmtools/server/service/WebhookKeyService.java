package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.dto.CreateWebhookKeyRequest;
import com.github.istin.dmtools.dto.CreateWebhookKeyResponse;
import com.github.istin.dmtools.dto.WebhookKeyDto;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.WebhookKey;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
import com.github.istin.dmtools.server.repository.WebhookKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing webhook API keys.
 * Handles key generation, validation, and lifecycle management.
 */
@Service
@Slf4j
public class WebhookKeyService {
    
    private static final String KEY_PREFIX = "wk_";
    private static final int KEY_LENGTH = 32; // Length of the random part after prefix
    private static final SecureRandom secureRandom = new SecureRandom();
    
    @Autowired
    private WebhookKeyRepository webhookKeyRepository;
    
    @Autowired
    private JobConfigurationRepository jobConfigurationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new webhook API key for a job configuration.
     * 
     * @param jobConfigId The job configuration ID
     * @param request The create webhook key request
     * @param userId The user ID creating the key
     * @return Create webhook key response with the actual key
     */
    @Transactional
    public Optional<CreateWebhookKeyResponse> createWebhookKey(String jobConfigId, 
                                                              CreateWebhookKeyRequest request, 
                                                              String userId) {
        try {
            // Find user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Find job configuration and verify ownership
            Optional<JobConfiguration> optionalJobConfig = jobConfigurationRepository
                    .findByIdAndCreatedBy(jobConfigId, user);
            if (optionalJobConfig.isEmpty()) {
                log.warn("Job configuration not found or not accessible: {} for user: {}", jobConfigId, userId);
                return Optional.empty();
            }
            
            JobConfiguration jobConfig = optionalJobConfig.get();
            
            // Check for duplicate key name within job configuration
            if (webhookKeyRepository.existsByJobConfigurationAndNameIgnoreCase(jobConfig, request.getName())) {
                throw new IllegalArgumentException("Webhook key with name '" + request.getName() + "' already exists for this job configuration");
            }
            
            // Generate API key
            String apiKey = generateApiKey();
            String keyHash = hashApiKey(apiKey);
            String keyPrefix = apiKey.substring(0, Math.min(10, apiKey.length())); // First 10 chars for display
            
            // Create webhook key entity
            WebhookKey webhookKey = new WebhookKey();
            webhookKey.setName(request.getName());
            webhookKey.setDescription(request.getDescription());
            webhookKey.setJobConfiguration(jobConfig);
            webhookKey.setCreatedBy(user);
            webhookKey.setKeyHash(keyHash);
            webhookKey.setKeyPrefix(keyPrefix);
            webhookKey.setEnabled(true);
            
            // Save webhook key
            webhookKey = webhookKeyRepository.save(webhookKey);
            
            log.info("Created webhook key: {} for job configuration: {} by user: {}", 
                    webhookKey.getId(), jobConfigId, userId);
            
            return Optional.of(CreateWebhookKeyResponse.create(
                    webhookKey.getId(),
                    apiKey,
                    webhookKey.getName(),
                    webhookKey.getDescription(),
                    jobConfigId,
                    webhookKey.getCreatedAt()
            ));
            
        } catch (Exception e) {
            log.error("Error creating webhook key for job configuration: {} by user: {}", jobConfigId, userId, e);
            throw new IllegalArgumentException("Failed to create webhook key: " + e.getMessage());
        }
    }
    
    /**
     * Get all webhook keys for a job configuration.
     * 
     * @param jobConfigId The job configuration ID
     * @param userId The user ID requesting the keys
     * @return List of webhook key DTOs
     */
    public List<WebhookKeyDto> getWebhookKeys(String jobConfigId, String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            List<WebhookKey> webhookKeys = webhookKeyRepository
                    .findByJobConfigurationIdAndCreatedBy(jobConfigId, user);
            
            return webhookKeys.stream()
                    .map(WebhookKeyDto::fromEntity)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error retrieving webhook keys for job configuration: {} by user: {}", jobConfigId, userId, e);
            throw new IllegalArgumentException("Failed to retrieve webhook keys: " + e.getMessage());
        }
    }
    
    /**
     * Delete a webhook key.
     * 
     * @param jobConfigId The job configuration ID
     * @param keyId The webhook key ID
     * @param userId The user ID requesting the deletion
     * @return True if deleted successfully, false if not found
     */
    @Transactional
    public boolean deleteWebhookKey(String jobConfigId, String keyId, String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Find job configuration and verify ownership
            Optional<JobConfiguration> optionalJobConfig = jobConfigurationRepository
                    .findByIdAndCreatedBy(jobConfigId, user);
            if (optionalJobConfig.isEmpty()) {
                return false;
            }
            
            JobConfiguration jobConfig = optionalJobConfig.get();
            
            // Find webhook key and verify ownership
            Optional<WebhookKey> optionalWebhookKey = webhookKeyRepository
                    .findByIdAndJobConfiguration(keyId, jobConfig);
            if (optionalWebhookKey.isEmpty()) {
                return false;
            }
            
            WebhookKey webhookKey = optionalWebhookKey.get();
            
            // Verify the key belongs to the requesting user
            if (!webhookKey.getCreatedBy().getId().equals(userId)) {
                log.warn("User {} attempted to delete webhook key {} not owned by them", userId, keyId);
                return false;
            }
            
            webhookKeyRepository.delete(webhookKey);
            
            log.info("Deleted webhook key: {} for job configuration: {} by user: {}", keyId, jobConfigId, userId);
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting webhook key: {} for job configuration: {} by user: {}", keyId, jobConfigId, userId, e);
            return false;
        }
    }
    
    /**
     * Validate API key and return associated job configuration and user.
     * 
     * @param apiKey The API key to validate
     * @param jobConfigId The job configuration ID
     * @return Optional containing the webhook key if valid
     */
    public Optional<WebhookKey> validateApiKeyForJobConfig(String apiKey, String jobConfigId) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return Optional.empty();
            }
            
            String keyHash = hashApiKey(apiKey.trim());
            Optional<WebhookKey> optionalWebhookKey = webhookKeyRepository
                    .findByKeyHashAndJobConfigurationId(keyHash, jobConfigId);
            
            if (optionalWebhookKey.isPresent()) {
                WebhookKey webhookKey = optionalWebhookKey.get();
                if (webhookKey.isActive()) {
                    // Record usage
                    webhookKey.recordUsage();
                    webhookKeyRepository.save(webhookKey);
                    
                    log.debug("Valid API key used for job configuration: {}", jobConfigId);
                    return Optional.of(webhookKey);
                } else {
                    log.warn("Disabled API key attempted for job configuration: {}", jobConfigId);
                }
            } else {
                log.warn("Invalid API key attempted for job configuration: {}", jobConfigId);
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error validating API key for job configuration: {}", jobConfigId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Generate a secure random API key.
     * 
     * @return Generated API key
     */
    private String generateApiKey() {
        StringBuilder sb = new StringBuilder(KEY_PREFIX);
        
        // Generate random alphanumeric string
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Hash API key using SHA-256.
     * 
     * @param apiKey The API key to hash
     * @return SHA-256 hash of the API key
     */
    private String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}