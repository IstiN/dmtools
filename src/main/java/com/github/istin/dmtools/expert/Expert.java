package com.github.istin.dmtools.expert;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;

public class Expert extends AbstractJob<ExpertParams> {

    @Override
    public void runJob(ExpertParams expertParams) throws Exception {
        runJob(expertParams.getInputJQL(), expertParams.getProjectContext(), expertParams.getRequest(), expertParams.getInitiator());
    }

    public static void runJob(String inputJQL, String projectContext, String request, String initiator) throws Exception {
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();

        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();

        JAssistant jAssistant = new JAssistant(trackerClient, null, openAI, promptManager);

        trackerClient.searchAndPerform(ticket -> {
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext();
            String response = jAssistant.makeResponseOnRequest(ticketContext, projectContext, request);
            trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", there is response on your request: \n" + request + "\n\nAI Response is: \n" + response);
            return false;
        }, inputJQL, trackerClient.getExtendedQueryFields());
    }

}
