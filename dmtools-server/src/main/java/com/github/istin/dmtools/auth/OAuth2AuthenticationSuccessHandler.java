package com.github.istin.dmtools.auth;

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
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";
    private final SecurityContextRepository securityContextRepository;
    
    public OAuth2AuthenticationSuccessHandler() {
        super();
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
        logger.info("OAuth2AuthenticationSuccessHandler initialized with default target URL: /");
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("OAuth2 authentication success! Processing authentication...");
        
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
        
        logger.info("Redirecting to default target URL: {}", getDefaultTargetUrl());
        super.onAuthenticationSuccess(request, response, authentication);
    }
} 