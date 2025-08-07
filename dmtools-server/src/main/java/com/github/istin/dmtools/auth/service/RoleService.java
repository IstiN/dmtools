package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service for role assignment logic and cache management
 */
@Service
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
    
    private final UserService userService;
    private final Map<String, String> roleCache = new ConcurrentHashMap<>();

    public RoleService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get user role with caching
     */
    public String getUserRole(String userId) {
        // Check cache first
        String cachedRole = roleCache.get(userId);
        if (cachedRole != null) {
            logger.debug("Role cache hit for user {}: {}", userId, cachedRole);
            return cachedRole;
        }

        // Fallback to database
        logger.debug("Role cache miss for user {}, checking database", userId);
        return getUserRoleFromDatabase(userId);
    }

    /**
     * Get user role from database and cache it
     */
    private String getUserRoleFromDatabase(String userId) {
        try {
            User user = userService.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User not found in database: {}", userId);
                return "REGULAR_USER";
            }

            String role = userService.getUserRole(user);
            
            // Cache the role
            roleCache.put(userId, role);
            logger.debug("Cached role for user {}: {}", userId, role);
            
            return role;
        } catch (Exception e) {
            logger.error("Error retrieving user role from database for user {}: {}", userId, e.getMessage());
            return "REGULAR_USER";
        }
    }

    /**
     * Update user role and invalidate cache
     */
    public User updateUserRole(String userId, String role) {
        logger.info("Updating role for user {} to {}", userId, role);
        
        User updatedUser = userService.updateUserRole(userId, role);
        
        // Invalidate cache immediately
        invalidateUserCache(userId);
        
        logger.info("Role updated and cache invalidated for user {}", userId);
        return updatedUser;
    }

    /**
     * Invalidate cache entry for specific user
     */
    public void invalidateUserCache(String userId) {
        String removedRole = roleCache.remove(userId);
        if (removedRole != null) {
            logger.info("Cache invalidated for user {}, removed role: {}", userId, removedRole);
        } else {
            logger.debug("No cache entry found for user {} to invalidate", userId);
        }
    }

    /**
     * Clear entire role cache
     */
    public void clearCache() {
        int size = roleCache.size();
        roleCache.clear();
        logger.info("Cleared entire role cache, {} entries removed", size);
    }

    /**
     * Check if user has admin role (cached)
     */
    public boolean hasAdminRole(String userId) {
        String role = getUserRole(userId);
        return "ADMIN".equals(role);
    }

    /**
     * Check if user has admin role (from User object)
     */
    public boolean hasAdminRole(User user) {
        return userService.hasAdminRole(user);
    }

    /**
     * Get cache statistics for monitoring
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheSize", roleCache.size());
        stats.put("cachedUserIds", roleCache.keySet());
        return stats;
    }

    /**
     * Pre-warm cache for user
     */
    public void warmUpCache(String userId) {
        logger.debug("Warming up cache for user {}", userId);
        getUserRole(userId);
    }
}
