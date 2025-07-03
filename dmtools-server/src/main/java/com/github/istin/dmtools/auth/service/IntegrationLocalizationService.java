package com.github.istin.dmtools.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Service for handling localization of integration configurations.
 */
@Service
public class IntegrationLocalizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationLocalizationService.class);
    private static final String I18N_LOCATION = "classpath:i18n/integrations_*.properties";
    
    private final Map<String, Properties> localeProperties = new HashMap<>();
    private final String defaultLocale = "en";
    
    @PostConstruct
    public void loadTranslations() {
        try {
            loadTranslationFiles();
            logger.info("Successfully loaded translations for {} locales", localeProperties.size());
        } catch (IOException e) {
            logger.error("Failed to load integration translations", e);
            throw new IllegalStateException("Failed to load integration translations", e);
        }
    }
    
    private void loadTranslationFiles() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(I18N_LOCATION);
        
        logger.info("Found {} translation files", resources.length);
        
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) {
                logger.warn("Skipping resource with null filename: {}", resource);
                continue;
            }
            
            // Extract locale from filename (e.g., integrations_en.properties -> en)
            String locale = extractLocaleFromFilename(filename);
            if (locale == null) {
                logger.warn("Could not extract locale from filename: {}", filename);
                continue;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                Properties properties = new Properties();
                properties.load(inputStream);
                
                localeProperties.put(locale, properties);
                logger.debug("Loaded {} translation keys for locale: {}", properties.size(), locale);
            } catch (Exception e) {
                logger.error("Failed to load translation file: {}", filename, e);
                throw new IOException("Failed to load translation file: " + filename, e);
            }
        }
        
        // Ensure default locale is available
        if (!localeProperties.containsKey(defaultLocale)) {
            logger.warn("Default locale '{}' not found in translations", defaultLocale);
        }
    }
    
    private String extractLocaleFromFilename(String filename) {
        // Expected format: integrations_<locale>.properties
        if (!filename.startsWith("integrations_") || !filename.endsWith(".properties")) {
            return null;
        }
        
        int start = "integrations_".length();
        int end = filename.indexOf(".properties");
        
        if (start >= end) {
            return null;
        }
        
        return filename.substring(start, end);
    }
    
    /**
     * Get a localized message for the given key and locale.
     *
     * @param key The translation key
     * @param locale The locale (e.g., "en", "ru")
     * @return The localized message, or the key itself if not found
     */
    public String getMessage(String key, String locale) {
        if (key == null) {
            return null;
        }
        
        // Try requested locale first
        Properties properties = localeProperties.get(locale);
        if (properties != null) {
            String message = properties.getProperty(key);
            if (message != null) {
                return message;
            }
        }
        
        // Fallback to default locale
        if (!defaultLocale.equals(locale)) {
            properties = localeProperties.get(defaultLocale);
            if (properties != null) {
                String message = properties.getProperty(key);
                if (message != null) {
                    logger.debug("Using fallback locale '{}' for key '{}'", defaultLocale, key);
                    return message;
                }
            }
        }
        
        // Return key as fallback
        logger.warn("Translation not found for key '{}' in locale '{}' or default locale '{}'", 
                    key, locale, defaultLocale);
        return key;
    }
    
    /**
     * Get a localized message for the given key using the default locale.
     *
     * @param key The translation key
     * @return The localized message, or the key itself if not found
     */
    public String getMessage(String key) {
        return getMessage(key, defaultLocale);
    }
    
    /**
     * Get all available locales.
     *
     * @return Set of available locale codes
     */
    public java.util.Set<String> getAvailableLocales() {
        return localeProperties.keySet();
    }
    
    /**
     * Check if a locale is supported.
     *
     * @param locale The locale to check
     * @return true if the locale is supported, false otherwise
     */
    public boolean isLocaleSupported(String locale) {
        return localeProperties.containsKey(locale);
    }
    
    /**
     * Get the default locale.
     *
     * @return The default locale code
     */
    public String getDefaultLocale() {
        return defaultLocale;
    }
} 