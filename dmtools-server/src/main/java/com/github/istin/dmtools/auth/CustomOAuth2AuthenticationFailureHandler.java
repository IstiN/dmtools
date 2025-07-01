package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.service.OAuthProxyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnBean(ClientRegistrationRepository.class)
public class CustomOAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2AuthenticationFailureHandler.class);
    
    private final OAuthProxyService oAuthProxyService;

    public CustomOAuth2AuthenticationFailureHandler(OAuthProxyService oAuthProxyService) {
        this.oAuthProxyService = oAuthProxyService;
        logger.info("🔧 CustomOAuth2AuthenticationFailureHandler initialized");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        logger.info("🚨 OAUTH FAILURE - Authentication failure occurred: {}", exception.getMessage());
        logger.debug("🚨 OAUTH FAILURE - Exception type: {}", exception.getClass().getSimpleName());
        
        String state = request.getParameter("state");
        String code = request.getParameter("code");
        
        logger.info("📋 OAUTH FAILURE - Request details: URI={}, state={}, code={}", 
                   request.getRequestURI(), state, code != null ? "present" : "null");
        
        // Check if this is a proxy state and the error is about missing authorization request
        if (state != null && oAuthProxyService.isProxyState(state)) {
            logger.info("🎯 OAUTH FAILURE - Detected proxy state in failed request: {}", state);
            
            // Check if the error is about authorization request not found
            if (exception instanceof OAuth2AuthenticationException) {
                OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
                String errorCode = oauth2Exception.getError().getErrorCode();
                
                logger.info("🔍 OAUTH FAILURE - OAuth2 error code: {}", errorCode);
                
                if ("authorization_request_not_found".equals(errorCode) && code != null) {
                    logger.info("✨ OAUTH FAILURE - This is the expected authorization_request_not_found error for proxy state!");
                    logger.info("🔄 OAUTH FAILURE - Attempting to handle proxy OAuth callback manually...");
                    
                    try {
                        // Handle the proxy OAuth callback manually
                        handleProxyOAuthCallback(request, response, state, code);
                        return;
                    } catch (Exception e) {
                        logger.error("💥 OAUTH FAILURE - Error handling proxy OAuth callback", e);
                        // Fall through to default error handling
                    }
                }
            }
        }
        
        logger.warn("❌ OAUTH FAILURE - Falling back to default error handling");
        super.onAuthenticationFailure(request, response, exception);
    }
    
    private void handleProxyOAuthCallback(HttpServletRequest request, HttpServletResponse response, 
                                        String state, String code) throws IOException {
        logger.info("🔧 OAUTH PROXY CALLBACK - Manually handling OAuth callback for proxy state: {}", state);
        logger.info("📋 OAUTH PROXY CALLBACK - Authorization code: {}", code.substring(0, Math.min(10, code.length())) + "...");
        
        try {
            // For now, let's redirect to the client with the authorization code
            // The client can then use the /api/oauth/exchange endpoint to get the JWT token
            
            // Get the client redirect URI from the stored state
            String clientRedirectUri = oAuthProxyService.getClientRedirectUri(state);
            if (clientRedirectUri == null) {
                logger.error("❌ OAUTH PROXY CALLBACK - Could not find client redirect URI for state: {}", state);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid OAuth state");
                return;
            }
            
            logger.info("🎯 OAUTH PROXY CALLBACK - Client redirect URI: {}", clientRedirectUri);
            
            // Generate temporary code
            String tempCode = oAuthProxyService.generateTempCode(state, code);
            if (tempCode == null) {
                logger.error("❌ OAUTH PROXY CALLBACK - Could not generate temporary code");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate temporary code");
                return;
            }
            
            // Build redirect URL with temporary code and original state
            String redirectUrl = clientRedirectUri + 
                (clientRedirectUri.contains("?") ? "&" : "?") + 
                "code=" + tempCode + "&state=" + state;
            
            logger.info("📤 OAUTH PROXY CALLBACK - Redirecting to client: {}", redirectUrl.replaceAll("code=[^&]*", "code=***"));
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            logger.error("💥 OAUTH PROXY CALLBACK - Error in manual callback handling", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth callback error: " + e.getMessage());
        }
    }
} 