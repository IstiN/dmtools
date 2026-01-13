package com.github.istin.dmtools.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verifies Apple Sign in with Apple identity tokens
 */
@Service
public class AppleTokenVerifier {

    private static final Logger logger = LoggerFactory.getLogger(AppleTokenVerifier.class);

    @Value("${apple.team-id:}")
    private String expectedTeamId;

    @Value("${apple.client-id:}")
    private String expectedClientId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cache for Apple public keys
    private final Map<String, RSAPublicKey> keyCache = new ConcurrentHashMap<>();

    /**
     * Verifies an Apple identity token and extracts user information
     *
     * @param identityToken The JWT identity token from Apple
     * @return AppleUserInfo containing verified user data
     * @throws Exception if verification fails
     */
    public AppleUserInfo verifyIdentityToken(String identityToken) throws Exception {
        logger.info("🍎 Verifying Apple identity token");

        try {
            // Decode the JWT without verification first to get the key ID
            DecodedJWT decodedJWT = JWT.decode(identityToken);
            String keyId = decodedJWT.getKeyId();

            logger.info("🔑 Apple token key ID: {}", keyId);

            // Get the public key for this key ID
            RSAPublicKey publicKey = getApplePublicKey(keyId);
            if (publicKey == null) {
                throw new Exception("Unable to find Apple public key for key ID: " + keyId);
            }

            // Create algorithm for verification
            Algorithm algorithm = Algorithm.RSA256(publicKey);

            // Verify the JWT
            DecodedJWT verifiedJWT = JWT.require(algorithm)
                .withIssuer("https://appleid.apple.com")
                .withAudience(expectedClientId)
                .build()
                .verify(identityToken);

            logger.info("✅ Apple identity token verified successfully");

            // Extract user information
            return extractUserInfo(verifiedJWT);

        } catch (JWTVerificationException e) {
            logger.error("❌ Apple identity token verification failed: {}", e.getMessage());
            throw new Exception("Invalid Apple identity token: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Error verifying Apple identity token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves Apple public key by key ID
     */
    private RSAPublicKey getApplePublicKey(String keyId) throws Exception {
        // Check cache first
        if (keyCache.containsKey(keyId)) {
            return keyCache.get(keyId);
        }

        logger.info("📡 Fetching Apple public keys from https://appleid.apple.com/auth/keys");

        try {
            String response = restTemplate.getForObject("https://appleid.apple.com/auth/keys", String.class);
            JsonNode keys = objectMapper.readTree(response);

            for (JsonNode key : keys.get("keys")) {
                if (keyId.equals(key.get("kid").asText())) {
                    RSAPublicKey publicKey = createPublicKey(key);
                    keyCache.put(keyId, publicKey);
                    logger.info("✅ Apple public key loaded for key ID: {}", keyId);
                    return publicKey;
                }
            }

            logger.warn("⚠️ Apple public key not found for key ID: {}", keyId);
            return null;

        } catch (Exception e) {
            logger.error("❌ Failed to fetch Apple public keys: {}", e.getMessage());
            throw new Exception("Failed to fetch Apple public keys", e);
        }
    }

    /**
     * Creates RSA public key from Apple's JWK format
     */
    private RSAPublicKey createPublicKey(JsonNode key) throws Exception {
        try {
            // Apple's keys are in JWK format
            String n = key.get("n").asText();
            String e = key.get("e").asText();

            // Decode base64url
            byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return (RSAPublicKey) factory.generatePublic(spec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            logger.error("❌ Failed to create Apple public key: {}", ex.getMessage());
            throw new Exception("Failed to create Apple public key", ex);
        }
    }

    /**
     * Extracts user information from verified JWT
     */
    private AppleUserInfo extractUserInfo(DecodedJWT jwt) {
        String subject = jwt.getSubject(); // Apple user ID
        String email = jwt.getClaim("email").asString();
        String emailVerified = jwt.getClaim("email_verified").asString();
        String isPrivateEmail = jwt.getClaim("is_private_email").asString();

        logger.info("👤 Extracted Apple user info - subject: {}, email: {}, verified: {}, private: {}",
                   subject, email, emailVerified, isPrivateEmail);

        return new AppleUserInfo(subject, email, "true".equals(emailVerified), "true".equals(isPrivateEmail));
    }

    /**
     * Data class for Apple user information
     */
    public static class AppleUserInfo {
        private final String userId;
        private final String email;
        private final boolean emailVerified;
        private final boolean isPrivateEmail;

        public AppleUserInfo(String userId, String email, boolean emailVerified, boolean isPrivateEmail) {
            this.userId = userId;
            this.email = email;
            this.emailVerified = emailVerified;
            this.isPrivateEmail = isPrivateEmail;
        }

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public boolean isEmailVerified() { return emailVerified; }
        public boolean isPrivateEmail() { return isPrivateEmail; }
    }
}