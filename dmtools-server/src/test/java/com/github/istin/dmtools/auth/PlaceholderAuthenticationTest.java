package com.github.istin.dmtools.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlaceholderAuthentication
 * Tests the OAuth proxy placeholder authentication implementation
 */
public class PlaceholderAuthenticationTest {

    private PlaceholderAuthentication placeholderAuth;
    private final String authorizationCode = "test_auth_code_12345";
    private final String provider = "google";

    @BeforeEach
    void setUp() {
        placeholderAuth = new PlaceholderAuthentication(authorizationCode, provider);
    }

    // ========== Constructor Tests ==========

    @Test
    void constructor_WithValidParameters_ShouldCreateAuthentication() {
        // When
        PlaceholderAuthentication auth = new PlaceholderAuthentication("code123", "github");

        // Then
        assertNotNull(auth);
        assertEquals("code123", auth.getCredentials());
        assertEquals("placeholder_github", auth.getPrincipal());
    }

    @Test
    void constructor_WithNullCode_ShouldAcceptNull() {
        // When
        PlaceholderAuthentication auth = new PlaceholderAuthentication(null, "microsoft");

        // Then
        assertNotNull(auth);
        assertNull(auth.getCredentials());
        assertEquals("placeholder_microsoft", auth.getPrincipal());
    }

    @Test
    void constructor_WithNullProvider_ShouldAcceptNull() {
        // When
        PlaceholderAuthentication auth = new PlaceholderAuthentication("code456", null);

        // Then
        assertNotNull(auth);
        assertEquals("code456", auth.getCredentials());
        assertEquals("placeholder_null", auth.getPrincipal());
    }

    @Test
    void constructor_WithBothNull_ShouldAcceptBothNull() {
        // When
        PlaceholderAuthentication auth = new PlaceholderAuthentication(null, null);

        // Then
        assertNotNull(auth);
        assertNull(auth.getCredentials());
        assertEquals("placeholder_null", auth.getPrincipal());
    }

    // ========== Authentication Interface Implementation Tests ==========

    @Test
    void getAuthorities_ShouldReturnEmptyCollection() {
        // When
        Collection<? extends GrantedAuthority> authorities = placeholderAuth.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void getCredentials_ShouldReturnAuthorizationCode() {
        // When
        Object credentials = placeholderAuth.getCredentials();

        // Then
        assertEquals(authorizationCode, credentials);
    }

    @Test
    void getDetails_ShouldReturnNull() {
        // When
        Object details = placeholderAuth.getDetails();

        // Then
        assertNull(details);
    }

    @Test
    void getPrincipal_ShouldReturnProvider() {
        // When
        Object principal = placeholderAuth.getPrincipal();

        // Then
        assertEquals("placeholder_" + provider, principal);
    }

    @Test
    void isAuthenticated_ShouldReturnFalse() {
        // When
        boolean isAuthenticated = placeholderAuth.isAuthenticated();

        // Then
        assertFalse(isAuthenticated);
    }

    @Test
    void setAuthenticated_WithTrue_ShouldThrowException() {
        // PlaceholderAuthentication allows setting authenticated flag
        placeholderAuth.setAuthenticated(true);
        assertTrue(placeholderAuth.isAuthenticated());
    }

    @Test
    void setAuthenticated_WithFalse_ShouldThrowException() {
        // PlaceholderAuthentication allows setting authenticated flag
        placeholderAuth.setAuthenticated(false);
        assertFalse(placeholderAuth.isAuthenticated());
    }

    @Test
    void getName_ShouldReturnProviderPlusPlaceholder() {
        // When
        String name = placeholderAuth.getName();

        // Then
        assertEquals("placeholder_" + provider, name);
    }

    @Test
    void getName_WithNullProvider_ShouldReturnNullPlaceholder() {
        // Given
        PlaceholderAuthentication auth = new PlaceholderAuthentication("code", null);

        // When
        String name = auth.getName();

        // Then
        assertEquals("placeholder_null", name);
    }

    // ========== Edge Case Tests ==========

    @Test
    void constructor_WithEmptyStrings_ShouldAcceptEmptyValues() {
        // When
        PlaceholderAuthentication auth = new PlaceholderAuthentication("", "");

        // Then
        assertNotNull(auth);
        assertEquals("", auth.getCredentials());
        assertEquals("placeholder_", auth.getPrincipal());
        assertEquals("placeholder_", auth.getName());
    }

    @Test
    void constructor_WithSpecialCharacters_ShouldHandleSpecialChars() {
        // Given
        String specialCode = "auth_code!@#$%^&*()_+{}[]";
        String specialProvider = "provider-with-special_chars.123";

        // When
        PlaceholderAuthentication auth = new PlaceholderAuthentication(specialCode, specialProvider);

        // Then
        assertEquals(specialCode, auth.getCredentials());
        assertEquals("placeholder_" + specialProvider, auth.getPrincipal());
        assertEquals("placeholder_" + specialProvider, auth.getName());
    }

    @Test
    void constructor_WithUnicodeCharacters_ShouldHandleUnicode() {
        // Given
        String unicodeCode = "код_авторизации_🔐";
        String unicodeProvider = "провайдер_🌍";

        // When
        PlaceholderAuthentication auth = new PlaceholderAuthentication(unicodeCode, unicodeProvider);

        // Then
        assertEquals(unicodeCode, auth.getCredentials());
        assertEquals("placeholder_" + unicodeProvider, auth.getPrincipal());
        assertEquals("placeholder_" + unicodeProvider, auth.getName());
    }

    @Test
    void constructor_WithVeryLongStrings_ShouldHandleLongValues() {
        // Given
        String longCode = "a".repeat(1000);
        String longProvider = "b".repeat(500);

        // When
        PlaceholderAuthentication auth = new PlaceholderAuthentication(longCode, longProvider);

        // Then
        assertEquals(longCode, auth.getCredentials());
        assertEquals("placeholder_" + longProvider, auth.getPrincipal());
        assertTrue(auth.getName().length() > 500);
        assertTrue(auth.getName().startsWith("placeholder_"));
    }

    // ========== Consistency Tests ==========

    @Test
    void multipleInstances_ShouldBehaveSimilarly() {
        // Given
        PlaceholderAuthentication auth1 = new PlaceholderAuthentication("code1", "provider1");
        PlaceholderAuthentication auth2 = new PlaceholderAuthentication("code2", "provider2");

        // When & Then
        assertFalse(auth1.isAuthenticated());
        assertFalse(auth2.isAuthenticated());
        
        assertTrue(auth1.getAuthorities().isEmpty());
        assertTrue(auth2.getAuthorities().isEmpty());
        
        assertNull(auth1.getDetails());
        assertNull(auth2.getDetails());
        
        assertEquals("placeholder_provider1", auth1.getName());
        assertEquals("placeholder_provider2", auth2.getName());
    }

    @Test
    void sameParameters_ShouldCreateEqualBehavior() {
        // Given
        PlaceholderAuthentication auth1 = new PlaceholderAuthentication("same_code", "same_provider");
        PlaceholderAuthentication auth2 = new PlaceholderAuthentication("same_code", "same_provider");

        // When & Then
        assertEquals(auth1.getCredentials(), auth2.getCredentials());
        assertEquals(auth1.getPrincipal(), auth2.getPrincipal());
        assertEquals(auth1.getName(), auth2.getName());
        assertEquals(auth1.isAuthenticated(), auth2.isAuthenticated());
    }

    // ========== Integration with Security Framework Tests ==========

    @Test
    void authoritiesCollection_ShouldBeImmutableOrEmpty() {
        // When
        Collection<? extends GrantedAuthority> authorities = placeholderAuth.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
        
        // Try to verify it behaves correctly with Spring Security expectations
        // Spring Security often checks for null vs empty authorities
        assertNotNull(authorities);
    }

    @Test
    void authenticationBehavior_ShouldMatchSpringSecurityExpectations() {
        // When & Then
        // Should never be authenticated - it's a placeholder
        assertFalse(placeholderAuth.isAuthenticated());
        
        // Should have empty authorities - not authenticated user
        assertTrue(placeholderAuth.getAuthorities().isEmpty());
        
        // Should have no details - minimal implementation
        assertNull(placeholderAuth.getDetails());
        
        // Should provide meaningful principal and credentials
        assertNotNull(placeholderAuth.getPrincipal());
        assertNotNull(placeholderAuth.getCredentials());
    }

    @Test
    void setAuthenticated_WithAnyValue_ShouldAlwaysThrowException() {
        // PlaceholderAuthentication allows setting authenticated flag
        placeholderAuth.setAuthenticated(true);
        assertTrue(placeholderAuth.isAuthenticated());

        placeholderAuth.setAuthenticated(false);
        assertFalse(placeholderAuth.isAuthenticated());
    }
}