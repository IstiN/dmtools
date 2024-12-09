package com.github.istin.dmtools.ba;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import org.json.JSONArray;

import java.io.IOException;
import java.util.List;

public class UserStoryGenerator extends AbstractJob<UserStoryGeneratorParams> {


    @Override
    public void runJob(UserStoryGeneratorParams params) throws Exception {
        runJob(params.getInputJQL(), params.getExistingUserStoriesJql(), params.getOutputType(), params.getPriorities(), params.getInitiator(), params.getProjectCode(), params.getIssueType(), params.getAcceptanceCriteriaField(), params.getRelationship());
    }

    public static void runJob(String inputJQL, String existingUserStoriesJQL, String outputType, String priorities, String initiator, String projectCode, String issueType, String acceptanceCriteriaField, String relationship) throws Exception {
        BasicConfluence confluence = BasicConfluence.getInstance();
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();

        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();

        JAssistant jAssistant = new JAssistant(trackerClient, null, openAI, promptManager);

        trackerClient.searchAndPerform(ticket -> {
            List<? extends ITicket> existingUserStories = trackerClient.searchAndPerform(existingUserStoriesJQL, trackerClient.getExtendedQueryFields());
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.setOnTicketDetailsRequest(new TicketContext.OnTicketDetailsRequest() {
                @Override
                public ITicket getTicketDetails(String key) throws IOException {
                    for (ITicket ticket : existingUserStories) {
                        if (ticket.getKey().equalsIgnoreCase(key)) {
                            return ticket;
                        }
                    }
                    return null;
                }
            });
            ticketContext.prepareContext();
            JSONArray updatedStories = jAssistant.generateUserStories(
                    ticketContext,
                    existingUserStories,
                    projectCode,
                    issueType,
                    acceptanceCriteriaField,
                    relationship,
                    outputType,
                    priorities
            );
            if (updatedStories != null) {
                String comment = trackerClient.tag(initiator) + ", user stories are generated and linked.\n\n";
                if (!updatedStories.isEmpty()) {
                    comment += "Following user stories are updated:";
                }
                for (int i = 0; i < updatedStories.length(); i++) {
                    comment += "\n" + updatedStories.getJSONObject(i).getString("key");
                }
                trackerClient.postCommentIfNotExists(ticket.getTicketKey(), comment);
            } else {
                trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", user stories are generated and linked.");
            }
            return false;
        }, inputJQL, trackerClient.getExtendedQueryFields());
    }

}
