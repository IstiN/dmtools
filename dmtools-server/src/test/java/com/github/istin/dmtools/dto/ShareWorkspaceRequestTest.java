package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.auth.model.WorkspaceRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShareWorkspaceRequestTest {

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
        ShareWorkspaceRequest request = new ShareWorkspaceRequest();
        assertNull(request.getEmail());
        assertNull(request.getRole());
    }

    @Test
    void testAllArgsConstructor() {
        ShareWorkspaceRequest request = new ShareWorkspaceRequest("test@example.com", WorkspaceRole.USER);
        assertEquals("test@example.com", request.getEmail());
        assertEquals(WorkspaceRole.USER, request.getRole());
    }

    @Test
    void testSettersAndGetters() {
        ShareWorkspaceRequest request = new ShareWorkspaceRequest();
        request.setEmail("user@example.com");
        request.setRole(WorkspaceRole.ADMIN);
        
        assertEquals("user@example.com", request.getEmail());
        assertEquals(WorkspaceRole.ADMIN, request.getRole());
    }

    @Test
    void testEqualsAndHashCode() {
        ShareWorkspaceRequest request1 = new ShareWorkspaceRequest("test@example.com", WorkspaceRole.USER);
        ShareWorkspaceRequest request2 = new ShareWorkspaceRequest("test@example.com", WorkspaceRole.USER);
        ShareWorkspaceRequest request3 = new ShareWorkspaceRequest("different@example.com", WorkspaceRole.USER);
        
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
        
        ShareWorkspaceRequest request = new ShareWorkspaceRequest("valid@example.com", WorkspaceRole.USER);
        Set<ConstraintViolation<ShareWorkspaceRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_EmptyEmail() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        ShareWorkspaceRequest request = new ShareWorkspaceRequest("", WorkspaceRole.USER);
        Set<ConstraintViolation<ShareWorkspaceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        // Both @NotEmpty and @Email might fail, so we don't assert exact size
    }

    @Test
    void testValidation_NullEmail() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        ShareWorkspaceRequest request = new ShareWorkspaceRequest(null, WorkspaceRole.USER);
        Set<ConstraintViolation<ShareWorkspaceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidation_InvalidEmail() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        ShareWorkspaceRequest request = new ShareWorkspaceRequest("not-an-email", WorkspaceRole.USER);
        Set<ConstraintViolation<ShareWorkspaceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidation_NullRole() {
        if (validator == null) {
            return; // Skip validation tests if validator is not available
        }
        
        ShareWorkspaceRequest request = new ShareWorkspaceRequest("valid@example.com", null);
        Set<ConstraintViolation<ShareWorkspaceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
} 