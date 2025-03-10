package com.github.istin.dmtools.documentation;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.documentation.area.KeyAreaMapperViaConfluence;
import com.github.istin.dmtools.documentation.area.TicketDocumentationHistoryTrackerViaConfluence;
import com.github.istin.dmtools.figma.BasicFigmaClient;
import com.github.istin.dmtools.figma.FigmaClient;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.report.model.KeyTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentationGeneratorTest {

    @Mock
    private TrackerClient trackerClient;

    @Mock
    private BasicConfluence confluence;

    @Mock
    private FigmaClient figmaClient;

    @Mock
    private JAssistant jAssistant;

    @Mock
    private PromptManager promptManager;

    @Mock
    private ConversationObserver conversationObserver;

    @InjectMocks
    private DocumentationGenerator documentationGenerator;

    @Before
    public void setUp() {
        documentationGenerator = new DocumentationGenerator();
    }


    @Test
    public void testRetrieveTickets() throws Exception {
        String jql = "jql";
        String[] listOfStatusesToSort = {"status1", "status2"};
        List<ITicket> tickets = mock(List.class);
        when(trackerClient.searchAndPerform(anyString(), any())).thenReturn(tickets);

        List<? extends ITicket> result = DocumentationGenerator.retrieveTickets(trackerClient, jql, listOfStatusesToSort);

        assertNotNull(result);
        verify(trackerClient, times(1)).searchAndPerform(anyString(), any());
    }

    @Test
    public void testRetrievePages() throws Exception {
        String[] urls = {"url1", "url2"};
        List<Content> contents = mock(List.class);
        when(confluence.contentsByUrls(urls)).thenReturn(contents);

        List<Content> result = DocumentationGenerator.retrievePages(confluence, urls);

        assertNotNull(result);
        verify(confluence, times(1)).contentsByUrls(urls);
    }
}