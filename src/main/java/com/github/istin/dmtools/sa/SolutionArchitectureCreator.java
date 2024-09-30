package com.github.istin.dmtools.sa;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;

public class SolutionArchitectureCreator extends AbstractJob<SolutionArchitectureCreatorParams> {

    @Override
    public void runJob(SolutionArchitectureCreatorParams params) throws Exception {
        runJob(params.getRoleSpecific(), params.getProjectSpecific(), params.getStoriesJql(), params.getLabelNameToMarkAsReviewed());
    }

    public static void runJob(String roleSpecific, String projectSpecific, String storiesJql, String labelNameToMarkAsReviewed) throws Exception {
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
                String ticketDescription = ticket.getTicketDescription();

                String solution = jAssistant.createSolutionForTicket(trackerClient, roleSpecific, projectSpecific, ticketContext);
                trackerClient.updateDescription(ticket.getKey(), ticketDescription + "\n" + solution);
                trackerClient.addLabelIfNotExists(ticket, labelNameToMarkAsReviewed);
                return false;
            }
        }, storiesJql, trackerClient.getExtendedQueryFields());
    }
}
