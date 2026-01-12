package com.github.istin.dmtools.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Generates signed JWT client secrets required by Apple's Sign in with Apple service.
 * Apple requires a signed JWT as the client_secret instead of a static string.
 */
@Component
public class AppleSecretGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AppleSecretGenerator.class);

    @Value("${apple.team-id:}")
    private String teamId;

    @Value("${apple.key-id:}")
    private String keyId;

    @Value("${apple.client-id:}")
    private String clientId;

    @Value("${apple.private-key-path:}")
    private String privateKeyPath;

    @Value("${apple.private-key-content:}")
    private String privateKeyContent;

    /**
     * Generates a signed JWT that can be used as Apple's client_secret.
     * The JWT is valid for 6 months as per Apple's requirements.
     *
     * @return Signed JWT string
     * @throws Exception if the private key cannot be loaded or JWT generation fails
     */
    public String generateClientSecret() throws Exception {
        if (teamId == null || teamId.trim().isEmpty()) {
            throw new IllegalStateException("Apple Team ID is not configured. Set 'apple.team-id' property.");
        }
        if (keyId == null || keyId.trim().isEmpty()) {
            throw new IllegalStateException("Apple Key ID is not configured. Set 'apple.key-id' property.");
        }
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalStateException("Apple Client ID is not configured. Set 'apple.client-id' property.");
        }

        PrivateKey privateKey = loadPrivateKey();
        if (privateKey == null) {
            throw new IllegalStateException("Apple private key could not be loaded. Check 'apple.private-key-path' or 'apple.private-key-content' properties.");
        }

        // JWT expires in 6 months (Apple's maximum allowed)
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 15777000L * 1000L); // 6 months

        logger.info("🔐 Generating Apple client secret JWT with key ID: {}", keyId);

        String jwt = Jwts.builder()
                .setHeaderParam("alg", "ES256")
                .setHeaderParam("kid", keyId)
                .setIssuer(teamId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();

        logger.info("✅ Generated Apple client secret JWT successfully");
        return jwt;
    }

    /**
     * Loads the private key from either the file path or environment variable content.
     *
     * @return PrivateKey object
     * @throws Exception if key loading fails
     */
    private PrivateKey loadPrivateKey() throws Exception {
        String keyContent = null;

        // Try to load from environment variable first (preferred for CI/CD)
        if (privateKeyContent != null && !privateKeyContent.trim().isEmpty()) {
            logger.info("🔑 Loading Apple private key from environment variable");
            keyContent = privateKeyContent;
        }
        // Fall back to file path
        else if (privateKeyPath != null && !privateKeyPath.trim().isEmpty()) {
            logger.info("🔑 Loading Apple private key from file: {}", privateKeyPath);
            try {
                keyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
            } catch (IOException e) {
                logger.error("❌ Failed to read Apple private key from file: {}", privateKeyPath, e);
                throw new IllegalStateException("Failed to read Apple private key from file: " + privateKeyPath, e);
            }
        }

        if (keyContent == null || keyContent.trim().isEmpty()) {
            logger.warn("⚠️ No Apple private key content found. Set either 'apple.private-key-content' or 'apple.private-key-path'");
            return null;
        }

        try {
            // Remove header, footer, and whitespace
            String privateKeyPEM = keyContent
                    .replaceAll("\\n", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            logger.error("❌ Failed to parse Apple private key", e);
            throw new IllegalStateException("Failed to parse Apple private key", e);
        }
    }
}