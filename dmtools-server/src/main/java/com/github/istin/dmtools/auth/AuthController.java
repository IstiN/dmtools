package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.dto.*;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthConfigProperties authConfigProperties;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${auth.local.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    public AuthController(UserService userService, JwtUtils jwtUtils, AuthConfigProperties authConfigProperties) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.authConfigProperties = authConfigProperties;
    }

    @GetMapping("/login/{provider}")
    public ResponseEntity<?> initiateLogin(@PathVariable String provider) {
        // For development/demo purposes, return a message indicating OAuth setup needed
        // In production, you would configure real OAuth client IDs
        return ResponseEntity.ok(Map.of(
            "error", "OAuth not configured", 
            "message", "Please configure OAuth client credentials for " + provider + " in application.properties",
            "provider", provider
        ));
    }

    @GetMapping("/callback/{provider}")
    public ResponseEntity<?> handleCallback(@PathVariable String provider, @RequestParam String code) {
        // Mock successful authentication for now
        // TODO: Exchange code for access token and get user info
        String token = jwtUtils.generateJwtToken("user@example.com", "mock-user-id");
        
        // Redirect to frontend with token
        String redirectUrl = "/?token=" + token + "&login=success";
        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .body(Map.of("message", "Login successful"));
    }

    @GetMapping("/protected")
    @Operation(summary = "Protected test endpoint", description = "Test endpoint that requires authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Access granted",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - authentication required",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> protectedEndpoint() {
        return ResponseEntity.ok(new MessageResponse("This is a protected endpoint"));
    }

    @GetMapping("/user")
    @Operation(summary = "Get current authenticated user", description = "Returns information about the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthUserResponse.class)))
    })
    public ResponseEntity<AuthUserResponse> getCurrentUser(Authentication authentication) {
        logger.info("🔍 AUTH DEBUG - getCurrentUser called, authentication: {}", 
                   authentication != null ? authentication.getClass().getSimpleName() : "null");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.info("🔍 AUTH DEBUG - No authentication or not authenticated");
            return ResponseEntity.ok(new AuthUserResponse(false));
        }

        Object principal = authentication.getPrincipal();
        logger.info("🔍 AUTH DEBUG - Principal type: {}", principal.getClass().getSimpleName());
        
        User user = null;
        String email = null;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
            logger.info("🔍 AUTH DEBUG - UserDetails email: {}", email);
            user = userService.findByEmail(email).orElse(null);
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            logger.debug("🔍 AUTH DEBUG - OAuth2User attributes: {}", oauth2User.getAttributes());
            
            // Try different email attributes based on provider
            email = oauth2User.getAttribute("email");
            if (email == null) {
                email = oauth2User.getAttribute("mail"); // Microsoft
            }
            
            logger.info("🔍 AUTH DEBUG - OAuth2User email: {}", email);
            
            // Try to find user by email first
            if (email != null) {
                user = userService.findByEmail(email).orElse(null);
            }
            
            // If not found by email or email is null, try to find by providerId
            if (user == null) {
                String providerId = null;
                
                // Extract providerId based on OAuth2 provider
                if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauth2Token = 
                        (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
                    String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
                    
                    switch (registrationId.toLowerCase()) {
                        case "google":
                            providerId = oauth2User.getAttribute("sub");
                            break;
                        case "github":
                            Object id = oauth2User.getAttribute("id");
                            providerId = id != null ? id.toString() : null;
                            break;
                        case "microsoft":
                            providerId = oauth2User.getAttribute("id");
                            break;
                    }
                }
                
                logger.info("🔍 AUTH DEBUG - Trying to find user by providerId: {}", providerId);
                
                if (providerId != null) {
                    user = userService.findById(providerId).orElse(null);
                }
            }
        } else if (principal instanceof User) {
            user = (User) principal;
            logger.info("🔍 AUTH DEBUG - Direct User principal: {}", user.getEmail());
        } else if (authentication instanceof com.github.istin.dmtools.auth.PlaceholderAuthentication) {
            // Handle PlaceholderAuthentication during OAuth flow
            logger.info("🔍 AUTH DEBUG - PlaceholderAuthentication detected, authentication still in progress");
            return ResponseEntity.ok(new AuthUserResponse(false));
        } else if (principal instanceof String) {
            // Handle String principals, but check if it's a placeholder
            String principalStr = (String) principal;
            if (principalStr.startsWith("placeholder_")) {
                logger.info("🔍 AUTH DEBUG - Placeholder principal detected: {}", principalStr);
                return ResponseEntity.ok(new AuthUserResponse(false));
            }
            email = principalStr;
            logger.info("🔍 AUTH DEBUG - String principal email: {}", email);
            user = userService.findByEmail(email).orElse(null);
        }

        if (user != null) {
            logger.info("✅ AUTH DEBUG - User found: {}, picture: {}", user.getEmail(), user.getPictureUrl());

            AuthUserResponse response = new AuthUserResponse(true);
            response.setId(user.getId());
            response.setEmail(user.getEmail());
            response.setName(user.getName());
            response.setGivenName(user.getGivenName());
            response.setFamilyName(user.getFamilyName());
            response.setPictureUrl(user.getPictureUrl());
            response.setProvider(user.getProvider() != null ? user.getProvider().toString() : null);
            
            // Add role information
            String userRole = userService.getUserRole(user);
            response.setRole(userRole);

            return ResponseEntity.ok(response);
        }

        logger.warn("❌ AUTH DEBUG - No user found for email: {}", email);
        return ResponseEntity.ok(new AuthUserResponse(false));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the user session and logs out the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<MessageResponse> logout(jakarta.servlet.http.HttpServletRequest request) {
        // Invalidate the session
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("🔍 AUTH DEBUG - Invalidating session: {}", session.getId());
            session.invalidate();
        } else {
            logger.info("🔍 AUTH DEBUG - No session to invalidate");
        }
        
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/local-login")
    @Operation(summary = "Local login", description = "Authenticate user with local credentials (standalone mode only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocalLoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Local auth disabled",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> localLogin(@RequestBody LocalLoginRequest body, HttpServletResponse response) {
        logger.info("🔍 LOCAL AUTH - Login attempt for user: {}", body.getUsername());
        logger.info("🔍 LOCAL AUTH - Local standalone mode: {}", authConfigProperties.isLocalStandaloneMode());

        if (!authConfigProperties.isLocalStandaloneMode()) {
            logger.warn("❌ LOCAL AUTH - Local auth is disabled as not in standalone mode");
            return ResponseEntity.status(403).body(new ErrorResponse("Local auth disabled"));
        }
        
        String username = body.getUsername();
        String password = body.getPassword();

        if (!authConfigProperties.getAdminUsername().equals(username) || !authConfigProperties.getAdminPassword().equals(password)) {
            logger.warn("❌ LOCAL AUTH - Invalid credentials for user: {}", username);
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid credentials"));
        }
        
        try {
            logger.info("✅ LOCAL AUTH - Valid credentials, creating/updating user");
            
            // Create user if not exists - use proper email format
            String email = username.contains("@") ? username : username + "@local.test";
            com.github.istin.dmtools.auth.model.User user = userService.createOrUpdateUser(
                email, 
                username, 
                username, 
                "", 
                "", 
                "en", 
                com.github.istin.dmtools.auth.model.AuthProvider.LOCAL, 
                username
            );
            
            logger.info("✅ LOCAL AUTH - User created/updated: {}", user.getId());
            
            // Generate JWT
            String jwt = jwtUtils.generateJwtTokenCustom(email, user.getId(), jwtSecret, jwtExpirationMs);
            
            // Set JWT as cookie
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(jwtExpirationMs / 1000);
            response.addCookie(jwtCookie);
            
            logger.info("✅ LOCAL AUTH - Login successful for user: {}", username);
            
            // Get user role with fallback protection
            String userRole = userService.getUserRole(user);
            
            LocalLoginResponse.UserInfo userInfo = new LocalLoginResponse.UserInfo(
                user.getId(),
                email,
                username,
                "LOCAL",
                userRole != null ? userRole : "REGULAR_USER",
                true
            );
            
            return ResponseEntity.ok(new LocalLoginResponse(jwt, userInfo));
        } catch (Exception e) {
            logger.error("❌ LOCAL AUTH - Error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ErrorResponse("Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/is-local")
    @Operation(summary = "Check if user is local", description = "Determines if the authenticated user is a local user (not OAuth)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IsLocalResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> isLocal(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Not authenticated"));
        }

        Object principal = authentication.getPrincipal();
        logger.info("🔍 AUTH - /is-local endpoint. Principal type: {}", principal.getClass().getName());

        if (principal instanceof User) {
            logger.info("✅ AUTH - User principal is of type User");
            return ResponseEntity.ok(new IsLocalResponse(true));
        } else if (principal instanceof UserDetails) {
            logger.info("✅ AUTH - Principal is UserDetails");
            return ResponseEntity.ok(new IsLocalResponse(false));
        } else if (principal instanceof OAuth2User) {
            logger.info("✅ AUTH - Principal is OAuth2User");
            return ResponseEntity.ok(new IsLocalResponse(false));
        } else {
            logger.error("❌ AUTH - Unknown principal type: {}. Cannot determine if user is local.", principal.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Cannot determine if user is local"));
        }
    }
} 