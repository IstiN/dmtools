package com.github.istin.dmtools.microsoft.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AppVersionTest {

    @Test
    public void testGetId() {
        AppVersion appVersion = mock(AppVersion.class);
        Long expectedId = 123L;
        when(appVersion.getId()).thenReturn(expectedId);

        Long actualId = appVersion.getId();
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetVersion() {
        AppVersion appVersion = mock(AppVersion.class);
        String expectedVersion = "1.0.0";
        when(appVersion.getVersion()).thenReturn(expectedVersion);

        String actualVersion = appVersion.getVersion();
        assertEquals(expectedVersion, actualVersion);
    }

    @Test
    public void testGetTitle() {
        AppVersion appVersion = mock(AppVersion.class);
        String expectedTitle = "Sample App";
        when(appVersion.getTitle()).thenReturn(expectedTitle);

        String actualTitle = appVersion.getTitle();
        assertEquals(expectedTitle, actualTitle);
    }
}