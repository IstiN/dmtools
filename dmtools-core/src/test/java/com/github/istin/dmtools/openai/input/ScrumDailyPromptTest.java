package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.sm.Change;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ScrumDailyPromptTest {

    @Test
    public void testGetUserName() {
        String userName = "testUser";
        List<Change> mockListOfChanges = mock(List.class);
        ScrumDailyPrompt scrumDailyPrompt = new ScrumDailyPrompt(userName, mockListOfChanges);

        assertEquals(userName, scrumDailyPrompt.getUserName());
    }

    @Test
    public void testGetListOfChanges() {
        String userName = "testUser";
        List<Change> mockListOfChanges = mock(List.class);
        ScrumDailyPrompt scrumDailyPrompt = new ScrumDailyPrompt(userName, mockListOfChanges);

        assertEquals(mockListOfChanges, scrumDailyPrompt.getListOfChanges());
    }
}