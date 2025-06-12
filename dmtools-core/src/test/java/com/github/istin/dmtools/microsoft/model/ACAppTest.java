package com.github.istin.dmtools.microsoft.model;

import org.junit.Test;
import org.mockito.Mockito;
import org.json.JSONObject;
import org.json.JSONException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ACAppTest {

    @Test
    public void testGetId() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", "12345");
        ACApp acApp = new ACApp(json);

        assertEquals("12345", acApp.getId());
    }

    @Test
    public void testGetPublicIdentifier() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", "publicIdentifier");
        ACApp acApp = new ACApp(json);

        assertEquals("publicIdentifier", acApp.getPublicIdentifier());
    }

    @Test
    public void testGetTitle() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("display_name", "App Title");
        ACApp acApp = new ACApp(json);

        assertEquals("App Title", acApp.getTitle());
    }

    @Test
    public void testGetName() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", "AppName");
        ACApp acApp = new ACApp(json);

        assertEquals("AppName", acApp.getName());
    }

    @Test
    public void testConstructorWithJSONString() throws JSONException {
        String jsonString = "{\"id\":\"12345\", \"display_name\":\"App Title\", \"name\":\"AppName\"}";
        ACApp acApp = new ACApp(jsonString);

        assertEquals("12345", acApp.getId());
        assertEquals("App Title", acApp.getTitle());
        assertEquals("AppName", acApp.getName());
    }

    @Test
    public void testConstructorWithJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", "12345");
        json.put("display_name", "App Title");
        json.put("name", "AppName");
        ACApp acApp = new ACApp(json);

        assertEquals("12345", acApp.getId());
        assertEquals("App Title", acApp.getTitle());
        assertEquals("AppName", acApp.getName());
    }
}