package com.github.istin.dmtools.dev;

import com.github.istin.dmtools.dev.CodeGenerator;
import com.github.istin.dmtools.dev.CodeGeneratorParams;
import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.report.freemarker.GenericCell;
import com.github.istin.dmtools.report.freemarker.GenericReport;
import com.github.istin.dmtools.report.freemarker.GenericRow;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.*;

public class CodeGeneratorTest {

    private CodeGenerator codeGenerator;
    private CodeGeneratorParams params;
    private BasicConfluence confluence;
    private TrackerClient<ITicket> trackerClient;
    private List<SourceCode> basicSourceCodes;
    private ConversationObserver conversationObserver;
    private BasicOpenAI openAI;
    private PromptManager promptManager;
    private JAssistant jAssistant;

    @Before
    public void setUp() {
        codeGenerator = new CodeGenerator();
        params = mock(CodeGeneratorParams.class);
        confluence = mock(BasicConfluence.class);
        trackerClient = mock(TrackerClient.class);
        basicSourceCodes = mock(List.class);
        conversationObserver = mock(ConversationObserver.class);
        openAI = mock(BasicOpenAI.class);
        promptManager = mock(PromptManager.class);
        jAssistant = mock(JAssistant.class);
    }


    @Test
    public void testGenerateCode() throws Exception {
        TicketContext ticketContext = mock(TicketContext.class);
        when(ticketContext.getTicket()).thenReturn(mock(ITicket.class));

        CodeGenerator.generateCode("rootPage", "prefix", "role", ticketContext, jAssistant, conversationObserver, confluence, basicSourceCodes);

        verify(jAssistant, times(1)).generateCode(eq("role"), eq(ticketContext));
        verify(conversationObserver, times(1)).getMessages();
    }
}