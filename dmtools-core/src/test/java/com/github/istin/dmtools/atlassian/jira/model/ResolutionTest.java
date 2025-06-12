package com.github.istin.dmtools.atlassian.jira.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.json.JSONObject;
import org.json.JSONException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ResolutionTest {

    private Resolution resolution;
    private JSONObject mockJsonObject;

    @Before
    public void setUp() throws JSONException {
        mockJsonObject = mock(JSONObject.class);
        resolution = new Resolution(mockJsonObject);
    }

    @Test
    public void testGetId() {
        when(mockJsonObject.getString("id")).thenReturn("123");
        assertEquals("123", resolution.getId());
    }

    @Test
    public void testGetName() {
        when(mockJsonObject.getString("name")).thenReturn("Fixed");
        assertEquals("Fixed", resolution.getName());
    }

    @Test
    public void testSetId() throws JSONException {
        resolution.setId("456");
        verify(mockJsonObject).put("id", "456");
    }

    @Test
    public void testIsFixed() {
        when(mockJsonObject.getString("name")).thenReturn("Fixed");
        assertTrue(resolution.isFixed());

        when(mockJsonObject.getString("name")).thenReturn("in progress");
        assertFalse(resolution.isFixed());
    }

    @Test
    public void testIsRejected() {
        when(mockJsonObject.getString("name")).thenReturn("Not an issue");
        assertTrue(resolution.isRejected());

        when(mockJsonObject.getString("name")).thenReturn("Won't fix");
        assertTrue(resolution.isRejected());

        when(mockJsonObject.getString("name")).thenReturn("Not a bug");
        assertTrue(resolution.isRejected());

        when(mockJsonObject.getString("name")).thenReturn("Not valid");
        assertTrue(resolution.isRejected());

        when(mockJsonObject.getString("name")).thenReturn("Cannot reproduce");
        assertTrue(resolution.isRejected());

        when(mockJsonObject.getString("name")).thenReturn("Duplicate");
        assertTrue(resolution.isRejected());

        when(mockJsonObject.getString("name")).thenReturn("Not implemented");
        assertTrue(resolution.isRejected());

        when(mockJsonObject.getString("name")).thenReturn("Valid issue");
        assertFalse(resolution.isRejected());
    }
}