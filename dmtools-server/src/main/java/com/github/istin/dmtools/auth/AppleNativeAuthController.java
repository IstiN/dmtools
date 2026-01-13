package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.dto.AppleNativeAuthRequest;
import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for handling native Apple Sign in with Apple authentication
 */
@RestController
@RequestMapping("/api/auth")
public class AppleNativeAuthController {

    private static final Logger logger = LoggerFactory.getLogger(AppleNativeAuthController.class);

    private final AppleTokenVerifier appleTokenVerifier;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AppleNativeAuthController(AppleTokenVerifier appleTokenVerifier,
                                   UserService userService,
                                   JwtUtils jwtUtils) {
        this.appleTokenVerifier = appleTokenVerifier;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Authenticate user with Apple Sign in with Apple native flow
     *
     * @param request Apple native authentication request containing identity token
     * @return JWT tokens for authenticated user
     */
    @PostMapping("/apple-native")
    public ResponseEntity<?> authenticateAppleNative(@RequestBody AppleNativeAuthRequest request) {
        logger.info("🍎 Apple native authentication request received");

        try {
            // Validate request
            if (request.getIdentityToken() == null || request.getIdentityToken().trim().isEmpty()) {
                logger.error("❌ Apple native auth: identity token is missing");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_request",
                    "message", "identity_token is required"
                ));
            }

            // Verify the Apple identity token
            AppleTokenVerifier.AppleUserInfo appleUser = appleTokenVerifier.verifyIdentityToken(request.getIdentityToken());
            logger.info("✅ Apple identity token verified for user: {}", appleUser.getUserId());

            // Extract additional user info from request (provided by client)
            String email = request.getEmail() != null ? request.getEmail() : appleUser.getEmail();
            String name = null;
            if (request.getGivenName() != null || request.getFamilyName() != null) {
                name = (request.getGivenName() != null ? request.getGivenName() : "") +
                       (request.getFamilyName() != null ? " " + request.getFamilyName() : "").trim();
            }

            String givenName = request.getGivenName();
            String familyName = request.getFamilyName();

            logger.info("👤 Apple user details - email: {}, name: {}, verified: {}, private: {}",
                       email, name, appleUser.isEmailVerified(), appleUser.isPrivateEmail());

            // Create or update user in database
            User user = userService.createOrUpdateUser(
                email,
                name,
                givenName,
                familyName,
                null, // pictureUrl - Apple doesn't provide in native flow
                null, // locale
                AuthProvider.APPLE,
                appleUser.getUserId() // provider-specific ID
            );

            logger.info("✅ Apple user created/updated in database: {}", user.getId());

            // Generate JWT tokens
            String accessToken = jwtUtils.generateJwtToken(email, user.getId());
            String refreshToken = jwtUtils.generateRefreshToken(email, user.getId());

            logger.info("🎯 Apple native authentication successful for user: {}", email);

            // Return tokens
            Map<String, Object> response = Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken,
                "token_type", "Bearer",
                "expires_in", 86400, // 24 hours
                "refresh_expires_in", 2592000, // 30 days
                "user_id", user.getId(),
                "email", email,
                "is_private_email", appleUser.isPrivateEmail()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("💥 Apple native authentication failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "authentication_failed",
                "message", "Failed to authenticate with Apple: " + e.getMessage()
            ));
        }
    }
}