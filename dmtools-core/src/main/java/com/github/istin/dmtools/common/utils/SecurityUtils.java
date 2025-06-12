package com.github.istin.dmtools.common.utils;

import org.json.JSONObject;

/**
 * Utility class for security-related operations, particularly for protecting sensitive information in logs
 */
public class SecurityUtils {

    /**
     * Creates a masked version of the config JSON for safe logging by replacing sensitive values with asterisks
     * @param originalConfig The original configuration JSON that may contain sensitive information
     * @return A deep copy of the configuration with sensitive values masked
     */
    public static JSONObject maskSensitiveInformation(JSONObject originalConfig) {
        if (originalConfig == null) {
            return null;
        }
        
        JSONObject maskedConfig = new JSONObject(originalConfig.toString()); // Deep copy
        
        if (maskedConfig.has("secrets")) {
            JSONObject secrets = maskedConfig.getJSONObject("secrets");
            // Mask all secret keys
            for (String key : secrets.keySet()) {
                if (isSensitiveKey(key)) {
                    String originalValue = secrets.optString(key, "");
                    if (!originalValue.isEmpty()) {
                        secrets.put(key, maskValue(originalValue));
                    }
                }
            }
        }
        
        // Also check for any top-level keys that might be sensitive
        for (String key : maskedConfig.keySet()) {
            if (isSensitiveKey(key) && !key.equals("secrets")) {
                String originalValue = maskedConfig.optString(key, "");
                if (!originalValue.isEmpty()) {
                    maskedConfig.put(key, maskValue(originalValue));
                }
            }
        }
        
        return maskedConfig;
    }

    /**
     * Determines if a key name indicates it contains sensitive information
     * @param key The key name to check
     * @return true if the key is likely to contain sensitive information
     */
    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        
        String upperKey = key.toUpperCase();
        return upperKey.contains("KEY") || 
               upperKey.contains("TOKEN") || 
               upperKey.contains("PASSWORD") || 
               upperKey.contains("SECRET") ||
               upperKey.contains("AUTH") ||
               upperKey.contains("CREDENTIAL") ||
               upperKey.contains("PASS");
    }

    /**
     * Masks a sensitive value by showing only the first few characters
     * @param value The value to mask
     * @return A masked version of the value
     */
    private static String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return "****";
        }
        
        // For very short values, mask completely
        if (value.length() <= 4) {
            return "****";
        }
        
        // For longer values, show first 4 characters and mask the rest
        return value.substring(0, 4) + "****";
    }

    /**
     * Masks a plain string value for safe logging
     * @param value The value to mask
     * @return A masked version of the value
     */
    public static String maskSensitiveValue(String value) {
        return maskValue(value);
    }
} 