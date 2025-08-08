package com.github.istin.dmtools.auth.config;

import com.github.istin.dmtools.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Startup validator for admin email configuration
 */
@Component
public class AdminEmailValidator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminEmailValidator.class);
    
    @Value("${admin.emails:}")
    private String adminEmailsList;
    
    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        validateAdminEmailConfiguration();
        initializeUserRoles();
    }

    /**
     * Validate admin email configuration during startup
     */
    private void validateAdminEmailConfiguration() {
        logger.info("🔍 STARTUP - Validating admin email configuration...");
        
        if (adminEmailsList == null || adminEmailsList.trim().isEmpty()) {
            logger.warn("⚠️ STARTUP - No admin emails configured in application.properties");
            logger.warn("⚠️ STARTUP - Set 'admin.emails' property as comma-separated list of admin email addresses");
            logger.warn("⚠️ STARTUP - Example: admin.emails=admin@company.com,owner@company.com");
            return;
        }
        
        Set<String> adminEmails = Arrays.stream(adminEmailsList.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toSet());
                
        if (adminEmails.isEmpty()) {
            logger.warn("⚠️ STARTUP - Admin emails configured but all entries are empty");
            return;
        }
        
        logger.info("✅ STARTUP - Admin email configuration validated successfully");
        logger.info("✅ STARTUP - Configured admin emails: {}", adminEmails.size());
        
        // Log admin emails for verification (masked for security)
        for (String email : adminEmails) {
            if (isValidEmail(email)) {
                String maskedEmail = maskEmail(email);
                logger.info("✅ STARTUP - Admin email configured: {}", maskedEmail);
            } else {
                logger.warn("⚠️ STARTUP - Invalid email format in admin configuration: {}", email);
            }
        }
        
        // Initialize admin emails in UserService
        userService.initializeAdminEmails();
        
        // Re-evaluate all user roles based on current admin email configuration
        initializeUserRoles();
    }
    
    /**
     * Initialize roles for existing users and re-evaluate admin assignments
     */
    private void initializeUserRoles() {
        logger.info("🔍 STARTUP - Initializing and re-evaluating user roles...");
        try {
            // First ensure users without roles get default roles
            userService.ensureUserRoles();
            
            // Then re-evaluate all users to update admin assignments based on current config
            userService.updateAllUserRoles();
            
            logger.info("✅ STARTUP - User roles initialized and re-evaluated successfully");
        } catch (Exception e) {
            logger.error("❌ STARTUP - Error initializing user roles: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") 
               && email.length() > 5 && !email.startsWith("@") && !email.endsWith("@");
    }
    
    /**
     * Mask email for logging (security)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***";
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        // Mask local part
        String maskedLocal;
        if (localPart.length() <= 2) {
            maskedLocal = "*".repeat(localPart.length());
        } else {
            maskedLocal = localPart.charAt(0) + "*".repeat(localPart.length() - 2) + localPart.charAt(localPart.length() - 1);
        }
        
        return maskedLocal + "@" + domainPart;
    }
}
