package com.github.istin.dmtools.server.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Generic service for transforming flat dot notation parameters to nested object structures.
 * Supports transformation of parameters like "agentParams.aiRole" to nested Map structures.
 */
@Service
public class DotNotationTransformer {

    /**
     * Transforms a flat map with dot notation keys to a nested map structure.
     * 
     * @param flatParams Map with keys potentially containing dots (e.g., "agentParams.aiRole")
     * @return Map with nested structure where dot notation becomes nested objects
     */
    public Map<String, Object> transformToNested(Map<String, Object> flatParams) {
        if (flatParams == null) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        
        flatParams.forEach((key, value) -> {
            if (key != null && key.contains(".")) {
                setNestedValue(result, key, value);
            } else {
                result.put(key, value);
            }
        });
        
        return result;
    }

    /**
     * Sets a nested value in the map using dot notation path.
     * 
     * @param map The target map to set the value in
     * @param path Dot notation path (e.g., "agentParams.aiRole")
     * @param value The value to set
     */
    @SuppressWarnings("unchecked")
    private void setNestedValue(Map<String, Object> map, String path, Object value) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        String[] parts = path.split("\\.");
        Map<String, Object> current = map;
        
        // Navigate/create nested structure up to the last key
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            current = (Map<String, Object>) current.computeIfAbsent(
                part, k -> new HashMap<String, Object>()
            );
        }
        
        // Set the final value
        String finalKey = parts[parts.length - 1];
        current.put(finalKey, value);
    }

    /**
     * Transforms a nested map back to flat dot notation structure.
     * Useful for serialization or reverse transformation.
     * 
     * @param nestedParams Map with nested structure
     * @return Map with flat dot notation keys
     */
    public Map<String, Object> transformToFlat(Map<String, Object> nestedParams) {
        if (nestedParams == null) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        flattenMap(nestedParams, "", result);
        return result;
    }

    /**
     * Recursively flattens nested map structure to dot notation.
     * 
     * @param map Map to flatten
     * @param prefix Current prefix for keys
     * @param result Target flat map
     */
    @SuppressWarnings("unchecked")
    private void flattenMap(Map<String, Object> map, String prefix, Map<String, Object> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                flattenMap((Map<String, Object>) value, key, result);
            } else {
                result.put(key, value);
            }
        }
    }

    /**
     * Validates that a dot notation key is properly formatted.
     * 
     * @param key The dot notation key to validate
     * @return true if the key is valid, false otherwise
     */
    public boolean isValidDotNotationKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        
        // Check for consecutive dots or leading/trailing dots
        if (key.startsWith(".") || key.endsWith(".") || key.contains("..")) {
            return false;
        }
        
        // Check that all parts are valid identifiers
        String[] parts = key.split("\\.");
        for (String part : parts) {
            if (part.trim().isEmpty() || !part.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                return false;
            }
        }
        
        return true;
    }
}