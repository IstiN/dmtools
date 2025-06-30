package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.service.OAuthProxyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EnhancedOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedOAuth2AuthenticationSuccessHandler.class);
    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";
    private final SecurityContextRepository securityContextRepository;
    private final OAuthProxyService oAuthProxyService;

    public EnhancedOAuth2AuthenticationSuccessHandler(OAuthProxyService oAuthProxyService) {
        this.oAuthProxyService = oAuthProxyService;
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(false); // Changed to false to allow custom redirects
        logger.info("EnhancedOAuth2AuthenticationSuccessHandler initialized with OAuth proxy support");
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
            Cookie authCookie = new Cookie("auth_success", "true");
            authCookie.setPath("/");
            authCookie.setMaxAge(300); // 5 minutes
            response.addCookie(authCookie);
            logger.info("Added auth_success cookie for debugging");
        } else {
            logger.warn("Authentication is not an OAuth2AuthenticationToken: {}", authentication.getClass().getSimpleName());
        }
        
        // Use default redirect behavior for standard OAuth
        setAlwaysUseDefaultTargetUrl(true);
        logger.info("Redirecting to default target URL: {}", getDefaultTargetUrl());
        super.onAuthenticationSuccess(request, response, authentication);
    }
} 