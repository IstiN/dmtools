package com.github.istin.dmtools.auth.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OAuth DTO classes
 * Tests OAuthInitiateRequest, OAuthExchangeRequest, and OAuthStateData
 */
public class OAuthDtoTest {

    // ========== OAuthInitiateRequest Tests ==========

    @Test
    void oAuthInitiateRequest_DefaultConstructor_ShouldCreateEmptyObject() {
        // Act
        OAuthInitiateRequest request = new OAuthInitiateRequest();

        // Assert
        assertNull(request.getProvider());
        assertNull(request.getClientRedirectUri());
        assertNull(request.getClientType());
        assertNull(request.getEnvironment());
    }

    @Test
    void oAuthInitiateRequest_ParameterizedConstructor_ShouldSetAllFields() {
        // Arrange
        String provider = "google";
        String clientRedirectUri = "https://myapp.com/callback";
        String clientType = "web";
        String environment = "dev";

        // Act
        OAuthInitiateRequest request = new OAuthInitiateRequest(provider, clientRedirectUri, clientType, environment);

        // Assert
        assertEquals(provider, request.getProvider());
        assertEquals(clientRedirectUri, request.getClientRedirectUri());
        assertEquals(clientType, request.getClientType());
        assertEquals(environment, request.getEnvironment());
    }

    @Test
    void oAuthInitiateRequest_Setters_ShouldUpdateFields() {
        // Arrange
        OAuthInitiateRequest request = new OAuthInitiateRequest();

        // Act
        request.setProvider("microsoft");
        request.setClientRedirectUri("https://app.example.com/auth");
        request.setClientType("mobile");
        request.setEnvironment("staging");

        // Assert
        assertEquals("microsoft", request.getProvider());
        assertEquals("https://app.example.com/auth", request.getClientRedirectUri());
        assertEquals("mobile", request.getClientType());
        assertEquals("staging", request.getEnvironment());
    }

    @Test
    void oAuthInitiateRequest_WithNullValues_ShouldHandleNulls() {
        // Act
        OAuthInitiateRequest request = new OAuthInitiateRequest(null, null, null, null);

        // Assert
        assertNull(request.getProvider());
        assertNull(request.getClientRedirectUri());
        assertNull(request.getClientType());
        assertNull(request.getEnvironment());
    }

    @Test
    void oAuthInitiateRequest_WithEmptyStrings_ShouldHandleEmptyStrings() {
        // Act
        OAuthInitiateRequest request = new OAuthInitiateRequest("", "", "", "");

        // Assert
        assertEquals("", request.getProvider());
        assertEquals("", request.getClientRedirectUri());
        assertEquals("", request.getClientType());
        assertEquals("", request.getEnvironment());
    }

    @Test
    void oAuthInitiateRequest_WithSpecialCharacters_ShouldHandleSpecialCharacters() {
        // Arrange
        String provider = "github-enterprise";
        String clientRedirectUri = "myapp://auth/callback?param=value&another=test";
        String clientType = "desktop_app";
        String environment = "dev-test";

        // Act
        OAuthInitiateRequest request = new OAuthInitiateRequest(provider, clientRedirectUri, clientType, environment);

        // Assert
        assertEquals(provider, request.getProvider());
        assertEquals(clientRedirectUri, request.getClientRedirectUri());
        assertEquals(clientType, request.getClientType());
        assertEquals(environment, request.getEnvironment());
    }

    // ========== OAuthExchangeRequest Tests ==========

    @Test
    void oAuthExchangeRequest_DefaultConstructor_ShouldCreateEmptyObject() {
        // Act
        OAuthExchangeRequest request = new OAuthExchangeRequest();

        // Assert
        assertNull(request.getCode());
        assertNull(request.getState());
    }

    @Test
    void oAuthExchangeRequest_ParameterizedConstructor_ShouldSetAllFields() {
        // Arrange
        String code = "temp_code_12345";
        String state = "oauth_proxy_abcd-efgh-ijkl";

        // Act
        OAuthExchangeRequest request = new OAuthExchangeRequest(code, state);

        // Assert
        assertEquals(code, request.getCode());
        assertEquals(state, request.getState());
    }

    @Test
    void oAuthExchangeRequest_Setters_ShouldUpdateFields() {
        // Arrange
        OAuthExchangeRequest request = new OAuthExchangeRequest();

        // Act
        request.setCode("updated_code_67890");
        request.setState("updated_state_mnop-qrst-uvwx");

        // Assert
        assertEquals("updated_code_67890", request.getCode());
        assertEquals("updated_state_mnop-qrst-uvwx", request.getState());
    }

    @Test
    void oAuthExchangeRequest_WithNullValues_ShouldHandleNulls() {
        // Act
        OAuthExchangeRequest request = new OAuthExchangeRequest(null, null);

        // Assert
        assertNull(request.getCode());
        assertNull(request.getState());
    }

    @Test
    void oAuthExchangeRequest_WithEmptyStrings_ShouldHandleEmptyStrings() {
        // Act
        OAuthExchangeRequest request = new OAuthExchangeRequest("", "");

        // Assert
        assertEquals("", request.getCode());
        assertEquals("", request.getState());
    }

    @Test
    void oAuthExchangeRequest_WithLongValues_ShouldHandleLongStrings() {
        // Arrange
        String longCode = "a".repeat(1000);
        String longState = "b".repeat(1000);

        // Act
        OAuthExchangeRequest request = new OAuthExchangeRequest(longCode, longState);

        // Assert
        assertEquals(longCode, request.getCode());
        assertEquals(longState, request.getState());
    }

    // ========== OAuthStateData Tests ==========

    @Test
    void oAuthStateData_DefaultConstructor_ShouldCreateEmptyObject() {
        // Act
        OAuthStateData stateData = new OAuthStateData();

        // Assert
        assertNull(stateData.getProvider());
        assertNull(stateData.getClientRedirectUri());
        assertNull(stateData.getClientType());
        assertNull(stateData.getEnvironment());
        assertNull(stateData.getCreatedAt());
        assertNull(stateData.getExpiresAt());
        assertNull(stateData.getAuthorizationCode());
    }

    @Test
    void oAuthStateData_ParameterizedConstructor_ShouldSetFieldsAndTimestamps() {
        // Arrange
        String provider = "google";
        String clientRedirectUri = "https://myapp.com/callback";
        String clientType = "web";
        String environment = "prod";
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // Act
        OAuthStateData stateData = new OAuthStateData(provider, clientRedirectUri, clientType, environment);
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertEquals(provider, stateData.getProvider());
        assertEquals(clientRedirectUri, stateData.getClientRedirectUri());
        assertEquals(clientType, stateData.getClientType());
        assertEquals(environment, stateData.getEnvironment());
        
        // Verify timestamps
        assertNotNull(stateData.getCreatedAt());
        assertNotNull(stateData.getExpiresAt());
        assertTrue(stateData.getCreatedAt().isAfter(beforeCreation));
        assertTrue(stateData.getCreatedAt().isBefore(afterCreation));
        assertTrue(stateData.getExpiresAt().isAfter(stateData.getCreatedAt()));
        
        // Verify expiration is 5 minutes after creation
        assertEquals(5, stateData.getExpiresAt().getMinute() - stateData.getCreatedAt().getMinute());
    }

    @Test
    void oAuthStateData_Setters_ShouldUpdateAllFields() {
        // Arrange
        OAuthStateData stateData = new OAuthStateData();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(10);

        // Act
        stateData.setProvider("github");
        stateData.setClientRedirectUri("https://github-app.com/auth");
        stateData.setClientType("desktop");
        stateData.setEnvironment("staging");
        stateData.setCreatedAt(now);
        stateData.setExpiresAt(expiry);
        stateData.setAuthorizationCode("auth_code_xyz");

        // Assert
        assertEquals("github", stateData.getProvider());
        assertEquals("https://github-app.com/auth", stateData.getClientRedirectUri());
        assertEquals("desktop", stateData.getClientType());
        assertEquals("staging", stateData.getEnvironment());
        assertEquals(now, stateData.getCreatedAt());
        assertEquals(expiry, stateData.getExpiresAt());
        assertEquals("auth_code_xyz", stateData.getAuthorizationCode());
    }

    @Test
    void oAuthStateData_isExpired_WithFutureExpiryDate_ShouldReturnFalse() {
        // Arrange
        OAuthStateData stateData = new OAuthStateData();
        stateData.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        // Act & Assert
        assertFalse(stateData.isExpired());
    }

    @Test
    void oAuthStateData_isExpired_WithPastExpiryDate_ShouldReturnTrue() {
        // Arrange
        OAuthStateData stateData = new OAuthStateData();
        stateData.setExpiresAt(LocalDateTime.now().minusMinutes(10));

        // Act & Assert
        assertTrue(stateData.isExpired());
    }

    @Test
    void oAuthStateData_isExpired_WithCurrentTime_ShouldReturnTrue() {
        // Arrange
        OAuthStateData stateData = new OAuthStateData();
        stateData.setExpiresAt(LocalDateTime.now().minusNanos(1)); // Just passed

        // Act & Assert
        assertTrue(stateData.isExpired());
    }

    @Test
    void oAuthStateData_isExpired_WithNullExpiryDate_ShouldThrowException() {
        // Arrange
        OAuthStateData stateData = new OAuthStateData();
        stateData.setExpiresAt(null);

        // Act & Assert
        assertThrows(NullPointerException.class, stateData::isExpired);
    }

    @Test
    void oAuthStateData_WithNullValues_ShouldHandleNulls() {
        // Act
        OAuthStateData stateData = new OAuthStateData(null, null, null, null);

        // Assert
        assertNull(stateData.getProvider());
        assertNull(stateData.getClientRedirectUri());
        assertNull(stateData.getClientType());
        assertNull(stateData.getEnvironment());
        assertNotNull(stateData.getCreatedAt()); // Should still be set by constructor
        assertNotNull(stateData.getExpiresAt()); // Should still be set by constructor
    }

    @Test
    void oAuthStateData_WithEmptyStrings_ShouldHandleEmptyStrings() {
        // Act
        OAuthStateData stateData = new OAuthStateData("", "", "", "");

        // Assert
        assertEquals("", stateData.getProvider());
        assertEquals("", stateData.getClientRedirectUri());
        assertEquals("", stateData.getClientType());
        assertEquals("", stateData.getEnvironment());
    }

    // ========== Edge Cases and Integration Tests ==========

    @Test
    void oAuthInitiateRequest_WithVeryLongUrls_ShouldHandleLongUrls() {
        // Arrange
        String veryLongUrl = "https://example.com/very/long/path/" + "segment/".repeat(100) + "callback";

        // Act
        OAuthInitiateRequest request = new OAuthInitiateRequest("google", veryLongUrl, "web", "dev");

        // Assert
        assertEquals(veryLongUrl, request.getClientRedirectUri());
        assertTrue(request.getClientRedirectUri().length() > 500);
    }

    @Test
    void oAuthStateData_ExpirationCalculation_ShouldBeConsistent() {
        // Act - Create multiple state data objects quickly
        OAuthStateData state1 = new OAuthStateData("google", "https://app1.com", "web", "dev");
        OAuthStateData state2 = new OAuthStateData("microsoft", "https://app2.com", "mobile", "prod");

        // Assert - Both should have similar creation times and consistent expiration
        long timeDifference = Math.abs(
            state1.getCreatedAt().getNano() - state2.getCreatedAt().getNano()
        );
        assertTrue(timeDifference < 1_000_000_000); // Less than 1 second difference

        assertFalse(state1.isExpired());
        assertFalse(state2.isExpired());
    }

    @Test
    void allDtoClasses_WithUnicodeCharacters_ShouldHandleUnicode() {
        // Arrange
        String unicodeProvider = "测试-provider";
        String unicodeUrl = "https://тест.example.com/回调";
        String unicodeType = "мобильный";
        String unicodeEnv = "разработка";

        // Act
        OAuthInitiateRequest request = new OAuthInitiateRequest(unicodeProvider, unicodeUrl, unicodeType, unicodeEnv);
        OAuthStateData stateData = new OAuthStateData(unicodeProvider, unicodeUrl, unicodeType, unicodeEnv);

        // Assert
        assertEquals(unicodeProvider, request.getProvider());
        assertEquals(unicodeUrl, request.getClientRedirectUri());
        assertEquals(unicodeProvider, stateData.getProvider());
        assertEquals(unicodeUrl, stateData.getClientRedirectUri());
    }

    @Test
    void oAuthStateData_SerializableCompliance_ShouldBeSerializable() {
        // Arrange
        OAuthStateData stateData = new OAuthStateData("google", "https://app.com", "web", "dev");
        stateData.setAuthorizationCode("test_auth_code");

        // Act & Assert - Simply verify that OAuthStateData implements Serializable
        assertTrue(stateData instanceof java.io.Serializable);
    }
}
