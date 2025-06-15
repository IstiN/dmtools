package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.auth.model.WorkspaceRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceUserDtoTest {

    @Test
    void testNoArgsConstructor() {
        WorkspaceUserDto dto = new WorkspaceUserDto();
        assertNull(dto.getId());
        assertNull(dto.getEmail());
        assertNull(dto.getRole());
    }

    @Test
    void testAllArgsConstructor() {
        String id = "user1";
        String email = "user@example.com";
        WorkspaceRole role = WorkspaceRole.USER;

        WorkspaceUserDto dto = new WorkspaceUserDto(id, email, role);
        
        assertEquals(id, dto.getId());
        assertEquals(email, dto.getEmail());
        assertEquals(role, dto.getRole());
    }

    @Test
    void testSettersAndGetters() {
        WorkspaceUserDto dto = new WorkspaceUserDto();
        
        String id = "user1";
        String email = "user@example.com";
        WorkspaceRole role = WorkspaceRole.ADMIN;
        
        dto.setId(id);
        dto.setEmail(email);
        dto.setRole(role);
        
        assertEquals(id, dto.getId());
        assertEquals(email, dto.getEmail());
        assertEquals(role, dto.getRole());
    }

    @Test
    void testEqualsAndHashCode() {
        WorkspaceUserDto dto1 = new WorkspaceUserDto("user1", "user@example.com", WorkspaceRole.USER);
        WorkspaceUserDto dto2 = new WorkspaceUserDto("user1", "user@example.com", WorkspaceRole.USER);
        WorkspaceUserDto dto3 = new WorkspaceUserDto("user2", "user@example.com", WorkspaceRole.USER);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        WorkspaceUserDto dto = new WorkspaceUserDto("user1", "user@example.com", WorkspaceRole.USER);
        String toString = dto.toString();
        
        assertTrue(toString.contains("id=user1"));
        assertTrue(toString.contains("email=user@example.com"));
        assertTrue(toString.contains("role=USER"));
    }
} 