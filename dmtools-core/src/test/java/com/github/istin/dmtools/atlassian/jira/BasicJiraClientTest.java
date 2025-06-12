package com.github.istin.dmtools.atlassian.jira;

import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class BasicJiraClientTest {

    private BasicJiraClient basicJiraClient;

    @Before
    public void setUp() throws IOException {
        basicJiraClient = Mockito.spy(new BasicJiraClient());
    }

    @Test
    public void testGetDefaultQueryFields() {
        String[] expectedFields = {
                "summary", "status", "attachment", "updated", "created", "creator", "reporter",
                "components", "issuetype", "fixVersions", "customfield_10004", "labels", "priority", "parent"
        };
        assertArrayEquals(expectedFields, basicJiraClient.getDefaultQueryFields());
    }

    @Test
    public void testGetExtendedQueryFields() {
        String[] expectedFields = {
                "description", "issuelinks", "summary", "status", "attachment", "updated", "created", "creator",
                "reporter", "components", "issuetype", "fixVersions", "customfield_10004", "labels",
                "priority", "parent"
        };
        assertArrayEquals(expectedFields, basicJiraClient.getExtendedQueryFields());
    }

    @Test
    public void testGetTestCases() throws IOException {
        ITicket mockTicket = mock(ITicket.class);
        List<? extends ITicket> testCases = basicJiraClient.getTestCases(mockTicket);
        assertNotNull(testCases);
        assertEquals(0, testCases.size());
    }

    @Test
    public void testGetTextType() {
        assertEquals(TrackerClient.TextType.MARKDOWN, basicJiraClient.getTextType());
    }

    @Test
    public void testDeleteCommentIfExists() throws IOException {
        // Placeholder for testing deleteCommentIfExists method
        // TODO: Implement test logic for deleteCommentIfExists
    }
}