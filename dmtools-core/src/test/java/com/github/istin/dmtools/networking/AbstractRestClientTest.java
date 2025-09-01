package com.github.istin.dmtools.networking;

import com.github.istin.dmtools.common.networking.GenericRequest;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractRestClientTest {

    private AbstractRestClient restClient;

    @Before
    public void setUp() throws IOException {
        restClient = new AbstractRestClient("http://example.com", "auth") {
            @Override
            public String path(String path) {
                return "";
            }

            @Override
            public Request.Builder sign(Request.Builder builder) {
                return builder;
            }

        };
    }

    @Test
    public void testGetTimeout() {
        assertEquals(60, restClient.getTimeout());
    }

    @Test
    public void testSetCachePostRequestsEnabled() {
        restClient.setCachePostRequestsEnabled(true);
        assertTrue(restClient.isCachePostRequestsEnabled());
    }

    @Test
    public void testSetClearCache() throws IOException {
        restClient.setClearCache(true);
        assertTrue(restClient.isClearCache);
    }


    @Test
    public void testGetBasePath() {
        assertEquals("http://example.com", restClient.getBasePath());
    }

    @Test
    public void testGetCachedFile() {
        GenericRequest mockRequest = mock(GenericRequest.class);
        when(mockRequest.url()).thenReturn("http://example.com");
        File cachedFile = restClient.getCachedFile(mockRequest);
        assertNotNull(cachedFile);
    }

    // URL Sanitization Tests

    @Test
    public void testSanitizeUrl_WithApiKey() {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=real_geminiKeysj4";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=***REDACTED***", sanitized);
    }

    @Test
    public void testSanitizeUrl_WithToken() {
        String url = "https://api.example.com/data?token=secret123&param=value";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?token=***REDACTED***&param=value", sanitized);
    }

    @Test
    public void testSanitizeUrl_WithMultipleSensitiveParams() {
        String url = "https://api.example.com/data?key=apikey123&token=token456&param=value&secret=mysecret";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?key=***REDACTED***&token=***REDACTED***&param=value&secret=***REDACTED***", sanitized);
    }

    @Test
    public void testSanitizeUrl_WithPassword() {
        String url = "https://api.example.com/login?username=user&password=secret123";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/login?username=user&password=***REDACTED***", sanitized);
    }

    @Test
    public void testSanitizeUrl_WithAccessToken() {
        String url = "https://api.example.com/data?access_token=bearer123&data=info";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?access_token=***REDACTED***&data=info", sanitized);
    }

    @Test
    public void testSanitizeUrl_WithApiKeyVariation() {
        String url = "https://api.example.com/data?api_key=mykey&apikey=anotherkey&normal=param";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?api_key=***REDACTED***&apikey=***REDACTED***&normal=param", sanitized);
    }

    @Test
    public void testSanitizeUrl_WithAuthorization() {
        String url = "https://api.example.com/data?authorization=Bearer_token123&param=value";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?authorization=***REDACTED***&param=value", sanitized);
    }

    @Test
    public void testSanitizeUrl_NoQueryParams() {
        String url = "https://api.example.com/data";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data", sanitized);
    }

    @Test
    public void testSanitizeUrl_NoSensitiveParams() {
        String url = "https://api.example.com/data?param1=value1&param2=value2";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?param1=value1&param2=value2", sanitized);
    }

    @Test
    public void testSanitizeUrl_EmptyQueryParam() {
        String url = "https://api.example.com/data?key=&param=value";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?key=***REDACTED***&param=value", sanitized);
    }

    @Test
    public void testSanitizeUrl_ParamWithoutValue() {
        String url = "https://api.example.com/data?key&param=value";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?key&param=value", sanitized);
    }

    @Test
    public void testSanitizeUrl_NullUrl() {
        String sanitized = AbstractRestClient.sanitizeUrl(null);
        assertNull(sanitized);
    }

    @Test
    public void testSanitizeUrl_EmptyUrl() {
        String url = "";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("", sanitized);
    }

    @Test
    public void testSanitizeUrl_CaseInsensitive() {
        String url = "https://api.example.com/data?KEY=secret123&Token=token456&APIKEY=key789";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?KEY=***REDACTED***&Token=***REDACTED***&APIKEY=***REDACTED***", sanitized);
    }

    @Test
    public void testSanitizeUrl_GeminiSpecificCase() {
        // This is the exact URL pattern from the bug report
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=real_geminiKeysj4";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        
        // Verify the key is redacted
        assertFalse("Sanitized URL should not contain the real API key", sanitized.contains("real_geminiKeysj4"));
        assertTrue("Sanitized URL should contain redacted placeholder", sanitized.contains("***REDACTED***"));
        assertTrue("Sanitized URL should preserve the base URL", sanitized.startsWith("https://generativelanguage.googleapis.com"));
    }

    @Test
    public void testSanitizeUrl_SubstringMatching() {
        // Test that parameters containing sensitive words are also redacted
        String url = "https://api.example.com/data?my_api_key=secret&user_token=token123&normal=value";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?my_api_key=***REDACTED***&user_token=***REDACTED***&normal=value", sanitized);
    }

    @Test
    public void testSanitizeUrl_UrlEncoded() {
        String url = "https://api.example.com/data?key=encoded%20value&param=normal";
        String sanitized = AbstractRestClient.sanitizeUrl(url);
        assertEquals("https://api.example.com/data?key=***REDACTED***&param=normal", sanitized);
    }

}