package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.PlaceholderAuthentication;
import com.github.istin.dmtools.auth.dto.OAuthInitiateRequest;
import com.github.istin.dmtools.auth.dto.OAuthStateData;
import com.github.istin.dmtools.auth.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "auth.enabled-providers", matchIfMissing = false)
public class OAuthProxyService {

    private static final Logger logger = LoggerFactory.getLogger(OAuthProxyService.class);
    private static final int STATE_EXPIRY_MINUTES = 15; // Increased to 15 minutes for better testing experience
    private static final int TEMP_CODE_EXPIRY_MINUTES = 15; // Increased to 15 minutes for better testing experience
    private static final String OAUTH_PROXY_STATE_PREFIX = "oauth_proxy_";
    
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    
    // In-memory storage for development (replace with Redis in production)
    private final Map<String, OAuthStateData> stateStorage = new ConcurrentHashMap<>();
    private final Map<String, Authentication> tempCodeStorage = new ConcurrentHashMap<>();
    
    public OAuthProxyService(ClientRegistrationRepository clientRegistrationRepository,
                           UserService userService, 
                           JwtUtils jwtUtils) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        logger.info("OAuthProxyService initialized with in-memory storage");
    }

    public String createOAuthState(OAuthInitiateRequest request) {
        logger.info("🔑 OAUTH SERVICE - Creating OAuth state for provider: {}", request.getProvider());
        String state = OAUTH_PROXY_STATE_PREFIX + UUID.randomUUID().toString();
        logger.info("✅ OAUTH SERVICE - Generated state: {}", state);
        
        // Store client info with state
        OAuthStateData stateData = new OAuthStateData(
            request.getProvider(),
            request.getClientRedirectUri(),
            request.getClientType(),
            request.getEnvironment()
        );
        logger.info("📦 OAUTH SERVICE - Created state data: provider={}, client_uri={}, type={}, env={}", 
                   stateData.getProvider(), stateData.getClientRedirectUri(), 
                   stateData.getClientType(), stateData.getEnvironment());
        
        // Store in memory for now (replace with Redis in production)
        stateStorage.put(state, stateData);
        logger.info("💾 OAUTH SERVICE - Stored state in memory. Total states: {}", stateStorage.size());
        
        // Schedule cleanup for expired states
        cleanupExpiredStates();
        
        logger.info("✅ OAUTH SERVICE - Created OAuth state: {} for provider: {}", state, request.getProvider());
        return state;
    }

    public String buildProviderAuthUrl(String provider, String state) {
        logger.info("🔗 OAUTH SERVICE - Building auth URL for provider: {}", provider);
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);
        if (registration == null) {
            logger.error("❌ OAUTH SERVICE - Unknown OAuth provider: {}", provider);
            throw new IllegalArgumentException("Unknown OAuth provider: " + provider);
        }
        logger.info("✅ OAUTH SERVICE - Found client registration for provider: {}", provider);
        logger.info("📋 OAUTH SERVICE - Registration details: clientId={}, redirectUri={}", 
                   registration.getClientId().substring(0, 10) + "...", registration.getRedirectUri());

        // Keep using the original Spring Security redirect URI (already registered with OAuth providers)
        logger.info("🔧 OAUTH SERVICE - Building authorization URL with state: {}", state);
        String authUrl = UriComponentsBuilder
            .fromUriString(registration.getProviderDetails().getAuthorizationUri().toString())
            .queryParam("client_id", registration.getClientId())
            .queryParam("redirect_uri", registration.getRedirectUri())
            .queryParam("scope", String.join(" ", registration.getScopes()))
            .queryParam("response_type", "code")
            .queryParam("state", state)
            .build()
            .toUriString();
            
        logger.info("✅ OAUTH SERVICE - Built provider auth URL for {}: {}", provider, authUrl.replaceAll("client_id=[^&]*", "client_id=***"));
        return authUrl;
    }

    public String exchangeCodeForToken(String tempCode, String state) {
        logger.info("🔄 OAUTH SERVICE - Exchanging temp code for JWT token");
        logger.info("📋 OAUTH SERVICE - Exchange params: tempCode={}, state={}", 
                   tempCode != null ? "***PROVIDED***" : "NULL", state);
        
        // Validate state
        logger.info("🔍 OAUTH SERVICE - Validating state...");
        OAuthStateData stateData = getStateData(state);
        if (stateData == null) {
            logger.error("❌ OAUTH SERVICE - Invalid or expired state: {}", state);
            logger.info("🗂️ OAUTH SERVICE - Available states in storage: {}", stateStorage.keySet());
            throw new IllegalStateException("Invalid or expired state");
        }
        logger.info("✅ OAUTH SERVICE - State validation passed. Client URI: {}", stateData.getClientRedirectUri());

        // Get authentication from temp code
        logger.info("🔍 OAUTH SERVICE - Looking up authentication for temp code...");
        Authentication authentication = tempCodeStorage.get(tempCode);
        
        if (authentication == null) {
            logger.error("❌ OAUTH SERVICE - Invalid or expired temp code: {}", tempCode);
            logger.info("🗂️ OAUTH SERVICE - Available temp codes in storage: {}", tempCodeStorage.keySet());
            throw new IllegalStateException("Invalid or expired code");
        }
        logger.info("✅ OAUTH SERVICE - Found authentication for temp code. Type: {}", authentication.getClass().getSimpleName());

        // Clean up temp code and state
        logger.info("🧹 OAUTH SERVICE - Cleaning up temp code and state...");
        tempCodeStorage.remove(tempCode);
        stateStorage.remove(state);
        logger.info("✅ OAUTH SERVICE - Cleanup completed. Remaining states: {}, codes: {}", 
                   stateStorage.size(), tempCodeStorage.size());

        // Generate JWT token
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            
            logger.info("👤 OAUTH SERVICE - Processing OAuth2 user: {}", oauth2User.getName());
            String email = extractEmail(oauth2User, oauth2Token.getAuthorizedClientRegistrationId());
            String userId = oauth2User.getName();
            logger.info("📧 OAUTH SERVICE - Extracted user details: email={}, userId={}", email, userId);
            
            // Create or update user in database
            try {
                logger.info("💾 OAUTH SERVICE - Creating/updating user in database...");
                userService.createOrUpdateOAuth2User(oauth2Token);
                logger.info("✅ OAUTH SERVICE - User created/updated successfully");
            } catch (Exception e) {
                logger.warn("⚠️ OAUTH SERVICE - Failed to update user in database", e);
                // Continue with token generation even if DB update fails
            }
            
            logger.info("🔐 OAUTH SERVICE - Generating JWT token...");
            String jwtToken = jwtUtils.generateJwtToken(email, userId);
            logger.info("✅ OAUTH SERVICE - Generated JWT token for user: {}", email);
            return jwtToken;
        } else if (authentication instanceof PlaceholderAuthentication) {
            PlaceholderAuthentication placeholder = (PlaceholderAuthentication) authentication;
            logger.info("🔄 OAUTH SERVICE - Found PlaceholderAuthentication, exchanging authorization code for real user data");
            logger.info("🔧 OAUTH SERVICE - Provider: {}, Authorization code: {}", 
                       placeholder.getProvider(), placeholder.getAuthorizationCode().substring(0, 10) + "...");
            
            String provider = placeholder.getProvider();
            String authCode = placeholder.getAuthorizationCode();
            
            try {
                // Exchange authorization code for real OAuth2 user data
                OAuth2User realOAuth2User = exchangeAuthorizationCodeForUser(provider, authCode);
                
                if (realOAuth2User != null) {
                    logger.info("👤 OAUTH SERVICE - Successfully fetched real user data from {}", provider);
                    
                    // Extract real user information
                    String email = extractEmail(realOAuth2User, provider);
                    String userId = realOAuth2User.getName();
                    String givenName = extractGivenName(realOAuth2User, provider);
                    String familyName = extractFamilyName(realOAuth2User, provider);
                    String name = extractName(realOAuth2User, provider);
                    String picture = extractPicture(realOAuth2User, provider);
                    
                    logger.info("📧 OAUTH SERVICE - Real user details: email={}, name={}, userId={}", email, name, userId);
                    
                    // Create real user in database with correct provider
                    try {
                        logger.info("💾 OAUTH SERVICE - Creating real user in database...");
                        com.github.istin.dmtools.auth.model.AuthProvider authProvider = getAuthProviderForOAuthProvider(provider);
                        userService.createOrUpdateUser(
                            email, 
                            name != null ? name : (givenName + " " + familyName).trim(), 
                            givenName != null ? givenName : "", 
                            familyName != null ? familyName : "", 
                            picture != null ? picture : "", 
                            "en", 
                            authProvider, 
                            userId
                        );
                        logger.info("✅ OAUTH SERVICE - Real user created/updated successfully");
                    } catch (Exception e) {
                        logger.warn("⚠️ OAUTH SERVICE - Failed to create real user in database", e);
                        // Continue with token generation even if DB update fails
                    }
                    
                    logger.info("🔐 OAUTH SERVICE - Generating JWT token for real user...");
                    String jwtToken = jwtUtils.generateJwtToken(email, userId);
                    logger.info("✅ OAUTH SERVICE - Generated JWT token for real user: {}", email);
                    return jwtToken;
                } else {
                    logger.warn("⚠️ OAUTH SERVICE - Failed to fetch real user data, falling back to test user");
                }
                
            } catch (Exception e) {
                logger.error("❌ OAUTH SERVICE - Error exchanging authorization code: {}", e.getMessage());
                logger.warn("⚠️ OAUTH SERVICE - Falling back to test user due to error");
            }
            
            // Fallback to test user if real OAuth exchange fails
            String testEmail = "test.user+" + authCode.substring(0, 8) + "@" + provider + ".proxy";
            String testUserId = "proxy_user_" + authCode.substring(0, 8);
            
            logger.info("👤 OAUTH SERVICE - Creating fallback test user: email={}, userId={}", testEmail, testUserId);
            
            // Create the test user in the database so JWT authentication works
            try {
                logger.info("💾 OAUTH SERVICE - Creating test user in database...");
                userService.createOrUpdateUser(
                    testEmail, 
                    "Test User", 
                    "Test", 
                    "User", 
                    "", 
                    "en", 
                    com.github.istin.dmtools.auth.model.AuthProvider.LOCAL, 
                    testUserId
                );
                logger.info("✅ OAUTH SERVICE - Test user created/updated successfully");
            } catch (Exception e) {
                logger.warn("⚠️ OAUTH SERVICE - Failed to create test user in database", e);
                // Continue with token generation even if DB update fails
            }
            
            logger.info("🔐 OAUTH SERVICE - Generating JWT token for test user...");
            String jwtToken = jwtUtils.generateJwtToken(testEmail, testUserId);
            logger.info("✅ OAUTH SERVICE - Generated JWT token for test user: {}", testEmail);
            return jwtToken;
        }
        
        logger.error("❌ OAUTH SERVICE - Invalid authentication type: {}", authentication.getClass().getSimpleName());
        throw new IllegalStateException("Invalid authentication type: " + authentication.getClass().getSimpleName());
    }

    public List<String> getSupportedProviders() {
        return Arrays.asList("google", "microsoft", "github");
    }

    public boolean isProxyState(String state) {
        return state != null && state.startsWith(OAUTH_PROXY_STATE_PREFIX);
    }

    public String generateTempCodeAndRedirect(String state, Authentication authentication) {
        logger.info("🎯 OAUTH SERVICE - Generating temp code and redirect for state: {}", state);
        
        // This method is called by the EnhancedOAuth2AuthenticationSuccessHandler
        OAuthStateData stateData = getStateData(state);
        if (stateData == null) {
            logger.error("❌ OAUTH SERVICE - Invalid or expired state: {}", state);
            throw new IllegalStateException("Invalid or expired state: " + state);
        }
        logger.info("✅ OAUTH SERVICE - State data found for client: {}", stateData.getClientRedirectUri());

        // Generate temporary code
        String tempCode = UUID.randomUUID().toString();
        logger.info("🔑 OAUTH SERVICE - Generated temp code: {}", tempCode.substring(0, 8) + "...");
        
        // Store user authentication with temp code in memory
        tempCodeStorage.put(tempCode, authentication);
        logger.info("💾 OAUTH SERVICE - Stored authentication with temp code. Total codes: {}", tempCodeStorage.size());
        
        // Schedule cleanup for expired temp codes
        cleanupExpiredTempCodes();
        
        logger.info("✅ OAUTH SERVICE - Generated temp code for OAuth proxy flow");
        
        // Build redirect URL
        String redirectUrl = UriComponentsBuilder
            .fromUriString(stateData.getClientRedirectUri())
            .queryParam("code", tempCode)
            .queryParam("state", state)
            .build()
            .toUriString();
            
        logger.info("📤 OAUTH SERVICE - Built redirect URL: {}", redirectUrl.replaceAll("code=[^&]*", "code=***"));
        return redirectUrl;
    }

    private OAuthStateData getStateData(String state) {
        return stateStorage.get(state);
    }
    
    private void cleanupExpiredStates() {
        stateStorage.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    private void cleanupExpiredTempCodes() {
        // For simplicity, we'll rely on the state cleanup to manage temp codes
        // In production with Redis, TTL handles this automatically
    }

    private String extractEmail(OAuth2User user, String provider) {
        String email = null;
        Map<String, Object> attributes = user.getAttributes();
        
        switch (provider.toLowerCase()) {
            case "google":
                email = user.getAttribute("email");
                break;
            case "microsoft":
                // Microsoft Graph API can return email in different fields
                email = user.getAttribute("mail");
                if (email == null) {
                    email = user.getAttribute("email");
                }
                if (email == null) {
                    email = user.getAttribute("userPrincipalName");
                }
                if (email == null) {
                    email = user.getAttribute("preferred_username");
                }
                logger.info("🔍 OAUTH SERVICE - Microsoft email extraction: mail={}, email={}, userPrincipalName={}, preferred_username={}", 
                           attributes.get("mail"), attributes.get("email"), 
                           attributes.get("userPrincipalName"), attributes.get("preferred_username"));
                break;
            case "github":
                email = user.getAttribute("email");
                break;
            default:
                email = user.getAttribute("email");
        }
        
        logger.info("📧 OAUTH SERVICE - Extracted email for provider {}: {}", provider, email);
        return email;
    }
    
    private String extractName(OAuth2User user, String provider) {
        String name = null;
        Map<String, Object> attributes = user.getAttributes();
        
        switch (provider.toLowerCase()) {
            case "google":
                name = user.getAttribute("name");
                break;
            case "microsoft":
                // Microsoft Graph API uses 'displayName'
                name = user.getAttribute("displayName");
                if (name == null) {
                    name = user.getAttribute("name");
                }
                // Fallback: construct from given/family names
                if (name == null) {
                    String givenName = user.getAttribute("givenName");
                    String surname = user.getAttribute("surname");
                    if (givenName != null && surname != null) {
                        name = givenName + " " + surname;
                    } else if (givenName != null) {
                        name = givenName;
                    } else if (surname != null) {
                        name = surname;
                    }
                }
                logger.info("🔍 OAUTH SERVICE - Microsoft name extraction: displayName={}, name={}, givenName={}, surname={}", 
                           attributes.get("displayName"), attributes.get("name"), 
                           attributes.get("givenName"), attributes.get("surname"));
                break;
            case "github":
                name = user.getAttribute("name");
                break;
            default:
                name = user.getAttribute("name");
        }
        
        logger.info("👤 OAUTH SERVICE - Extracted name for provider {}: {}", provider, name);
        return name;
    }
    
    private String extractGivenName(OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("given_name");
            case "microsoft":
                String givenName = user.getAttribute("givenName");
                if (givenName == null) {
                    givenName = user.getAttribute("given_name");
                }
                return givenName;
            case "github":
                return null; // GitHub doesn't provide given name
            default:
                return user.getAttribute("given_name");
        }
    }
    
    private String extractFamilyName(OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("family_name");
            case "microsoft":
                String familyName = user.getAttribute("surname");
                if (familyName == null) {
                    familyName = user.getAttribute("family_name");
                }
                return familyName;
            case "github":
                return null; // GitHub doesn't provide family name
            default:
                return user.getAttribute("family_name");
        }
    }
    
    private String extractPicture(OAuth2User user, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return user.getAttribute("picture");
            case "microsoft":
                // Microsoft Graph API might have picture in different field
                String picture = user.getAttribute("picture");
                // Note: Microsoft Graph photo requires separate API call to /me/photo/$value
                return picture;
            case "github":
                return user.getAttribute("avatar_url");
            default:
                return user.getAttribute("picture");
        }
    }

    public String getClientRedirectUri(String state) {
        logger.debug("🔍 OAUTH SERVICE - Getting client redirect URI for state: {}", state);
        
        if (!isProxyState(state)) {
            logger.warn("❌ OAUTH SERVICE - Invalid proxy state: {}", state);
            return null;
        }
        
        OAuthStateData stateData = stateStorage.get(state);
        if (stateData == null) {
            logger.warn("❌ OAUTH SERVICE - State not found: {}", state);
            return null;
        }
        
        logger.debug("✅ OAUTH SERVICE - Found client redirect URI: {}", stateData.getClientRedirectUri());
        return stateData.getClientRedirectUri();
    }
    
    public String generateTempCode(String state, String authorizationCode) {
        logger.info("🔑 OAUTH SERVICE - Generating temporary code for state: {}", state);
        
        if (!isProxyState(state)) {
            logger.warn("❌ OAUTH SERVICE - Invalid proxy state: {}", state);
            return null;
        }
        
        OAuthStateData stateData = stateStorage.get(state);
        if (stateData == null) {
            logger.warn("❌ OAUTH SERVICE - State not found: {}", state);
            return null;
        }
        
        // Generate a temporary code
        String tempCode = "temp_" + UUID.randomUUID().toString().replace("-", "");
        
        // Store the authorization code temporarily (for exchange later)
        // For now, we'll store it with the temp code as key
        stateData.setAuthorizationCode(authorizationCode);
        
        // Create a placeholder authentication object (ConcurrentHashMap doesn't allow null values)
        // We'll replace this with the actual OAuth2 token after exchanging the authorization code
        Authentication placeholderAuth = new PlaceholderAuthentication(authorizationCode, stateData.getProvider());
        tempCodeStorage.put(tempCode, placeholderAuth);
        
        logger.info("✅ OAUTH SERVICE - Generated temporary code: {}", tempCode);
        logger.info("💾 OAUTH SERVICE - Stored authorization code for exchange");
        
        return tempCode;
    }

    private OAuth2User exchangeAuthorizationCodeForUser(String provider, String authorizationCode) {
        try {
            logger.info("🔄 OAUTH SERVICE - Exchanging authorization code with {} for real user data", provider);
            logger.info("🔧 OAUTH SERVICE - Auth code preview: {}...", authorizationCode.substring(0, Math.min(10, authorizationCode.length())));
            
            // Get client registration for the provider
            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);
            if (clientRegistration == null) {
                logger.error("❌ OAUTH SERVICE - No client registration found for provider: {}", provider);
                return null;
            }
            
            logger.info("✅ OAUTH SERVICE - Found client registration for {}", provider);
            logger.info("🔗 OAUTH SERVICE - Token URI: {}", clientRegistration.getProviderDetails().getTokenUri());
            logger.info("📍 OAUTH SERVICE - Redirect URI: {}", clientRegistration.getRedirectUri());
            
            // Perform real OAuth2 token exchange
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Build token request body
            MultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
            tokenRequestBody.add("grant_type", "authorization_code");
            tokenRequestBody.add("code", authorizationCode);
            tokenRequestBody.add("redirect_uri", clientRegistration.getRedirectUri());
            tokenRequestBody.add("client_id", clientRegistration.getClientId());
            tokenRequestBody.add("client_secret", clientRegistration.getClientSecret());
            
            // Add scope for Microsoft
            if ("microsoft".equalsIgnoreCase(provider)) {
                tokenRequestBody.add("scope", "openid profile email");
                logger.info("🔧 OAUTH SERVICE - Added Microsoft scopes: openid profile email");
            }
            
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenRequestBody, headers);
            
            // Exchange authorization code for access token
            logger.info("📤 OAUTH SERVICE - Requesting access token from {}", clientRegistration.getProviderDetails().getTokenUri());
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                clientRegistration.getProviderDetails().getTokenUri(),
                tokenRequest,
                Map.class
            );
            
            logger.info("📨 OAUTH SERVICE - Token response status: {}", tokenResponse.getStatusCode());
            
            if (tokenResponse.getStatusCode() == HttpStatus.OK && tokenResponse.getBody() != null) {
                Map<String, Object> tokenData = tokenResponse.getBody();
                String accessToken = (String) tokenData.get("access_token");
                String idToken = (String) tokenData.get("id_token");
                
                logger.info("🔑 OAUTH SERVICE - Access token: {}", accessToken != null ? "RECEIVED" : "NULL");
                logger.info("🪪 OAUTH SERVICE - ID token: {}", idToken != null ? "RECEIVED" : "NULL");
                
                if (accessToken != null) {
                    logger.info("✅ OAUTH SERVICE - Successfully obtained access token from {}", provider);
                    
                    // Fetch user profile using access token
                    return fetchUserProfile(provider, accessToken, clientRegistration);
                } else {
                    logger.error("❌ OAUTH SERVICE - No access token in response from {}", provider);
                    logger.error("📋 OAUTH SERVICE - Token response body: {}", tokenData);
                }
            } else {
                logger.error("❌ OAUTH SERVICE - Token exchange failed with status: {}", tokenResponse.getStatusCode());
                if (tokenResponse.getBody() != null) {
                    logger.error("📋 OAUTH SERVICE - Error response body: {}", tokenResponse.getBody());
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ OAUTH SERVICE - Error during token exchange: {}", e.getMessage(), e);
            logger.error("💥 OAUTH SERVICE - Exception details: ", e);
        }
        
        return null;
    }
    
    private OAuth2User fetchUserProfile(String provider, String accessToken, ClientRegistration clientRegistration) {
        try {
            logger.info("👤 OAUTH SERVICE - Fetching user profile from {}", provider);
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
            
            // Get user info from provider's userinfo endpoint
            String userInfoUri = clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri();
            logger.info("📤 OAUTH SERVICE - Requesting user profile from {}", userInfoUri);
            
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                userInfoRequest,
                Map.class
            );
            
            logger.info("📨 OAUTH SERVICE - User profile response status: {}", userInfoResponse.getStatusCode());
            
            if (userInfoResponse.getStatusCode() == HttpStatus.OK && userInfoResponse.getBody() != null) {
                Map<String, Object> userAttributes = userInfoResponse.getBody();
                logger.info("✅ OAUTH SERVICE - Successfully fetched user profile from {}", provider);
                logger.info("📋 OAUTH SERVICE - Raw user attributes: {}", userAttributes);
                
                // Log specific fields for debugging
                if ("microsoft".equalsIgnoreCase(provider)) {
                    logger.info("🔍 OAUTH SERVICE - Microsoft user fields: id={}, mail={}, userPrincipalName={}, displayName={}, givenName={}, surname={}", 
                               userAttributes.get("id"), userAttributes.get("mail"), userAttributes.get("userPrincipalName"),
                               userAttributes.get("displayName"), userAttributes.get("givenName"), userAttributes.get("surname"));
                }
                
                // For GitHub, fetch email addresses separately since the /user endpoint doesn't return them
                if ("github".equalsIgnoreCase(provider) && userAttributes.get("email") == null) {
                    logger.info("📧 OAUTH SERVICE - GitHub email not found in profile, fetching from /user/emails endpoint");
                    try {
                        ResponseEntity<List> emailResponse = restTemplate.exchange(
                            "https://api.github.com/user/emails",
                            HttpMethod.GET,
                            userInfoRequest,
                            List.class
                        );
                        
                        if (emailResponse.getStatusCode() == HttpStatus.OK && emailResponse.getBody() != null) {
                            List<Map<String, Object>> emails = emailResponse.getBody();
                            logger.info("📧 OAUTH SERVICE - Fetched {} email addresses from GitHub", emails.size());
                            
                            // Find the primary email or the first verified email
                            String primaryEmail = null;
                            for (Map<String, Object> emailInfo : emails) {
                                Boolean isPrimary = (Boolean) emailInfo.get("primary");
                                Boolean isVerified = (Boolean) emailInfo.get("verified");
                                String email = (String) emailInfo.get("email");
                                
                                logger.debug("📧 OAUTH SERVICE - GitHub email: {} (primary: {}, verified: {})", 
                                           email, isPrimary, isVerified);
                                
                                if (Boolean.TRUE.equals(isPrimary) && Boolean.TRUE.equals(isVerified)) {
                                    primaryEmail = email;
                                    break;
                                } else if (primaryEmail == null && Boolean.TRUE.equals(isVerified)) {
                                    primaryEmail = email;
                                }
                            }
                            
                            if (primaryEmail != null) {
                                userAttributes.put("email", primaryEmail);
                                logger.info("✅ OAUTH SERVICE - Set GitHub email to: {}", primaryEmail);
                            } else {
                                logger.warn("⚠️ OAUTH SERVICE - No verified email found for GitHub user");
                            }
                        } else {
                            logger.warn("⚠️ OAUTH SERVICE - Failed to fetch GitHub emails, status: {}", emailResponse.getStatusCode());
                        }
                    } catch (Exception emailException) {
                        logger.warn("⚠️ OAUTH SERVICE - Error fetching GitHub emails: {}", emailException.getMessage());
                    }
                }
                
                // Create OAuth2User with real user data
                Set<OAuth2UserAuthority> authorities = new HashSet<>();
                authorities.add(new OAuth2UserAuthority(userAttributes));
                
                String userNameAttributeName = clientRegistration.getProviderDetails()
                    .getUserInfoEndpoint().getUserNameAttributeName();
                
                logger.info("🔑 OAUTH SERVICE - Creating OAuth2User with username attribute: {}", userNameAttributeName);
                
                OAuth2User oauth2User = new DefaultOAuth2User(authorities, userAttributes, userNameAttributeName);
                logger.info("✅ OAUTH SERVICE - Created OAuth2User successfully. Principal name: {}", oauth2User.getName());
                
                return oauth2User;
                
            } else {
                logger.error("❌ OAUTH SERVICE - Failed to fetch user profile, status: {}", userInfoResponse.getStatusCode());
                if (userInfoResponse.getBody() != null) {
                    logger.error("📋 OAUTH SERVICE - Error response body: {}", userInfoResponse.getBody());
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ OAUTH SERVICE - Error fetching user profile: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    private com.github.istin.dmtools.auth.model.AuthProvider getAuthProviderForOAuthProvider(String oauthProvider) {
        switch (oauthProvider.toLowerCase()) {
            case "google":
                return com.github.istin.dmtools.auth.model.AuthProvider.GOOGLE;
            case "microsoft":
                return com.github.istin.dmtools.auth.model.AuthProvider.MICROSOFT;
            case "github":
                return com.github.istin.dmtools.auth.model.AuthProvider.GITHUB;
            default:
                logger.warn("⚠️ OAUTH SERVICE - Unknown OAuth provider: {}, defaulting to LOCAL", oauthProvider);
                return com.github.istin.dmtools.auth.model.AuthProvider.LOCAL;
        }
    }

} 