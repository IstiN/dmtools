package com.github.istin.dmtools.microsoft.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class ACAppVersionTest {

    private ACAppVersion acAppVersion;
    private JSONObject mockJsonObject;

    @Before
    public void setUp() {
        mockJsonObject = Mockito.mock(JSONObject.class);
        acAppVersion = new ACAppVersion(mockJsonObject);
    }

    @Test
    public void testGetId() throws JSONException {
        when(mockJsonObject.getLong("id")).thenReturn(123L);
        Long id = acAppVersion.getId();
        assertEquals(Long.valueOf(123), id);
    }

    @Test
    public void testGetVersion() throws JSONException {
        when(mockJsonObject.getString("version")).thenReturn("1.0.0");
        String version = acAppVersion.getVersion();
        assertEquals("1.0.0", version);
    }

    @Test
    public void testGetShortVersion() throws JSONException {
        when(mockJsonObject.getString("short_version")).thenReturn("1.0");
        String shortVersion = acAppVersion.getShortVersion();
        assertEquals("1.0", shortVersion);
    }

    @Test
    public void testGetTitle() throws JSONException {
        when(mockJsonObject.getString("version")).thenReturn("App Title");
        String title = acAppVersion.getTitle();
        assertEquals("App Title", title);
    }

    @Test
    public void testConstructorWithJsonString() throws JSONException {
        String jsonString = "{\"id\": 123, \"version\": \"1.0.0\", \"short_version\": \"1.0\"}";
        ACAppVersion acAppVersionFromString = new ACAppVersion(jsonString);
        assertEquals(Long.valueOf(123), acAppVersionFromString.getId());
        assertEquals("1.0.0", acAppVersionFromString.getVersion());
        assertEquals("1.0", acAppVersionFromString.getShortVersion());
    }

    @Test
    public void testConstructorWithEmptyJson() {
        ACAppVersion acAppVersionEmpty = new ACAppVersion();
        assertNull(acAppVersionEmpty.getId());
        assertNull(acAppVersionEmpty.getVersion());
        assertNull(acAppVersionEmpty.getShortVersion());
        assertNull(acAppVersionEmpty.getTitle());
    }
}