package com.github.istin.dmtools.microsoft.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AppTest {

    @Test
    public void testGetId() {
        App app = mock(App.class);
        Object expectedId = new Object();
        when(app.getId()).thenReturn(expectedId);

        Object actualId = app.getId();
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetPublicIdentifier() {
        App app = mock(App.class);
        String expectedIdentifier = "publicIdentifier";
        when(app.getPublicIdentifier()).thenReturn(expectedIdentifier);

        String actualIdentifier = app.getPublicIdentifier();
        assertEquals(expectedIdentifier, actualIdentifier);
    }

    @Test
    public void testGetTitle() {
        App app = mock(App.class);
        String expectedTitle = "title";
        when(app.getTitle()).thenReturn(expectedTitle);

        String actualTitle = app.getTitle();
        assertEquals(expectedTitle, actualTitle);
    }

    @Test
    public void testGetName() {
        App app = mock(App.class);
        String expectedName = "name";
        when(app.getName()).thenReturn(expectedName);

        String actualName = app.getName();
        assertEquals(expectedName, actualName);
    }
}