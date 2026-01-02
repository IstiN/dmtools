package com.github.istin.dmtools.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Value("${jwt.refresh.expiration:2592000000}")
    private long jwtRefreshExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(String email, String userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.debug("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.debug("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String generateJwtTokenCustom(String email, String userId, String secret, int expirationMs) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateJwtTokenCustom(String token, String secret) {
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromJwtTokenCustom(String token, String secret) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String getUserIdFromJwtTokenCustom(String token, String secret) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("userId", String.class);
    }

    /**
     * Generates a refresh token JWT with longer expiration time.
     *
     * @param email User email
     * @param userId User ID
     * @return Refresh token JWT string
     */
    public String generateRefreshToken(String email, String userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a refresh token. Refresh tokens must be valid and not expired.
     *
     * @param token Refresh token to validate
     * @return true if token is valid and not expired, false otherwise
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Verify it's a refresh token
            String type = claims.get("type", String.class);
            if (type == null || !"refresh".equals(type)) {
                logger.debug("Token is not a refresh token (missing or invalid type claim)");
                return false;
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            logger.debug("Refresh token is expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.debug("Invalid refresh token: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.debug("Unsupported refresh token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.debug("Refresh token claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error validating refresh token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Extracts claims from a refresh token, even if it's expired.
     * This is useful for extracting user information from expired tokens.
     *
     * @param token Refresh token
     * @return Claims object containing token information
     * @throws JwtException if token is invalid or cannot be parsed
     */
    public Claims getClaimsFromRefreshToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Even if expired, we can still extract claims
            return e.getClaims();
        } catch (Exception e) {
            throw new JwtException("Failed to extract claims from refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts user email from a refresh token.
     *
     * @param token Refresh token
     * @return User email
     */
    public String getEmailFromRefreshToken(String token) {
        Claims claims = getClaimsFromRefreshToken(token);
        return claims.getSubject();
    }

    /**
     * Extracts user ID from a refresh token.
     *
     * @param token Refresh token
     * @return User ID
     */
    public String getUserIdFromRefreshToken(String token) {
        Claims claims = getClaimsFromRefreshToken(token);
        return claims.get("userId", String.class);
    }
} 