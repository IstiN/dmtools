package com.github.istin.dmtools.auth;

import jakarta.servlet.FilterChain;
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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Component
public class AuthDebugFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthDebugFilter.class);
    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // Special breakpoint for OAuth2 callback endpoint
        boolean isOAuth2Callback = requestURI.contains("/login/oauth2/code/");
        if (isOAuth2Callback) {
            logger.info("🚨 OAUTH2 CALLBACK DETECTED: {}", requestURI);
            logger.info("🚨 Request parameters: {}", request.getParameterMap().entrySet().stream()
                    .map(e -> e.getKey() + "=" + String.join(",", Arrays.asList(e.getValue())))
                    .collect(Collectors.joining(", ")));
            
            // Log cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                logger.info("🚨 Cookies: {}", Arrays.stream(cookies)
                        .map(c -> c.getName() + "=" + c.getValue())
                        .collect(Collectors.joining(", ")));
            } else {
                logger.info("🚨 No cookies found");
            }
        }
        
        // Only log for authentication-related requests to avoid spam
        if (requestURI.contains("/api/auth") || requestURI.contains("/oauth2") || requestURI.contains("/login") || isOAuth2Callback) {
            logger.info("🔍 AUTH DEBUG - Request: {} {}", request.getMethod(), requestURI);
            
            // Log session information
            HttpSession session = request.getSession(false);
            if (session != null) {
                logger.info("🔍 AUTH DEBUG - Session ID: {}, Created: {}, Last Accessed: {}", 
                    session.getId(), 
                    new java.util.Date(session.getCreationTime()),
                    new java.util.Date(session.getLastAccessedTime()));
                
                // Log session attributes
                logger.debug("🔍 AUTH DEBUG - Session attributes:");
                Enumeration<String> attributeNames = session.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    String attributeName = attributeNames.nextElement();
                    logger.debug("🔍 AUTH DEBUG - Session attribute: {} = {}", attributeName, 
                            attributeName.contains("SPRING_SECURITY_CONTEXT") ? "[SECURITY_CONTEXT]" : session.getAttribute(attributeName));
                }
                
                // Check if security context exists in session using Spring's key
                Object securityContextFromSession = session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
                if (securityContextFromSession != null) {
                    logger.info("🔍 AUTH DEBUG - SecurityContext found in session under SPRING_SECURITY_CONTEXT_KEY");
                    
                    if (securityContextFromSession instanceof SecurityContext) {
                        SecurityContext sc = (SecurityContext) securityContextFromSession;
                        logger.info("🔍 AUTH DEBUG - SecurityContext from session authentication: {}", 
                                sc.getAuthentication() != null ? sc.getAuthentication().getName() : "null");
                    }
                } else {
                    logger.warn("🔍 AUTH DEBUG - No SecurityContext found in session under SPRING_SECURITY_CONTEXT_KEY");
                }
            } else {
                logger.info("🔍 AUTH DEBUG - No session found");
            }
            
            // Log security context from SecurityContextHolder
            SecurityContext securityContext = SecurityContextHolder.getContext();
            Authentication authentication = securityContext.getAuthentication();
            
            if (authentication != null) {
                logger.info("🔍 AUTH DEBUG - Authentication found: {}", authentication.getClass().getSimpleName());
                logger.info("🔍 AUTH DEBUG - Principal: {}", authentication.getName());
                logger.info("🔍 AUTH DEBUG - Authenticated: {}", authentication.isAuthenticated());
                logger.info("🔍 AUTH DEBUG - Authorities: {}", authentication.getAuthorities());
                
                if (authentication instanceof OAuth2AuthenticationToken) {
                    OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                    logger.info("🔍 AUTH DEBUG - OAuth2 Provider: {}", oauth2Token.getAuthorizedClientRegistrationId());
                    logger.info("🔍 AUTH DEBUG - OAuth2 Principal type: {}", oauth2Token.getPrincipal().getClass().getName());
                    logger.info("🔍 AUTH DEBUG - OAuth2 Attributes keys: {}", oauth2Token.getPrincipal().getAttributes().keySet());
                }
            } else {
                logger.info("🔍 AUTH DEBUG - No authentication found in SecurityContext");
            }
            
            // Log request headers that might be relevant for authentication
            logger.debug("🔍 AUTH DEBUG - Authentication related headers:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (headerName.toLowerCase().contains("auth") || 
                    headerName.toLowerCase().contains("cookie") || 
                    headerName.toLowerCase().contains("token")) {
                    logger.debug("🔍 AUTH DEBUG - Header: {} = {}", headerName, 
                            headerName.toLowerCase().contains("cookie") ? "[COOKIE_VALUE]" : request.getHeader(headerName));
                }
            }
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
        
        // Special post-processing for OAuth2 callback (after the request is processed)
        if (isOAuth2Callback) {
            logger.info("🚨 OAUTH2 CALLBACK PROCESSED - Response status: {}", response.getStatus());
            logger.info("🚨 Authentication after processing: {}", SecurityContextHolder.getContext().getAuthentication() != null ? 
                    SecurityContextHolder.getContext().getAuthentication().getName() : "null");
        }
    }
} 