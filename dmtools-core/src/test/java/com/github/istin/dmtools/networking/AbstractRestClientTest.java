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

}