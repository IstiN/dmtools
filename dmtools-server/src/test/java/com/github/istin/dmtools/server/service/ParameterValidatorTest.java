package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.server.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParameterValidator service.
 */
class ParameterValidatorTest {

    private ParameterValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ParameterValidator();
    }

    @Test
    void testValidateTeammateParameters_ValidParameters() {
        // Given
        Map<String, Object> agentParams = new HashMap<>();
        agentParams.put("aiRole", "Senior Code Reviewer");
        agentParams.put("instructions", Arrays.asList("Review for bugs", "Check security"));
        agentParams.put("formattingRules", "Use markdown format");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams", agentParams);

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> validator.validateTeammateParameters(parameters));
    }

    @Test
    void testValidateTeammateParameters_MissingRequiredFields() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("initiator", "user@example.com");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("inputJql is required"));
    }

    @Test
    void testValidateTeammateParameters_InvalidEmailFormat() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "invalid-email");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("initiator must be a valid email address"));
    }

    @Test
    void testValidateTeammateParameters_EmptyJql() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "  ");
        parameters.put("initiator", "user@example.com");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("inputJql cannot be empty"));
    }

    @Test
    void testValidateTeammateParameters_MissingAiRole() {
        // Given
        Map<String, Object> agentParams = new HashMap<>();
        agentParams.put("instructions", Arrays.asList("Review code"));
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams", agentParams);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("agentParams.aiRole is required"));
    }

    @Test
    void testValidateTeammateParameters_TooManyInstructions() {
        // Given
        Map<String, Object> agentParams = new HashMap<>();
        agentParams.put("aiRole", "Code Reviewer");
        List<String> tooManyInstructions = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            tooManyInstructions.add("Instruction " + i);
        }
        agentParams.put("instructions", tooManyInstructions);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams", agentParams);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("cannot have more than 10 items"));
    }

    @Test
    void testValidateTeammateParameters_StringTooLong() {
        // Given
        Map<String, Object> agentParams = new HashMap<>();
        agentParams.put("aiRole", "a".repeat(1001)); // Too long
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams", agentParams);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("must be no more than 1000 characters"));
    }

    @Test
    void testValidateTeammateParameters_DotNotationParameters() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams.aiRole", "Code Reviewer");
        parameters.put("agentParams.instructions", Arrays.asList("Review code"));
        parameters.put("agentParams.formattingRules", "Use markdown");

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> validator.validateTeammateParameters(parameters));
    }

    @Test
    void testValidateTeammateParameters_InvalidDotNotationAiRole() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams.aiRole", null); // Required but null

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("agentParams.aiRole is required"));
    }

    @Test
    void testValidateTeammateParameters_InvalidInstructionType() {
        // Given
        Map<String, Object> agentParams = new HashMap<>();
        agentParams.put("aiRole", "Code Reviewer");
        agentParams.put("instructions", 123); // Should be array or string
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams", agentParams);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("must be an array or string"));
    }

    @Test
    void testValidateTeammateParameters_NullParameters() {
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(null));
        assertTrue(exception.getMessage().contains("Parameters cannot be null"));
    }

    @Test
    void testValidateTeammateParameters_ValidEmailFormats() {
        // Given - Test various valid email formats
        String[] validEmails = {
            "user@example.com",
            "test.user@company.org",
            "admin+test@domain.co.uk",
            "712020:2a248756-40e8-49d6-8ddc-6852e518451f"  // User ID format
        };

        for (String email : validEmails) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("inputJql", "key = DMC-123");
            parameters.put("initiator", email);

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> validator.validateTeammateParameters(parameters),
                "Email should be valid: " + email);
        }
    }

    @Test
    void testValidateTeammateParameters_InvalidFieldTypes() {
        // Given
        Map<String, Object> agentParams = new HashMap<>();
        agentParams.put("aiRole", 123); // Should be string
        agentParams.put("formattingRules", Arrays.asList("not", "a", "string")); // Should be string
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams", agentParams);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validateTeammateParameters(parameters));
        assertTrue(exception.getMessage().contains("must be a string"));
    }

    @Test
    void testValidateTeammateParameters_OptionalFieldsCanBeNull() {
        // Given
        Map<String, Object> agentParams = new HashMap<>();
        agentParams.put("aiRole", "Code Reviewer");
        agentParams.put("instructions", null);
        agentParams.put("formattingRules", null);
        agentParams.put("fewShots", null);
        agentParams.put("knownInfo", null);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputJql", "key = DMC-123");
        parameters.put("initiator", "user@example.com");
        parameters.put("agentParams", agentParams);

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> validator.validateTeammateParameters(parameters));
    }
}