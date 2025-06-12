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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtils jwtUtils;

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
    public ResponseEntity<?> getCurrentUser(java.security.Principal principal) {
        logger.info("🔍 AUTH DEBUG - getCurrentUser called");
        
        // Log complete request details for better debugging
        jakarta.servlet.http.HttpServletRequest request = 
            ((org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();
        
        logger.info("🔍 AUTH DEBUG - Request details:");
        logger.info("🔍 AUTH DEBUG - URL: {}", request.getRequestURL());
        logger.info("🔍 AUTH DEBUG - Method: {}", request.getMethod());
        logger.info("🔍 AUTH DEBUG - Session ID: {}", request.getSession(false) != null ? request.getSession(false).getId() : "No session");
        
        if (principal == null) {
            logger.warn("❌ AUTH DEBUG - GET /api/auth/user called with null principal. User is not authenticated.");
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        logger.info("✅ AUTH DEBUG - Principal found, type: {}", principal.getClass().getName());
        logger.info("✅ AUTH DEBUG - Principal name: {}", principal.getName());

        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            String provider = oauthToken.getAuthorizedClientRegistrationId();

            logger.info("✅ AUTH DEBUG - User is authenticated via OAuth2 from provider: {}", provider);
            logger.info("✅ AUTH DEBUG - OAuth2User class: {}", oauth2User.getClass().getName());
            logger.info("✅ AUTH DEBUG - OAuth2User attributes: {}", oauth2User.getAttributes());

            String id, email, name, givenName, familyName, pictureUrl;

            if ("microsoft".equalsIgnoreCase(provider)) {
                // Use the keys you see in the log!
                id = oauth2User.getAttribute("sub"); // fallback to "oid" if you want
                email = oauth2User.getAttribute("email");
                if (email == null) email = oauth2User.getAttribute("mail");
                if (email == null) email = oauth2User.getAttribute("preferred_username");
                if (email == null) email = oauth2User.getAttribute("userPrincipalName");
                name = oauth2User.getAttribute("name");
                givenName = oauth2User.getAttribute("given_name");
                familyName = oauth2User.getAttribute("family_name");
                pictureUrl = oauth2User.getAttribute("picture"); // might be null for MS
                logger.info("✅ AUTH DEBUG - Extracted Microsoft user attributes: id={}, email={}, name={}", id, email, name);
            } else { // Assuming Google or default
                id = oauth2User.getAttribute("sub");
                email = oauth2User.getAttribute("email");
                name = oauth2User.getAttribute("name");
                givenName = oauth2User.getAttribute("given_name");
                familyName = oauth2User.getAttribute("family_name");
                pictureUrl = oauth2User.getAttribute("picture");
                logger.info("✅ AUTH DEBUG - Extracted Google/default user attributes: id={}, email={}, name={}", id, email, name);
            }

            if (name == null || name.trim().isEmpty()) {
                if (givenName != null && familyName != null) {
                    name = givenName + " " + familyName;
                } else if (givenName != null) {
                    name = givenName;
                } else if (email != null) {
                    name = email.split("@")[0]; // Fallback to email username
                }
                logger.info("✅ AUTH DEBUG - Constructed name: {}", name);
            }

            Map<String, Object> responseMap = Map.of(
                "id", id != null ? id : "",
                "email", email != null ? email : "",
                "name", name != null ? name : "User",
                "givenName", givenName != null ? givenName : "",
                "familyName", familyName != null ? familyName : "",
                "pictureUrl", pictureUrl != null ? pictureUrl : "",
                "provider", provider.toUpperCase(),
                "authenticated", true
            );
            
            logger.info("✅ AUTH DEBUG - Returning OAuth2 user response: {}", responseMap);
            return ResponseEntity.ok(responseMap);
        }

        // Fallback for other authentication types
        logger.info("⚠️ AUTH DEBUG - Principal is not an OAuth2 token. Returning basic principal name.");
        Map<String, Object> responseMap = Map.of(
            "name", principal.getName(),
            "authenticated", true
        );
        logger.info("✅ AUTH DEBUG - Returning basic user response: {}", responseMap);
        return ResponseEntity.ok(responseMap);
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
} 