package com.github.istin.dmtools.server;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.auth.model.Workspace;
import com.github.istin.dmtools.auth.model.WorkspaceUser;
import com.github.istin.dmtools.auth.service.WorkspaceService;
import com.github.istin.dmtools.dto.CreateWorkspaceRequest;
import com.github.istin.dmtools.dto.ShareWorkspaceRequest;
import com.github.istin.dmtools.dto.WorkspaceDto;
import com.github.istin.dmtools.dto.WorkspaceUserDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceController.class);

    private final WorkspaceService workspaceService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<WorkspaceDto> createWorkspace(@RequestBody CreateWorkspaceRequest request, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Workspace createdWorkspace = workspaceService.createWorkspace(request.getName(), request.getDescription(), user);
        return new ResponseEntity<>(convertToDto(createdWorkspace), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceDto>> getUserWorkspaces(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Workspace> workspaces = workspaceService.getUserWorkspaces(user);
        List<WorkspaceDto> workspaceDtos = workspaces.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDtos);
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDto> getWorkspaceById(@PathVariable String workspaceId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Workspace> workspace = workspaceService.getWorkspaceByIdForUser(workspaceId, user);
        return workspace.map(w -> ResponseEntity.ok(convertToDto(w)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable String workspaceId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (workspaceService.deleteWorkspace(workspaceId, user)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{workspaceId}/share")
    public ResponseEntity<?> shareWorkspace(@PathVariable String workspaceId, @RequestBody ShareWorkspaceRequest request, Authentication authentication) {
        User owner = getUserFromAuthentication(authentication);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            WorkspaceUser workspaceUser = workspaceService.shareWorkspace(workspaceId, request.getEmail(), request.getRole(), owner);
            return ResponseEntity.ok(new WorkspaceUserDto(workspaceUser.getUser().getId(), workspaceUser.getUser().getEmail(), workspaceUser.getRole()));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{workspaceId}/users/{targetUserId}")
    public ResponseEntity<?> removeUserFromWorkspace(@PathVariable String workspaceId,
                                                        @PathVariable String targetUserId,
                                                        Authentication authentication) {
        User owner = getUserFromAuthentication(authentication);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User targetUser = userService.findById(targetUserId)
                .orElse(null);

        if (targetUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Target user not found"));
        }

        try {
            workspaceService.removeUserFromWorkspace(workspaceId, owner, targetUser);
            return ResponseEntity.noContent().build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/default")
    public ResponseEntity<WorkspaceDto> createDefaultWorkspace(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Workspace workspace = workspaceService.createDefaultWorkspace(user);
            return ResponseEntity.ok(convertToDto(workspace));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Authentication is null or not authenticated.");
            return null;
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof OAuth2User) {
            email = ((OAuth2User) principal).getAttribute("email");
        } else if (principal instanceof User) {
            email = ((User) principal).getEmail();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            logger.warn("Unknown principal type: {}", principal.getClass().getName());
            return null;
        }

        if (email == null) {
            logger.warn("Could not extract email from principal.");
            return null;
        }

        return userService.findByEmail(email).orElse(null);
    }

    private WorkspaceDto convertToDto(Workspace workspace) {
        if (workspace == null) {
            return null;
        }
        Set<WorkspaceUserDto> userDtos = Optional.ofNullable(workspace.getUsers())
                .orElse(Collections.emptySet())
                .stream()
                .map(wu -> new WorkspaceUserDto(wu.getUser().getId(), wu.getUser().getEmail(), wu.getRole()))
                .collect(Collectors.toSet());

        WorkspaceDto dto = new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getOwner().getId(),
                userDtos
        );
        dto.setDescription(workspace.getDescription());
        dto.setOwnerName(workspace.getOwner().getName());
        dto.setOwnerEmail(workspace.getOwner().getEmail());
        dto.setCreatedAt(workspace.getCreatedAt());
        dto.setUpdatedAt(workspace.getUpdatedAt());
        return dto;
    }
} 