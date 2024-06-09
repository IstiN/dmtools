package com.github.istin.dmtools.diagram;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.Diagram;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;



public class DiagramsCreator extends AbstractJob<DiagramsCreatorParams> {
    @Override
    public void runJob(DiagramsCreatorParams params) throws Exception {
        runJob(params.getRoleSpecific(), params.getProjectSpecific(), params.getStoriesJql(), params.getLabelNameToMarkAsReviewed());
    }

    public static void runJob(String roleSpecific, String projectSpecific, String storiesJql, String labelNameToMarkAsReviewed) throws Exception {
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();
        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();
        JAssistant jAssistant = new JAssistant(trackerClient, null, openAI, promptManager);
        DiagramsDrawer diagramsDrawer = new DiagramsDrawer();
        trackerClient.searchAndPerform(new JiraClient.Performer() {
            @Override
            public boolean perform(ITicket ticket) throws Exception {
                Set<String> keys = IssuesIDsParser.extractAllJiraIDs(ticket.getTicketDescription());
                List<ITicket> extraTickets = new ArrayList<>();
                if (!keys.isEmpty()) {
                    for (String key : keys) {
                        extraTickets.add(trackerClient.performTicket(key, trackerClient.getExtendedQueryFields()));
                    }
                }
                List<Diagram> diagrams = jAssistant.createDiagrams(ticket, extraTickets, roleSpecific, projectSpecific);
                for (Diagram diagram : diagrams) {
                    File screenshot = diagramsDrawer.draw(ticket.getTicketTitle() + "_" + diagram.getType(), diagram);
                    trackerClient.attachFileToTicket(ticket.getTicketKey(), screenshot.getName(), null, screenshot);
                    trackerClient.postCommentIfNotExists(ticket.getKey(), diagram.getType() + " \n " + diagram.getCode());

                }
                trackerClient.addLabelIfNotExists(ticket, labelNameToMarkAsReviewed);

                return false;
            }
        }, storiesJql, trackerClient.getExtendedQueryFields());
    }

}
