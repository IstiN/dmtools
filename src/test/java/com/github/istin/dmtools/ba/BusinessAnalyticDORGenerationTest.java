package com.github.istin.dmtools.ba;

import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.confluence.model.Storage;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class BusinessAnalyticDORGenerationTest {

    private BasicJiraClient trackerClientMock;
    private BasicConfluence confluenceMock;
    private JAssistant jAssistantMock;
    private BusinessAnalyticDORGenerationParams paramsMock;

    @Before
    public void setUp() {
        trackerClientMock = mock(BasicJiraClient.class);
        confluenceMock = mock(BasicConfluence.class);
        jAssistantMock = mock(JAssistant.class);
        paramsMock = mock(BusinessAnalyticDORGenerationParams.class);
    }


}