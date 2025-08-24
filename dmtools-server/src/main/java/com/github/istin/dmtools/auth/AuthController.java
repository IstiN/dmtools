package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Value("${auth.local.enabled:true}")
    private boolean localAuthEnabled;

    @Value("${auth.local.username:testuser}")
    private String localUsername;

    @Value("${auth.local.password:secret123}")
    private String localPassword;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${auth.local.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
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

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        logger.info("🔍 AUTH DEBUG - getCurrentUser called, authentication: {}", 
                   authentication != null ? authentication.getClass().getSimpleName() : "null");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.info("🔍 AUTH DEBUG - No authentication or not authenticated");
            return ResponseEntity.ok(Map.of("authenticated", false));
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
            return ResponseEntity.ok(Map.of("authenticated", false, "message", "Authentication in progress"));
        } else if (principal instanceof String) {
            // Handle String principals, but check if it's a placeholder
            String principalStr = (String) principal;
            if (principalStr.startsWith("placeholder_")) {
                logger.info("🔍 AUTH DEBUG - Placeholder principal detected: {}", principalStr);
                return ResponseEntity.ok(Map.of("authenticated", false, "message", "Authentication in progress"));
            }
            email = principalStr;
            logger.info("🔍 AUTH DEBUG - String principal email: {}", email);
            user = userService.findByEmail(email).orElse(null);
        }

        if (user != null) {
            logger.info("✅ AUTH DEBUG - User found: {}, picture: {}", user.getEmail(), user.getPictureUrl());

            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("authenticated", true);
            userMap.put("id", user.getId());
            if (user.getEmail() != null) userMap.put("email", user.getEmail());
            if (user.getName() != null) userMap.put("name", user.getName());
            if (user.getGivenName() != null) userMap.put("givenName", user.getGivenName());
            if (user.getFamilyName() != null) userMap.put("familyName", user.getFamilyName());
            if (user.getPictureUrl() != null) {
                userMap.put("pictureUrl", user.getPictureUrl());
                userMap.put("picture", user.getPictureUrl()); // For backward compatibility
            }
            if (user.getProvider() != null) userMap.put("provider", user.getProvider());
            
            // Add role information
            String userRole = userService.getUserRole(user);
            userMap.put("role", userRole);

            return ResponseEntity.ok(userMap);
        }

        logger.warn("❌ AUTH DEBUG - No user found for email: {}", email);
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletRequest request) {
        // Invalidate the session
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("🔍 AUTH DEBUG - Invalidating session: {}", session.getId());
            session.invalidate();
        } else {
            logger.info("🔍 AUTH DEBUG - No session to invalidate");
        }
        
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/local-login")
    public ResponseEntity<?> localLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {
        logger.info("🔍 LOCAL AUTH - Login attempt for user: {}", body.get("username"));

        String username = body.get("username");
        String password = body.get("password");

        boolean isAuthenticated = false;
        String authenticatedUsername = null;
        String authenticatedPassword = null;

        if (authConfigProperties.isLocalStandaloneMode()) {
            logger.info("🔍 LOCAL AUTH - Operating in local standalone mode.");
            authenticatedUsername = authConfigProperties.getAdminUsername();
            authenticatedPassword = authConfigProperties.getAdminPassword();
            if (authenticatedUsername.equals(username) && authenticatedPassword.equals(password)) {
                isAuthenticated = true;
            }
        } else {
            logger.info("🔍 LOCAL AUTH - Operating in regular local auth mode. Local auth enabled: {}", localAuthEnabled);
            if (!localAuthEnabled) {
                logger.warn("❌ LOCAL AUTH - Local auth is disabled");
                return ResponseEntity.status(403).body(Map.of("error", "Local auth disabled"));
            }
            authenticatedUsername = localUsername;
            authenticatedPassword = localPassword;
            if (authenticatedUsername.equals(username) && authenticatedPassword.equals(password)) {
                isAuthenticated = true;
            }
        }

        if (!isAuthenticated) {
            logger.warn("❌ LOCAL AUTH - Invalid credentials for user: {}", username);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
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
            
            logger.info("✅ LOCAL AUTH - Login successful for user: {}", localUsername);
            
            // Get user role with fallback protection
            String userRole = userService.getUserRole(user);
            
            return ResponseEntity.ok(Map.of(
                "token", jwt,
                "user", Map.of(
                    "id", user.getId(), 
                    "email", email, 
                    "name", localUsername, 
                    "provider", "LOCAL",
                    "role", userRole != null ? userRole : "REGULAR_USER",
                    "authenticated", true
                )
            ));
        } catch (Exception e) {
            logger.error("❌ LOCAL AUTH - Error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/test-jwt")
    public ResponseEntity<?> testJwt(org.springframework.security.core.Authentication authentication) {
        logger.info("🔍 TEST JWT - testJwt called");
        
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No authentication"));
        }
        
        if (!authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        Object principal = authentication.getPrincipal();
        logger.info("✅ TEST JWT - Principal type: {}", principal.getClass().getName());
        
        if (principal instanceof User) {
            User user = (User) principal;
            return ResponseEntity.ok(Map.of(
                "message", "JWT authentication working",
                "userId", user.getId(),
                "email", user.getEmail(),
                "principalType", principal.getClass().getSimpleName()
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Authentication working but not JWT",
            "principalType", principal.getClass().getSimpleName(),
            "authType", authentication.getClass().getSimpleName()
        ));
    }

    @GetMapping("/simple-test")
    public ResponseEntity<String> simpleTest(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("No authentication");
        }
        
        if (!authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        return ResponseEntity.ok("Authentication working! Principal type: " + principal.getClass().getSimpleName());
    }

    @GetMapping("/basic-test")
    public ResponseEntity<String> basicTest() {
        logger.info("🔍 BASIC TEST - endpoint called");
        
        // Get authentication from SecurityContextHolder directly
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null) {
            logger.warn("❌ BASIC TEST - No authentication in SecurityContext");
            return ResponseEntity.status(401).body("No authentication");
        }
        
        logger.info("✅ BASIC TEST - Authentication found: {}", auth.getClass().getSimpleName());
        logger.info("✅ BASIC TEST - Is authenticated: {}", auth.isAuthenticated());
        logger.info("✅ BASIC TEST - Principal type: {}", auth.getPrincipal().getClass().getSimpleName());
        
        return ResponseEntity.ok("Authentication working! Principal: " + auth.getName());
    }

    @GetMapping("/public-test")
    public ResponseEntity<Map<String, String>> publicTest() {
        logger.info("🔍 PUBLIC TEST - endpoint called");
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "message", "Server is running",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @GetMapping("/is-local")
    public ResponseEntity<?> isLocal(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        Object principal = authentication.getPrincipal();
        logger.info("🔍 AUTH - /is-local endpoint. Principal type: {}", principal.getClass().getName());

        if (principal instanceof User) {
            logger.info("✅ AUTH - User principal is of type User");
            return ResponseEntity.ok(Map.of("isLocal", true));
        } else if (principal instanceof UserDetails) {
            logger.info("✅ AUTH - Principal is UserDetails");
            return ResponseEntity.ok(Map.of("isLocal", false));
        } else if (principal instanceof OAuth2User) {
            logger.info("✅ AUTH - Principal is OAuth2User");
            return ResponseEntity.ok(Map.of("isLocal", false));
        } else {
            logger.error("❌ AUTH - Unknown principal type: {}. Cannot determine if user is local.", principal.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Cannot determine if user is local"));
        }
    }
} 