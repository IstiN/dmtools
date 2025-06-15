package com.github.istin.dmtools.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateWorkspaceRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        } catch (Exception e) {
            // If validation is not available, tests requiring validation will be skipped
            System.out.println("Validation not available: " + e.getMessage());
        }
    }

    @Test
    void testNoArgsConstructor() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        assertNull(request.getName());
        assertNull(request.getDescription());
    }

    @Test
    void testAllArgsConstructor() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest("Test Workspace", "Test Description");
        assertEquals("Test Workspace", request.getName());
        assertEquals("Test Description", request.getDescription());
    }

    @Test
    void testSettersAndGetters() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("New Workspace");
        request.setDescription("New Description");
        
        assertEquals("New Workspace", request.getName());
        assertEquals("New Description", request.getDescription());
    }

    @Test
    void testEqualsAndHashCode() {
        CreateWorkspaceRequest request1 = new CreateWorkspaceRequest("Test", "Description");
        CreateWorkspaceRequest request2 = new CreateWorkspaceRequest("Test", "Description");
        CreateWorkspaceRequest request3 = new CreateWorkspaceRequest("Different", "Description");
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testValidation_ValidRequest() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        CreateWorkspaceRequest request = new CreateWorkspaceRequest("Valid Name", "Valid Description");
        Set<ConstraintViolation<CreateWorkspaceRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_BlankName() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        CreateWorkspaceRequest request = new CreateWorkspaceRequest("", "Valid Description");
        Set<ConstraintViolation<CreateWorkspaceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        boolean hasNameRequiredMessage = violations.stream()
            .anyMatch(v -> v.getMessage().equals("Workspace name is required"));
        assertTrue(hasNameRequiredMessage, "Should have 'Workspace name is required' message");
    }

    @Test
    void testValidation_NullName() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        CreateWorkspaceRequest request = new CreateWorkspaceRequest(null, "Valid Description");
        Set<ConstraintViolation<CreateWorkspaceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Workspace name is required", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NameTooLong() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        String longName = "a".repeat(101); // 101 characters
        CreateWorkspaceRequest request = new CreateWorkspaceRequest(longName, "Valid Description");
        Set<ConstraintViolation<CreateWorkspaceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Workspace name must be between 1 and 100 characters", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_DescriptionTooLong() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        String longDescription = "a".repeat(501); // 501 characters
        CreateWorkspaceRequest request = new CreateWorkspaceRequest("Valid Name", longDescription);
        Set<ConstraintViolation<CreateWorkspaceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Description cannot exceed 500 characters", violations.iterator().next().getMessage());
    }
} 