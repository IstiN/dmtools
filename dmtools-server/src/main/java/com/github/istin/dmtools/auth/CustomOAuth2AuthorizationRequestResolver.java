package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.service.OAuthProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(ClientRegistrationRepository.class)
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2AuthorizationRequestResolver.class);
    
    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private final OAuthProxyService oAuthProxyService;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
                                                   OAuthProxyService oAuthProxyService) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
        this.oAuthProxyService = oAuthProxyService;
        logger.info("🔧 CustomOAuth2AuthorizationRequestResolver initialized");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        logger.debug("🔍 OAUTH RESOLVER - Resolving authorization request for: {}", request.getRequestURI());
        
        // Check if this request contains a proxy state parameter
        String state = request.getParameter("state");
        if (state != null && oAuthProxyService.isProxyState(state)) {
            logger.info("🎯 OAUTH RESOLVER - Detected proxy state in request: {}", state);
            // For proxy states, we don't need to create a new authorization request
            // The proxy flow has already been initiated
            return null;
        }
        
        // Use default resolver for standard OAuth flows
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
        logger.debug("🔄 OAUTH RESOLVER - Default resolver returned: {}", authRequest != null ? "found" : "not found");
        
        return authRequest;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        logger.debug("🔍 OAUTH RESOLVER - Resolving authorization request for client: {} URI: {}", 
                    clientRegistrationId, request.getRequestURI());
        
        // Check if this request contains a proxy state parameter
        String state = request.getParameter("state");
        if (state != null && oAuthProxyService.isProxyState(state)) {
            logger.info("🎯 OAUTH RESOLVER - Detected proxy state for client {}: {}", clientRegistrationId, state);
            // For proxy states, we don't need to create a new authorization request
            return null;
        }
        
        // Use default resolver for standard OAuth flows
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
        logger.debug("🔄 OAUTH RESOLVER - Default resolver for client {} returned: {}", 
                    clientRegistrationId, authRequest != null ? "found" : "not found");
        
        return authRequest;
    }
} 