package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.dto.OAuthExchangeRequest;
import com.github.istin.dmtools.auth.dto.OAuthInitiateRequest;
import com.github.istin.dmtools.auth.service.OAuthProxyService;
import com.github.istin.dmtools.auth.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
public class OAuthProxyController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthProxyController.class);
    private final OAuthProxyService oAuthProxyService;
    private final JwtUtils jwtUtils;

    public OAuthProxyController(OAuthProxyService oAuthProxyService, JwtUtils jwtUtils) {
        this.oAuthProxyService = oAuthProxyService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Step 1: Client initiates OAuth with custom redirect URI
     * POST /api/oauth/initiate
     * Body: {
     *   "provider": "google|microsoft|github",
     *   "client_redirect_uri": "https://myapp.com/auth/callback", // or "myapp://auth/callback"
     *   "client_type": "web|mobile|desktop",
     *   "environment": "dev|staging|prod"
     * }
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateOAuth(@RequestBody OAuthInitiateRequest request) {
        logger.info("🚀 OAUTH PROXY - INITIATE REQUEST RECEIVED");
        logger.info("📋 Request details: provider={}, client_type={}, environment={}, redirect_uri={}", 
                   request.getProvider(), request.getClientType(), request.getEnvironment(), request.getClientRedirectUri());
        
        try {
            // Validate request
            if (request.getProvider() == null || request.getClientRedirectUri() == null) {
                logger.error("❌ OAUTH PROXY - Invalid request: missing provider or redirect URI");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_request",
                    "message", "Provider and client_redirect_uri are required"
                ));
            }

            logger.info("✅ OAUTH PROXY - Request validation passed");

            // Generate temporary state with client info
            logger.info("🔑 OAUTH PROXY - Creating OAuth state...");
            String state = oAuthProxyService.createOAuthState(request);
            logger.info("✅ OAUTH PROXY - State created: {}", state);
            
            // Build OAuth provider URL
            logger.info("🔗 OAUTH PROXY - Building provider auth URL...");
            String authUrl = oAuthProxyService.buildProviderAuthUrl(request.getProvider(), state);
            logger.info("✅ OAUTH PROXY - Auth URL built successfully");
            
            logger.info("🎯 OAUTH PROXY - Generated OAuth state: {} for client redirect: {}", state, request.getClientRedirectUri());
            
            Map<String, Object> response = Map.of(
                "auth_url", authUrl,
                "state", state,
                "expires_in", 300 // 5 minutes
            );
            
            logger.info("📤 OAUTH PROXY - Returning successful response with auth_url");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("❌ OAUTH PROXY - Invalid OAuth initiation request", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "invalid_provider",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("💥 OAUTH PROXY - Failed to initiate OAuth", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "server_error",
                "message", "Failed to initiate OAuth: " + e.getMessage()
            ));
        }
    }

    /**
     * Step 2: Exchange temporary code for your service token
     * POST /api/oauth/exchange
     * Body: {
     *   "code": "temp_code_from_callback",
     *   "state": "original_state"
     * }
     */
    @PostMapping("/exchange")
    public ResponseEntity<?> exchangeCode(@RequestBody OAuthExchangeRequest request) {
        logger.info("🔄 OAUTH PROXY - CODE EXCHANGE REQUEST RECEIVED");
        logger.info("📋 Exchange details: code={}, state={}", 
                   request.getCode() != null ? "***PROVIDED***" : "NULL", request.getState());
        
        try {
            // Validate request
            if (request.getCode() == null || request.getState() == null) {
                logger.error("❌ OAUTH PROXY - Invalid exchange request: missing code or state");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_request",
                    "message", "Code and state are required"
                ));
            }

            logger.info("✅ OAUTH PROXY - Exchange request validation passed");

            // Validate and exchange code for JWT token
            logger.info("🔑 OAUTH PROXY - Exchanging temp code for JWT token...");
            String jwtToken = oAuthProxyService.exchangeCodeForToken(request.getCode(), request.getState());
            
            logger.info("✅ OAUTH PROXY - Successfully exchanged code for JWT token");
            
            Map<String, Object> response = Map.of(
                "access_token", jwtToken,
                "token_type", "Bearer",
                "expires_in", 3600 // 1 hour default
            );
            
            logger.info("📤 OAUTH PROXY - Returning successful token response");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            logger.error("❌ OAUTH PROXY - Invalid code exchange request", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "invalid_grant",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("💥 OAUTH PROXY - Failed to exchange code", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "server_error",
                "message", "Failed to exchange code: " + e.getMessage()
            ));
        }
    }

    /**
     * Get supported OAuth providers
     */
    @GetMapping("/providers")
    public ResponseEntity<?> getProviders() {
        return ResponseEntity.ok(Map.of(
            "providers", oAuthProxyService.getSupportedProviders(),
            "client_types", new String[]{"web", "mobile", "desktop"},
            "environments", new String[]{"dev", "staging", "prod"}
        ));
    }


} 