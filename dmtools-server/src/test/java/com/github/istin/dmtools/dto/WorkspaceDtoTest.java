package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.auth.model.WorkspaceRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceDtoTest {

    @Test
    void testNoArgsConstructor() {
        WorkspaceDto dto = new WorkspaceDto();
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getOwnerId());
        assertNull(dto.getUsers());
        assertNull(dto.getDescription());
        assertNull(dto.getOwnerName());
        assertNull(dto.getOwnerEmail());
        assertNull(dto.getCurrentUserRole());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testPartialConstructor() {
        String id = "1";
        String name = "Test Workspace";
        String ownerId = "owner123";
        Set<WorkspaceUserDto> users = new HashSet<>();
        users.add(new WorkspaceUserDto("user1", "user1@example.com", WorkspaceRole.USER));

        WorkspaceDto dto = new WorkspaceDto(id, name, ownerId, users);
        
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(ownerId, dto.getOwnerId());
        assertEquals(users, dto.getUsers());
        assertNull(dto.getDescription());
        assertNull(dto.getOwnerName());
        assertNull(dto.getOwnerEmail());
        assertNull(dto.getCurrentUserRole());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        String id = "1";
        String name = "Test Workspace";
        String ownerId = "owner123";
        Set<WorkspaceUserDto> users = new HashSet<>();
        users.add(new WorkspaceUserDto("user1", "user1@example.com", WorkspaceRole.USER));
        String description = "Test Description";
        String ownerName = "Owner Name";
        String ownerEmail = "owner@example.com";
        WorkspaceRole currentUserRole = WorkspaceRole.ADMIN;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        WorkspaceDto dto = new WorkspaceDto(id, name, ownerId, users, description, ownerName, ownerEmail, 
                                           currentUserRole, createdAt, updatedAt);
        
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(ownerId, dto.getOwnerId());
        assertEquals(users, dto.getUsers());
        assertEquals(description, dto.getDescription());
        assertEquals(ownerName, dto.getOwnerName());
        assertEquals(ownerEmail, dto.getOwnerEmail());
        assertEquals(currentUserRole, dto.getCurrentUserRole());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        String id = "1";
        String name = "Test Workspace";
        String ownerId = "owner123";
        Set<WorkspaceUserDto> users = new HashSet<>();
        users.add(new WorkspaceUserDto("user1", "user1@example.com", WorkspaceRole.USER));
        String description = "Test Description";
        String ownerName = "Owner Name";
        String ownerEmail = "owner@example.com";
        WorkspaceRole currentUserRole = WorkspaceRole.ADMIN;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        WorkspaceDto dto = new WorkspaceDto();
        dto.setId(id);
        dto.setName(name);
        dto.setOwnerId(ownerId);
        dto.setUsers(users);
        dto.setDescription(description);
        dto.setOwnerName(ownerName);
        dto.setOwnerEmail(ownerEmail);
        dto.setCurrentUserRole(currentUserRole);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(ownerId, dto.getOwnerId());
        assertEquals(users, dto.getUsers());
        assertEquals(description, dto.getDescription());
        assertEquals(ownerName, dto.getOwnerName());
        assertEquals(ownerEmail, dto.getOwnerEmail());
        assertEquals(currentUserRole, dto.getCurrentUserRole());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Set<WorkspaceUserDto> users1 = new HashSet<>();
        users1.add(new WorkspaceUserDto("user1", "user1@example.com", WorkspaceRole.USER));
        
        Set<WorkspaceUserDto> users2 = new HashSet<>();
        users2.add(new WorkspaceUserDto("user2", "user2@example.com", WorkspaceRole.USER));
        
        WorkspaceDto dto1 = new WorkspaceDto("1", "Test", "owner1", users1);
        WorkspaceDto dto2 = new WorkspaceDto("1", "Test", "owner1", users2);
        WorkspaceDto dto3 = new WorkspaceDto("2", "Test", "owner1", users1);
        
        assertEquals(dto1, dto1);
        assertEquals(dto1.hashCode(), dto1.hashCode());
        
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        
        assertNotEquals(dto1, dto2);
    }
} 