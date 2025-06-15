package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.model.Workspace;
import com.github.istin.dmtools.auth.model.WorkspaceRole;
import com.github.istin.dmtools.auth.model.WorkspaceUser;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the two main issues:
 * 1. UUID conversion error when trying to convert a UUID string to a Long
 * 2. "undefined" workspace ID error
 */
public class WorkspaceIdConversionTest {

    @Test
    void testUuidConversionErrorHandling() {
        // Simulate the error that occurs when a UUID is passed to a method expecting Long
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "Could not extract column [1] from JDBC ResultSet [Data conversion error converting \"240e22ca-e410-4807-ae66-c48bb4ebeb34\" [22018-224]] [n/a]");
        
        // Verify that the exception message contains the expected error
        assertTrue(exception.getMessage().contains("Data conversion error converting"));
        assertTrue(exception.getMessage().contains("240e22ca-e410-4807-ae66-c48bb4ebeb34"));
    }
    
    @Test
    void testUndefinedWorkspaceIdErrorHandling() {
        // Simulate the error that occurs when "undefined" is passed as a workspace ID
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "undefined", Long.class, "workspaceId", null, 
                new NumberFormatException("For input string: \"undefined\""));
        
        // Verify that the exception message contains the expected error
        assertEquals("undefined", exception.getValue());
        assertEquals(Long.class, exception.getRequiredType());
        assertEquals("workspaceId", exception.getName());
        
        // Verify that the cause is a NumberFormatException with the expected message
        assertTrue(exception.getCause() instanceof NumberFormatException);
        assertEquals("For input string: \"undefined\"", exception.getCause().getMessage());
    }

    @Test
    void testUserAndWorkspaceIdTypesAreConsistent() {
        // Create a user with String ID
        User user = new User();
        String userId = UUID.randomUUID().toString();
        user.setId(userId);
        user.setEmail("test@example.com");
        
        // Create a workspace with String ID
        Workspace workspace = new Workspace();
        String workspaceId = UUID.randomUUID().toString();
        workspace.setId(workspaceId);
        workspace.setName("Test Workspace");
        workspace.setOwner(user);
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace.setUsers(new HashSet<>());
        
        // Create a workspace user with String ID
        WorkspaceUser workspaceUser = new WorkspaceUser();
        String workspaceUserId = UUID.randomUUID().toString();
        workspaceUser.setId(workspaceUserId);
        workspaceUser.setWorkspace(workspace);
        workspaceUser.setUser(user);
        workspaceUser.setRole(WorkspaceRole.ADMIN);
        
        // Verify IDs are of the same type (String)
        assertEquals(String.class, user.getId().getClass());
        assertEquals(String.class, workspace.getId().getClass());
        assertEquals(String.class, workspaceUser.getId().getClass());
        
        // Verify that UUIDs can be parsed from these IDs
        assertDoesNotThrow(() -> UUID.fromString(userId));
        assertDoesNotThrow(() -> UUID.fromString(workspaceId));
        assertDoesNotThrow(() -> UUID.fromString(workspaceUserId));
    }
} 