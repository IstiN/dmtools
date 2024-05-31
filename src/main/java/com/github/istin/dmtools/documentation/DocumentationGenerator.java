package com.github.istin.dmtools.documentation;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.documentation.area.TicketAreaMapperViaConfluence;
import com.github.istin.dmtools.documentation.area.TicketDocumentationHistoryTrackerViaConfluence;
import com.github.istin.dmtools.figma.BasicFigmaClient;
import com.github.istin.dmtools.figma.FigmaClient;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.report.model.KeyTime;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DocumentationGenerator {

    public static void runJob(DocumentationGeneratorParams params) throws Exception {
        runJob(params.getConfluenceRootPage(), params.getEachPagePrefix(), params.getJQL(), params.getListOfStatusesToSort());
    }

    public static void runJob(String confluenceRootPage, String eachPagePrefix, String jql, String[] listOfStatusesToSort) throws Exception {
        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAIClient = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();
        TrackerClient tracker = BasicJiraClient.getInstance();
        BasicConfluence confluence = BasicConfluence.getInstance();
        FigmaClient figma = BasicFigmaClient.getInstance();

        List<? extends ITicket> tickets = retrieveTickets(tracker, jql, listOfStatusesToSort);

        JAssistant jAssistant = new JAssistant(tracker, null, openAIClient, promptManager);

        TicketAreaMapperViaConfluence ticketAreaMapper = new TicketAreaMapperViaConfluence(eachPagePrefix, confluenceRootPage, confluence);
        DocumentationEditor documentationEditor = new DocumentationEditor(jAssistant, tracker,  confluence, eachPagePrefix);

        JSONArray areas = documentationEditor.buildDraftFeatureAreasByStories(tickets);
        areas = documentationEditor.cleanFeatureAreas(areas);
        System.out.println(areas);
        JSONObject optimizedFeatureAreas = documentationEditor.createFeatureAreasTree(areas);
        documentationEditor.buildConfluenceStructure(optimizedFeatureAreas, tickets, confluenceRootPage, confluence, ticketAreaMapper);
        System.out.println(optimizedFeatureAreas);

        TicketDocumentationHistoryTrackerViaConfluence ticketDocumentationHistoryTrackerViaConfluence = new TicketDocumentationHistoryTrackerViaConfluence(confluence);
        documentationEditor.buildPagesForTickets(tickets, eachPagePrefix, confluenceRootPage, confluence, ticketAreaMapper, ticketDocumentationHistoryTrackerViaConfluence, false);

        JSONObject jsonObject = documentationEditor.buildExistingAreasStructureForConfluence(confluenceRootPage);
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            System.out.println(key);
            JSONObject children = jsonObject.getJSONObject(key);
            Set<String> childrenKeys = children.keySet();
            for (String childKey : childrenKeys) {
                System.out.println("===" + childKey);
                documentationEditor.attachImagesForPage(childKey, tracker, figma);
            }
        }
    }

    public static @NotNull List<? extends ITicket> retrieveTickets(TrackerClient tracker, String jql, String[] listOfStatusesToSort) throws Exception {
        List<? extends ITicket> list = tracker.searchAndPerform(jql, tracker.getExtendedQueryFields());
        return list.stream().sorted(Comparator.comparingLong(ticket -> {
                    try {
                        List<KeyTime> datesWhenTicketWasInStatus = ChangelogAssessment.findDatesWhenTicketWasInStatus(tracker, ticket.getKey(), ticket, listOfStatusesToSort);
                        if (datesWhenTicketWasInStatus == null || datesWhenTicketWasInStatus.isEmpty()) {
                            return ticket.getUpdatedAsMillis();
                        }
                        Calendar when = datesWhenTicketWasInStatus.get(datesWhenTicketWasInStatus.size() - 1).getWhen();
                        return when.getTimeInMillis();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .collect(Collectors.toList());
    }

}
