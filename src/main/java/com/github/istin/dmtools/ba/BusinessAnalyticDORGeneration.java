package com.github.istin.dmtools.ba;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BusinessAnalyticDORGeneration extends AbstractJob<BusinessAnalyticDORGenerationParams> {
    private static final Logger logger = LogManager.getLogger(BusinessAnalyticDORGeneration.class);
    @Override
    public void runJob(BusinessAnalyticDORGenerationParams params) throws Exception {
        runJob(params.getOutputConfluencePage(), params.getInputJQL(), params.getInitiator());
    }

    public static void runJob(String outputConfluencePage, String inputJQL, String initiator) throws Exception {
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();
        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();
        JAssistant jAssistant = new JAssistant(trackerClient, null, openAI, promptManager);

        trackerClient.searchAndPerform(new JiraClient.Performer() {
            @Override
            public boolean perform(ITicket ticket) throws Exception {
                TicketContext ticketContext = new TicketContext(trackerClient, ticket);
                ticketContext.prepareContext();

                BasicConfluence confluence = BasicConfluence.getInstance();
                Content content = confluence.findContent(outputConfluencePage);
                String output = jAssistant.buildDORGenerationForStory(ticketContext, content.getStorage().getValue());
                confluence.updatePage(content.getId(), content.getTitle(), content.getParentId(), output, confluence.getDefaultSpace(), ticket.getKey() + " " + ticket.getTicketLink());
                return false;
            }
        }, inputJQL, trackerClient.getExtendedQueryFields());
    }

}
