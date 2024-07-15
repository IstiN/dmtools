package com.github.istin.dmtools.ba;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.model.Key;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.documentation.DocumentationEditor;
import com.github.istin.dmtools.documentation.area.TicketDocumentationHistoryTrackerViaConfluence;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RequirementsCollector extends AbstractJob<RequirementsCollectorParams> {
    private static final Logger logger = LogManager.getLogger(RequirementsCollector.class);
    @Override
    public void runJob(RequirementsCollectorParams params) throws Exception {
        runJob(params.getRoleSpecific(), params.getProjectSpecific(), params.getStoriesJql(), params.getExcludeJQL(), params.getLabelNameToMarkAsReviewed(), params.getEachPagePrefix());
    }

    public static void runJob(String roleSpecific, String projectSpecific, String storiesJql, String excludeJQL, String labelNameToMarkAsReviewed, String eachPagePrefix) throws Exception {
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();
        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();
        JAssistant jAssistant = new JAssistant(trackerClient, null, openAI, promptManager);

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
                String jqlToSearch = jAssistant.buildJQLForContent(trackerClient, roleSpecific, projectSpecific, ticket, extraTickets);
                logger.info(jqlToSearch);
                String researchPageName = makeSearchAndCollectRequirementsToPage(trackerClient, jAssistant, jqlToSearch, excludeJQL, ticket, extraTickets, roleSpecific, projectSpecific, eachPagePrefix);
                BasicConfluence confluence = BasicConfluence.getInstance();
                String viewUrl = confluence.findContent(researchPageName).getViewUrl(confluence.getBasePath());
                trackerClient.postCommentIfNotExists(ticket.getKey(), "Research in Existing Jira Tickets Was Done " + viewUrl);
                trackerClient.addLabelIfNotExists(ticket, labelNameToMarkAsReviewed);
                return false;
            }
        }, storiesJql, trackerClient.getExtendedQueryFields());
    }

    private static String makeSearchAndCollectRequirementsToPage(TrackerClient<? extends ITicket> trackerClient, JAssistant jAssistant, String jqlToSearch, String excludeJQL, ITicket feature, List<ITicket> extraTickets, String roleSpecific, String projectSpecific, String eachPagePrefix) throws Exception {
        List<Key> keys = new ArrayList<>();
        keys.add(feature);
        keys.addAll(extraTickets);
        String jqlKeysNotIn = JiraClient.buildNotInJQLByKeys(keys);
        jqlToSearch = "(" + jqlToSearch + ") and " + jqlKeysNotIn ;
        if (excludeJQL != null && !excludeJQL.isEmpty()) {
            jqlToSearch += " and " + excludeJQL;
        }

        trackerClient.postCommentIfNotExists(feature.getTicketKey(), "Research query: " + trackerClient.buildUrlToSearch(jqlToSearch));

        List<ITicket> relatedTickets = new ArrayList<>();
        final int[] i = {0};

        List<ITicket> ticketsToCheck = new ArrayList<>();

        trackerClient.searchAndPerform(new JiraClient.Performer() {


            @Override
            public boolean perform(ITicket ticket) throws Exception {
                logger.info("Progress : {}", i[0]);
                ticketsToCheck.add(ticket);
                if (ticketsToCheck.size() == 50) {
                    List<ITicket> tickets = jAssistant.checkSimilarTickets(roleSpecific, ticketsToCheck, false, feature, extraTickets);
                    relatedTickets.addAll(tickets);
                    ticketsToCheck.clear();
                }
                i[0]++;
                return false;
            }
        }, jqlToSearch, trackerClient.getExtendedQueryFields());

        if (!ticketsToCheck.isEmpty()) {
            List<ITicket> tickets = jAssistant.checkSimilarTickets(roleSpecific, ticketsToCheck, false, feature, extraTickets);
            relatedTickets.addAll(tickets);
        }

        BasicConfluence confluence = BasicConfluence.getInstance();
        DocumentationEditor documentationEditor = new DocumentationEditor(jAssistant, trackerClient, confluence, eachPagePrefix);
        Content rootContent = confluence.findContent(eachPagePrefix);
        TicketDocumentationHistoryTrackerViaConfluence ticketDocumentationHistoryTrackerViaConfluence = new TicketDocumentationHistoryTrackerViaConfluence(confluence);
        String researchPageName = eachPagePrefix + " " + feature.getKey();

        List<ITicket> finalList = new ArrayList<>();
        for (ITicket relatedTicket :relatedTickets) {
            boolean isRelated = jAssistant.baIsTicketRelatedToContent(trackerClient, roleSpecific, projectSpecific, feature, extraTickets, relatedTicket);
            if (isRelated) {
                finalList.add(relatedTicket);
            }
        }

        String searchQuery = JiraClient.buildJQLByKeys(finalList);
        trackerClient.postCommentIfNotExists(feature.getTicketKey(), "Filtered tickets: " + trackerClient.buildUrlToSearch(searchQuery));

        if (!finalList.isEmpty()) {
            trackerClient.searchAndPerform(new JiraClient.Performer() {
                @Override
                public boolean perform(ITicket content) throws Exception {
                    documentationEditor.extendDocumentationPageWithTicket(confluence, ticketDocumentationHistoryTrackerViaConfluence, content, researchPageName, rootContent, source ->
                            jAssistant.buildPageWithRequirementsForInputData(feature, extraTickets, roleSpecific, projectSpecific, source, content));
                    return false;
                }
            }, searchQuery, trackerClient.getExtendedQueryFields());
        }
        return researchPageName;
    }
}
