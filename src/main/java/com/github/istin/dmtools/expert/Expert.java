package com.github.istin.dmtools.expert;

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

import java.util.List;

import static com.github.istin.dmtools.expert.ExpertParams.OUTPUT_TYPE_FIELD;

public class Expert extends AbstractJob<ExpertParams> {

    @Override
    public void runJob(ExpertParams expertParams) throws Exception {
        runJob(expertParams.getInputJQL(), expertParams.getProjectContext(), expertParams.getRequest(), expertParams.getInitiator(), expertParams.getOutputType(), expertParams.getFieldName(), expertParams.getConfluencePages());
    }

    public static void runJob(String inputJQL, String projectContext, String request, String initiator, String outputType, String fieldName, String... confluencePages) throws Exception {
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();

        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();
        BasicConfluence confluence = BasicConfluence.getInstance();

        if (projectContext.startsWith("https://")) {
            projectContext = confluence.contentByUrl(projectContext).getStorage().getValue();
        }

        StringBuilder requestWithContext = new StringBuilder();
        requestWithContext.append(request).append("\n");
        if (confluencePages != null) {
            List<Content> contents = confluence.contentsByUrls(confluencePages);
            requestWithContext.append("Existing confluence pages\n");
            for (Content content : contents) {
                requestWithContext.append(content.getTitle()).append("\n").append(content.getStorage().getValue()).append("\n");
            }
        }

        JAssistant jAssistant = new JAssistant(trackerClient, null, openAI, promptManager);

        String finalProjectContext = projectContext;
        trackerClient.searchAndPerform(ticket -> {
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext();
            String response = jAssistant.makeResponseOnRequest(ticketContext, finalProjectContext, requestWithContext.toString());
            if (OUTPUT_TYPE_FIELD.equals(outputType)) {
                String fieldCustomCode = ((JiraClient) BasicJiraClient.getInstance()).getFieldCustomCode(ticket.getTicketKey().split("-")[0], fieldName);
                trackerClient.updateTicket(ticket.getTicketKey(), fields -> fields.set(fieldCustomCode, response));
                trackerClient.postComment(ticket.getTicketKey(), trackerClient.tag(initiator) + ", there is response in '"+ fieldName + "' on your request: \n" + request);
            } else {
                trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", there is response on your request: \n" + request + "\n\nAI Response is: \n" + response);
            }
            return false;
        }, inputJQL, trackerClient.getExtendedQueryFields());
    }

}