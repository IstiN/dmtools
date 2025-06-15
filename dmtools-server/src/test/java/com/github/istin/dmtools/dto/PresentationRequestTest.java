package com.github.istin.dmtools.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PresentationRequestTest {

    @Test
    void testNoArgsConstructor() {
        PresentationRequest request = new PresentationRequest();
        assertNull(request.getJsConfig());
        assertNull(request.getPresentationParams());
        assertNull(request.getCustomTopic());
    }

    @Test
    void testAllArgsConstructor() {
        String jsConfig = "{\"jsScript\": \"function generatePresentationJs(params, bridge) { return JSON.stringify({title: 'Test'}); }\", \"clientName\": \"Test\"}";
        String params = "{\"presenter\":\"Test User\",\"topic\":\"Test Topic\"}";
        String customTopic = "Custom_Topic";

        PresentationRequest request = new PresentationRequest(jsConfig, params, customTopic);
        
        assertEquals(jsConfig, request.getJsConfig());
        assertEquals(params, request.getPresentationParams());
        assertEquals(customTopic, request.getCustomTopic());
    }

    @Test
    void testSettersAndGetters() {
        PresentationRequest request = new PresentationRequest();
        
        String jsConfig = "{\"jsScript\": \"function generatePresentationJs(params, bridge) { return JSON.stringify({title: 'Test'}); }\", \"clientName\": \"Test\"}";
        String params = "{\"presenter\":\"Test User\",\"topic\":\"Test Topic\"}";
        String customTopic = "Custom_Topic";
        
        request.setJsConfig(jsConfig);
        request.setPresentationParams(params);
        request.setCustomTopic(customTopic);
        
        assertEquals(jsConfig, request.getJsConfig());
        assertEquals(params, request.getPresentationParams());
        assertEquals(customTopic, request.getCustomTopic());
    }

    @Test
    void testEqualsAndHashCode() {
        String jsConfig = "{\"jsScript\": \"function generatePresentationJs(params, bridge) { return JSON.stringify({title: 'Test'}); }\", \"clientName\": \"Test\"}";
        String params = "{\"presenter\":\"Test User\",\"topic\":\"Test Topic\"}";
        String customTopic = "Custom_Topic";
        
        PresentationRequest request1 = new PresentationRequest(jsConfig, params, customTopic);
        PresentationRequest request2 = new PresentationRequest(jsConfig, params, customTopic);
        PresentationRequest request3 = new PresentationRequest(jsConfig, params, "Different_Topic");
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testToString() {
        String jsConfig = "{\"jsScript\": \"function generatePresentationJs(params, bridge) { return JSON.stringify({title: 'Test'}); }\", \"clientName\": \"Test\"}";
        String params = "{\"presenter\":\"Test User\",\"topic\":\"Test Topic\"}";
        String customTopic = "Custom_Topic";
        
        PresentationRequest request = new PresentationRequest(jsConfig, params, customTopic);
        String toString = request.toString();
        
        assertTrue(toString.contains("jsConfig=" + jsConfig));
        assertTrue(toString.contains("presentationParams=" + params));
        assertTrue(toString.contains("customTopic=" + customTopic));
    }
} 