package com.github.istin.dmtools.auth;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private final String testSecret = "testSecretKeyMustBeLongEnoughForHmacSha256Algorithm";
    private final int testExpirationMs = 60000; // 1 minute
    private final String testEmail = "test@example.com";
    private final String testUserId = "user123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    void generateJwtToken_ShouldCreateValidToken() {
        // Act
        String token = jwtUtils.generateJwtToken(testEmail, testUserId);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals(testEmail, jwtUtils.getUserEmailFromJwtToken(token));
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtils.generateJwtToken(testEmail, testUserId);

        // Act & Assert
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_WithInvalidToken_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken("invalid.token.string"));
    }

    @Test
    void getUserEmailFromJwtToken_ShouldReturnCorrectEmail() {
        // Arrange
        String token = jwtUtils.generateJwtToken(testEmail, testUserId);

        // Act
        String email = jwtUtils.getUserEmailFromJwtToken(token);

        // Assert
        assertEquals(testEmail, email);
    }

    @Test
    void generateJwtTokenCustom_ShouldCreateValidToken() {
        // Act
        String token = jwtUtils.generateJwtTokenCustom(testEmail, testUserId, testSecret, testExpirationMs);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtTokenCustom(token, testSecret));
        assertEquals(testEmail, jwtUtils.getEmailFromJwtTokenCustom(token, testSecret));
        assertEquals(testUserId, jwtUtils.getUserIdFromJwtTokenCustom(token, testSecret));
    }

    @Test
    void validateJwtTokenCustom_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtils.generateJwtTokenCustom(testEmail, testUserId, testSecret, testExpirationMs);

        // Act & Assert
        assertTrue(jwtUtils.validateJwtTokenCustom(token, testSecret));
    }

    @Test
    void validateJwtTokenCustom_WithInvalidToken_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtTokenCustom("invalid.token.string", testSecret));
    }

    @Test
    void validateJwtTokenCustom_WithWrongSecret_ShouldReturnFalse() {
        // Arrange
        String token = jwtUtils.generateJwtTokenCustom(testEmail, testUserId, testSecret, testExpirationMs);
        String wrongSecret = "wrongSecretKeyMustBeLongEnoughForHmacSha256Algorithm";

        // Act & Assert
        assertFalse(jwtUtils.validateJwtTokenCustom(token, wrongSecret));
    }

    @Test
    void getEmailFromJwtTokenCustom_ShouldReturnCorrectEmail() {
        // Arrange
        String token = jwtUtils.generateJwtTokenCustom(testEmail, testUserId, testSecret, testExpirationMs);

        // Act
        String email = jwtUtils.getEmailFromJwtTokenCustom(token, testSecret);

        // Assert
        assertEquals(testEmail, email);
    }

    @Test
    void getUserIdFromJwtTokenCustom_ShouldReturnCorrectUserId() {
        // Arrange
        String token = jwtUtils.generateJwtTokenCustom(testEmail, testUserId, testSecret, testExpirationMs);

        // Act
        String userId = jwtUtils.getUserIdFromJwtTokenCustom(token, testSecret);

        // Assert
        assertEquals(testUserId, userId);
    }

    @Test
    void generateJwtToken_WithExpiredToken_ShouldFailValidation() throws InterruptedException {
        // Arrange
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1); // 1ms expiration
        String token = jwtUtils.generateJwtToken(testEmail, testUserId);
        
        // Wait for token to expire
        Thread.sleep(10);
        
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(token));
    }
} 