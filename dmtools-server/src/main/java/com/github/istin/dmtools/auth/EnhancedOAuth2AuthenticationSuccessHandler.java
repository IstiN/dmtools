package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.OAuthProxyService;
import com.github.istin.dmtools.auth.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom OAuth2 authentication success handler for proxy OAuth flow
 */
@Component
public class EnhancedOAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedOAuth2AuthenticationSuccessHandler.class);
    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";
    
    private final SecurityContextRepository securityContextRepository;
    private final OAuthProxyService oAuthProxyService;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    
    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    public EnhancedOAuth2AuthenticationSuccessHandler(OAuthProxyService oAuthProxyService, 
                                                     UserService userService, 
                                                     JwtUtils jwtUtils) {
        this.oAuthProxyService = oAuthProxyService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(false); // Changed to false to allow custom redirects
        logger.info("EnhancedOAuth2AuthenticationSuccessHandler initialized with OAuth proxy support and JWT generation");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        logger.info("OAuth2 authentication success! Processing authentication...");
        
        String state = request.getParameter("state");
        logger.info("OAuth state parameter: {}", state);
        
        // Check if this is a proxied OAuth request
        if (oAuthProxyService.isProxyState(state)) {
            logger.info("🎯 OAUTH HANDLER - Detected OAuth proxy request! State: {}", state);
            try {
                logger.info("🚀 OAUTH HANDLER - Generating temp code and redirect for client...");
                String redirectUrl = oAuthProxyService.generateTempCodeAndRedirect(state, authentication);
                logger.info("✅ OAUTH HANDLER - Generated redirect URL successfully");
                logger.info("📤 OAUTH HANDLER - Redirecting to client application: {}", redirectUrl.replaceAll("code=[^&]*", "code=***"));
                response.sendRedirect(redirectUrl);
                return;
            } catch (Exception e) {
                logger.error("💥 OAUTH HANDLER - Failed to handle proxied OAuth", e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth proxy error: " + e.getMessage());
                return;
            }
        }
        
        // Standard OAuth flow (existing behavior for backward compatibility)
        logger.info("Standard OAuth flow, processing normally");
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            
            logger.info("OAuth2 authentication details:");
            logger.info("  - Registration ID: {}", oauth2Token.getAuthorizedClientRegistrationId());
            logger.info("  - Principal name: {}", oauth2User.getName());
            logger.info("  - Authorities: {}", oauth2Token.getAuthorities());
            logger.debug("  - User attributes: {}", oauth2User.getAttributes());
            
            // Extract user information and create/update user in database
            try {
                logger.info("💾 OAUTH HANDLER - Creating/updating user in database...");
                User user = userService.createOrUpdateOAuth2User(oauth2Token);
                logger.info("✅ OAUTH HANDLER - User created/updated successfully: {}", user.getEmail());
                
                // Generate JWT token for the user
                logger.info("🔐 OAUTH HANDLER - Generating JWT token for user: {}", user.getEmail());
                String jwtToken = jwtUtils.generateJwtToken(user.getEmail(), user.getId());
                logger.info("✅ OAUTH HANDLER - JWT token generated successfully");
                
                // Set JWT token as secure cookie
                setJwtCookie(response, jwtToken);
                logger.info("🍪 OAUTH HANDLER - JWT token set as secure cookie");
                
            } catch (Exception e) {
                logger.error("❌ OAUTH HANDLER - Failed to create user or generate JWT token", e);
                // Continue with session-based auth even if JWT generation fails
            }
            
            // Check session
            HttpSession session = request.getSession(true); // Always create session if it doesn't exist
            logger.info("Session: ID = {}", session.getId());
            logger.info("Session creation time: {}", session.getCreationTime());
            logger.info("Session last accessed: {}", session.getLastAccessedTime());
            
            // Store authentication in security context using official Spring Security approach
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            logger.info("Authentication stored in SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication() != null);
            
            // Store security context in session explicitly using the repository
            securityContextRepository.saveContext(securityContext, request, response);
            logger.info("SecurityContext saved to repository");
            
            // Also store it the traditional way (redundant but helps ensure it works)
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);
            logger.info("SecurityContext explicitly stored in session");
            
            // Set session timeout and protect against session fixation
            session.setMaxInactiveInterval(3600); // 1 hour timeout
            
            // Add auth success cookie for debugging
            if (isProductionEnvironment()) {
                // Use Set-Cookie header for production with SameSite attribute
                response.addHeader("Set-Cookie", "auth_success=true; Path=/; Max-Age=300; Secure; SameSite=Lax");
            } else {
                Cookie authCookie = new Cookie("auth_success", "true");
                authCookie.setPath("/");
                authCookie.setMaxAge(300); // 5 minutes
                response.addCookie(authCookie);
            }
            logger.info("Added auth_success cookie for debugging");
        } else {
            logger.warn("Authentication is not an OAuth2AuthenticationToken: {}", authentication.getClass().getSimpleName());
        }
        
        // Use default redirect behavior for standard OAuth
        setAlwaysUseDefaultTargetUrl(true);
        logger.info("Redirecting to default target URL: {}", getDefaultTargetUrl());
        super.onAuthenticationSuccess(request, response, authentication);
    }
    
    /**
     * Sets JWT token as a secure cookie with appropriate settings for production/development
     */
    private void setJwtCookie(HttpServletResponse response, String jwtToken) {
        if (isProductionEnvironment()) {
            // Use Set-Cookie header for production with all security attributes
            String cookieValue = String.format("jwt=%s; Path=/; Max-Age=86400; HttpOnly; Secure; SameSite=Lax", jwtToken);
            response.addHeader("Set-Cookie", cookieValue);
            logger.info("🔒 OAUTH HANDLER - Set secure JWT cookie for production environment");
        } else {
            // Use Cookie object for development (non-secure)
            Cookie jwtCookie = new Cookie("jwt", jwtToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // 24 hours (same as JWT expiration)
            response.addCookie(jwtCookie);
            logger.info("🔓 OAUTH HANDLER - Set non-secure JWT cookie for development environment");
        }
    }
    
    /**
     * Determines if we're running in a production environment
     */
    private boolean isProductionEnvironment() {
        return "prod".equals(activeProfile) || "production".equals(activeProfile);
    }
} 