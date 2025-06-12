package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.common.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.json.JSONObject;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FixVersionTest {

    private FixVersion fixVersion;
    private JSONObject jsonObject;

    @Before
    public void setUp() {
        jsonObject = mock(JSONObject.class);
        fixVersion = new FixVersion(jsonObject);
    }

    @Test
    public void testGetName() {
        when(jsonObject.getString(FixVersion.NAME)).thenReturn("Version 1.0");
        assertEquals("Version 1.0", fixVersion.getName());
    }

    @Test
    public void testGetIterationName() {
        when(jsonObject.getString(FixVersion.NAME)).thenReturn("Iteration 1");
        assertEquals("Iteration 1", fixVersion.getIterationName());
    }

    @Test
    public void testGetId() {
        when(jsonObject.getString("id")).thenReturn("123");
        assertEquals("123".hashCode(), fixVersion.getId());
    }

    @Test
    public void testGetIdAsString() {
        when(jsonObject.getString("id")).thenReturn("123");
        assertEquals("123", fixVersion.getIdAsString());
    }

    @Test
    public void testGetStartDate() {
        when(jsonObject.getString(FixVersion.USER_START_DATE)).thenReturn("01/Jan/21");
        Date expectedDate = DateUtils.parseJiraDate("01/Jan/21");
        assertEquals(expectedDate, fixVersion.getStartDate());
    }

    @Test
    public void testGetEndDate() {
        when(jsonObject.getString(FixVersion.USER_RELEASE_DATE)).thenReturn("31/Dec/21");
        Date expectedDate = DateUtils.parseJiraDate("31/Dec/21");
        assertEquals(expectedDate, fixVersion.getEndDate());
    }

    @Test
    public void testIsReleased() {
        when(jsonObject.getBoolean("released")).thenReturn(true);
        assertTrue(fixVersion.isReleased());
    }

    @Test
    public void testGetUserReleaseDate() {
        when(jsonObject.getString(FixVersion.USER_RELEASE_DATE)).thenReturn("31/Dec/21");
        assertEquals("31/Dec/21", fixVersion.getUserReleaseDate());
    }

    @Test
    public void testGetUserStartDate() {
        when(jsonObject.getString(FixVersion.USER_START_DATE)).thenReturn("01/Jan/21");
        assertEquals("01/Jan/21", fixVersion.getUserStartDate());
    }

    @Test
    public void testSetUserReleaseDate() {
        fixVersion.setUserReleaseDate("31/Dec/21");
        verify(jsonObject).put(FixVersion.USER_RELEASE_DATE, "31/Dec/21");
    }

    @Test
    public void testSetUserStartDate() {
        fixVersion.setUserStartDate("01/Jan/21");
        verify(jsonObject).put(FixVersion.USER_START_DATE, "01/Jan/21");
    }

    @Test
    public void testSetReleaseDate() {
        fixVersion.setReleaseDate("31/Dec/21");
        verify(jsonObject).put(FixVersion.RELEASE_DATE, "31/Dec/21");
    }

    @Test
    public void testSetStartDate() {
        fixVersion.setStartDate("01/Jan/21");
        verify(jsonObject).put(FixVersion.START_DATE, "01/Jan/21");
    }

    @Test
    public void testGetArchived() {
        when(jsonObject.getBoolean("archived")).thenReturn(true);
        assertTrue(fixVersion.getArchived());
    }

    @Test
    public void testGetReleased() {
        when(jsonObject.getBoolean("released")).thenReturn(true);
        assertTrue(fixVersion.getReleased());
    }

    @Test
    public void testIsNotPlanned() {
        when(jsonObject.getString(FixVersion.NAME)).thenReturn("Not Planned");
        assertTrue(fixVersion.isNotPlanned());

        when(jsonObject.getString(FixVersion.NAME)).thenReturn("unknown");
        assertTrue(fixVersion.isNotPlanned());

        when(jsonObject.getString(FixVersion.NAME)).thenReturn("Planned");
        assertFalse(fixVersion.isNotPlanned());
    }

    @Test
    public void testCompareTo() {
        FixVersion other = mock(FixVersion.class);
        when(other.getName()).thenReturn("Version 2.0");
        when(jsonObject.getString(FixVersion.NAME)).thenReturn("Version 1.0");
        assertTrue(fixVersion.compareTo(other) < 0);
    }

    @Test
    public void testEquals() {
        FixVersion other = mock(FixVersion.class);
        when(other.getName()).thenReturn("Version 1.0");
        when(jsonObject.getString(FixVersion.NAME)).thenReturn("Version 1.0");
        assertTrue(fixVersion.equals(other));
    }

    @Test
    public void testHashCode() {
        when(jsonObject.getString(FixVersion.NAME)).thenReturn("Version 1.0");
        assertEquals("Version 1.0".hashCode(), fixVersion.hashCode());
    }
}