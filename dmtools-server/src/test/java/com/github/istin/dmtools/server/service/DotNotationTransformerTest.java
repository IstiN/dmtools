package com.github.istin.dmtools.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DotNotationTransformer service.
 */
class DotNotationTransformerTest {

    private DotNotationTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new DotNotationTransformer();
    }

    @Test
    void testTransformToNested_SimpleCase() {
        // Given
        Map<String, Object> flatParams = new HashMap<>();
        flatParams.put("agentParams.aiRole", "Code Reviewer");
        flatParams.put("agentParams.instructions", Arrays.asList("Review for bugs", "Check security"));
        flatParams.put("inputJql", "key = DMC-123");

        // When
        Map<String, Object> result = transformer.transformToNested(flatParams);

        // Then
        assertEquals("key = DMC-123", result.get("inputJql"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> agentParams = (Map<String, Object>) result.get("agentParams");
        assertNotNull(agentParams);
        assertEquals("Code Reviewer", agentParams.get("aiRole"));
        assertEquals(Arrays.asList("Review for bugs", "Check security"), agentParams.get("instructions"));
    }

    @Test
    void testTransformToNested_DeepNesting() {
        // Given
        Map<String, Object> flatParams = new HashMap<>();
        flatParams.put("config.database.host", "localhost");
        flatParams.put("config.database.port", 5432);
        flatParams.put("config.cache.enabled", true);

        // When
        Map<String, Object> result = transformer.transformToNested(flatParams);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) result.get("config");
        assertNotNull(config);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> database = (Map<String, Object>) config.get("database");
        assertNotNull(database);
        assertEquals("localhost", database.get("host"));
        assertEquals(5432, database.get("port"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cache = (Map<String, Object>) config.get("cache");
        assertNotNull(cache);
        assertEquals(true, cache.get("enabled"));
    }

    @Test
    void testTransformToNested_NoDotsInKeys() {
        // Given
        Map<String, Object> flatParams = new HashMap<>();
        flatParams.put("inputJql", "key = DMC-123");
        flatParams.put("initiator", "user@example.com");

        // When
        Map<String, Object> result = transformer.transformToNested(flatParams);

        // Then
        assertEquals(flatParams, result);
    }

    @Test
    void testTransformToNested_NullInput() {
        // When
        Map<String, Object> result = transformer.transformToNested(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTransformToNested_EmptyInput() {
        // Given
        Map<String, Object> flatParams = new HashMap<>();

        // When
        Map<String, Object> result = transformer.transformToNested(flatParams);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTransformToFlat() {
        // Given
        Map<String, Object> agentParams = new HashMap<>();
        agentParams.put("aiRole", "Code Reviewer");
        agentParams.put("instructions", Arrays.asList("Review for bugs"));
        
        Map<String, Object> nestedParams = new HashMap<>();
        nestedParams.put("inputJql", "key = DMC-123");
        nestedParams.put("agentParams", agentParams);

        // When
        Map<String, Object> result = transformer.transformToFlat(nestedParams);

        // Then
        assertEquals("key = DMC-123", result.get("inputJql"));
        assertEquals("Code Reviewer", result.get("agentParams.aiRole"));
        assertEquals(Arrays.asList("Review for bugs"), result.get("agentParams.instructions"));
    }

    @Test
    void testIsValidDotNotationKey_ValidKeys() {
        assertTrue(transformer.isValidDotNotationKey("agentParams.aiRole"));
        assertTrue(transformer.isValidDotNotationKey("config.database.host"));
        assertTrue(transformer.isValidDotNotationKey("simple"));
        assertTrue(transformer.isValidDotNotationKey("a.b.c.d.e"));
    }

    @Test
    void testIsValidDotNotationKey_InvalidKeys() {
        assertFalse(transformer.isValidDotNotationKey(null));
        assertFalse(transformer.isValidDotNotationKey(""));
        assertFalse(transformer.isValidDotNotationKey(" "));
        assertFalse(transformer.isValidDotNotationKey(".startWithDot"));
        assertFalse(transformer.isValidDotNotationKey("endWithDot."));
        assertFalse(transformer.isValidDotNotationKey("double..dot"));
        assertFalse(transformer.isValidDotNotationKey("123invalid"));
        assertFalse(transformer.isValidDotNotationKey("invalid-dash"));
    }

    @Test
    void testTransformToNested_ArrayValues() {
        // Given
        Map<String, Object> flatParams = new HashMap<>();
        flatParams.put("agentParams.instructions", Arrays.asList("Check security", "Review performance"));
        flatParams.put("agentParams.tags", Arrays.asList("java", "security"));

        // When
        Map<String, Object> result = transformer.transformToNested(flatParams);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> agentParams = (Map<String, Object>) result.get("agentParams");
        assertNotNull(agentParams);
        
        @SuppressWarnings("unchecked")
        List<String> instructions = (List<String>) agentParams.get("instructions");
        assertEquals(Arrays.asList("Check security", "Review performance"), instructions);
        
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) agentParams.get("tags");
        assertEquals(Arrays.asList("java", "security"), tags);
    }

    @Test
    void testTransformToNested_MixedTypes() {
        // Given
        Map<String, Object> flatParams = new HashMap<>();
        flatParams.put("config.timeout", 30000);
        flatParams.put("config.enabled", true);
        flatParams.put("config.name", "test-config");
        flatParams.put("config.values", Arrays.asList(1, 2, 3));

        // When
        Map<String, Object> result = transformer.transformToNested(flatParams);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) result.get("config");
        assertNotNull(config);
        assertEquals(30000, config.get("timeout"));
        assertEquals(true, config.get("enabled"));
        assertEquals("test-config", config.get("name"));
        assertEquals(Arrays.asList(1, 2, 3), config.get("values"));
    }
}