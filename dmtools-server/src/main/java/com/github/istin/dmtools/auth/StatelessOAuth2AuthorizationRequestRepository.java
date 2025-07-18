package com.github.istin.dmtools.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Stateless OAuth2 authorization request repository that stores requests in encrypted cookies
 * instead of sessions. This solves the Cloud Run production issue where sessions don't
 * persist across container instances.
 */
@Component
public class StatelessOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(StatelessOAuth2AuthorizationRequestRepository.class);
    private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final String OAUTH2_REDIRECT_URI_PARAM_COOKIE_NAME = "oauth2_redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3 minutes

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        logger.info("🔍 STATELESS OAUTH - Loading authorization request for state: {}", state);
        
        if (state == null) {
            logger.debug("🔍 STATELESS OAUTH - No state parameter found in request");
            return null;
        }
        
        OAuth2AuthorizationRequest authRequest = getOAuth2AuthorizationRequestFromCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        
        if (authRequest != null && state.equals(authRequest.getState())) {
            logger.info("✅ STATELESS OAUTH - Found matching authorization request for state: {}", state);
            return authRequest;
        } else if (authRequest != null) {
            logger.warn("⚠️ STATELESS OAUTH - State mismatch. Cookie state: {}, Request state: {}", 
                       authRequest.getState(), state);
        } else {
            logger.warn("⚠️ STATELESS OAUTH - No authorization request found in cookie for state: {}", state);
        }
        
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            logger.info("🗑️ STATELESS OAUTH - Removing authorization request cookies");
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            deleteCookie(request, response, OAUTH2_REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        logger.info("💾 STATELESS OAUTH - Saving authorization request with state: {}", authorizationRequest.getState());
        
        try {
            String cookieValue = encryptAndEncode(SerializationUtils.serialize(authorizationRequest));
            addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, cookieValue, COOKIE_EXPIRE_SECONDS);
            
            String redirectUriAfterLogin = request.getParameter("redirect_uri");
            if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isBlank()) {
                addCookie(response, OAUTH2_REDIRECT_URI_PARAM_COOKIE_NAME, 
                         encryptAndEncode(redirectUriAfterLogin.getBytes(StandardCharsets.UTF_8)), COOKIE_EXPIRE_SECONDS);
            }
            
            logger.info("✅ STATELESS OAUTH - Authorization request saved successfully");
        } catch (Exception e) {
            logger.error("❌ STATELESS OAUTH - Failed to save authorization request", e);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        String state = request.getParameter("state");
        logger.info("🗑️ STATELESS OAUTH - Removing authorization request for state: {}", state);
        
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        
        if (authRequest != null) {
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            deleteCookie(request, response, OAUTH2_REDIRECT_URI_PARAM_COOKIE_NAME);
            logger.info("✅ STATELESS OAUTH - Authorization request removed successfully");
        } else {
            logger.debug("🔍 STATELESS OAUTH - No authorization request found to remove");
        }
        
        return authRequest;
    }

    private OAuth2AuthorizationRequest getOAuth2AuthorizationRequestFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            logger.debug("🔍 STATELESS OAUTH - No cookies found in request");
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                try {
                    logger.debug("🔍 STATELESS OAUTH - Found cookie: {}", cookieName);
                    String decryptedValue = decodeAndDecrypt(cookie.getValue());
                    OAuth2AuthorizationRequest authRequest = (OAuth2AuthorizationRequest) SerializationUtils.deserialize(decryptedValue.getBytes(StandardCharsets.ISO_8859_1));
                    logger.debug("✅ STATELESS OAUTH - Successfully deserialized authorization request");
                    return authRequest;
                } catch (Exception e) {
                    logger.error("❌ STATELESS OAUTH - Failed to deserialize authorization request from cookie", e);
                    return null;
                }
            }
        }
        
        logger.debug("🔍 STATELESS OAUTH - Cookie not found: {}", cookieName);
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        if (isProductionEnvironment()) {
            // Use Set-Cookie header for production with all security attributes
            String cookieValue = String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=Lax", 
                                              name, value, maxAge);
            response.addHeader("Set-Cookie", cookieValue);
            logger.debug("🔒 STATELESS OAUTH - Added secure cookie: {} (production)", name);
        } else {
            // Use Cookie object for development
            Cookie cookie = new Cookie(name, value);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(maxAge);
            response.addCookie(cookie);
            logger.debug("🔓 STATELESS OAUTH - Added non-secure cookie: {} (development)", name);
        }
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    if (isProductionEnvironment()) {
                        // Use Set-Cookie header for production
                        String cookieValue = String.format("%s=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=Lax", name);
                        response.addHeader("Set-Cookie", cookieValue);
                        logger.debug("🗑️ STATELESS OAUTH - Deleted secure cookie: {} (production)", name);
                    } else {
                        // Use Cookie object for development
                        cookie.setValue("");
                        cookie.setPath("/");
                        cookie.setHttpOnly(true);
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                        logger.debug("🗑️ STATELESS OAUTH - Deleted non-secure cookie: {} (development)", name);
                    }
                    break;
                }
            }
        }
    }

    private String encryptAndEncode(byte[] data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(getKey(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data);
        return Base64.getUrlEncoder().encodeToString(encrypted);
    }

    private String decodeAndDecrypt(String encryptedData) throws Exception {
        byte[] decodedData = Base64.getUrlDecoder().decode(encryptedData);
        SecretKeySpec secretKey = new SecretKeySpec(getKey(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(decodedData);
        return new String(decrypted, StandardCharsets.ISO_8859_1);
    }

    private byte[] getKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(hash, 16); // Use only first 128 bits for AES-128
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    private boolean isProductionEnvironment() {
        return "prod".equals(activeProfile) || "production".equals(activeProfile);
    }
} 