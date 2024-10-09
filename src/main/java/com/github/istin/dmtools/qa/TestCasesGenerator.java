package com.github.istin.dmtools.qa;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.model.Fields;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.report.freemarker.GenericCell;
import com.github.istin.dmtools.report.freemarker.GenericReport;
import com.github.istin.dmtools.report.freemarker.GenericRow;

import java.util.List;

public class TestCasesGenerator extends AbstractJob<TestCasesGeneratorParams> {

    @Override
    public void runJob(TestCasesGeneratorParams params) throws Exception {
        runJob(params.getConfluenceRootPage(), params.getEachPagePrefix(), params.getStoriesJQL(), params.getExistingTestCasesJQL(), params.getOutputType(), params.getTestCasesPriorities(), params.getInitiator());
    }

    public static void runJob(String confluenceRootPage, String eachPagePrefix, String storiesJQL, String existingTestCasesJQL, String outputType, String testCasesPriorities, String initiator) throws Exception {
        BasicConfluence confluence = BasicConfluence.getInstance();
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();

        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();

        JAssistant jAssistant = new JAssistant(trackerClient, null, openAI, promptManager);

        trackerClient.searchAndPerform(ticket -> {
            List<? extends ITicket> listOfAllTestCases = trackerClient.searchAndPerform(existingTestCasesJQL, new String[]{Fields.SUMMARY, Fields.DESCRIPTION});
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext();
            generateTestCases(confluenceRootPage, eachPagePrefix, ticketContext, jAssistant, conversationObserver, confluence, listOfAllTestCases, outputType, testCasesPriorities);
            trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", similar test cases are linked and new test cases are generated.");
            return false;
        }, storiesJQL, trackerClient.getExtendedQueryFields());
    }

    public static void generateTestCases(String confluenceRootPage, String eachPagePrefix, TicketContext ticketContext, JAssistant jAssistant, ConversationObserver conversationObserver, BasicConfluence confluence, List<? extends ITicket> listOfAllTestCases, String outputType, String testCasesPriorities) throws Exception {
        jAssistant.generateTestCases(ticketContext, listOfAllTestCases, outputType, testCasesPriorities);
        List<ConversationObserver.Message> messages = conversationObserver.getMessages();
        if (confluenceRootPage == null || eachPagePrefix == null || confluenceRootPage.isEmpty() || eachPagePrefix.isEmpty()) {
            messages.clear();
            return;
        }

        if (!messages.isEmpty()) {
            GenericReport genericReport = new GenericReport();
            genericReport.setIsNotWiki(false);
            genericReport.setName(eachPagePrefix + " " + ticketContext.getTicket().getKey());
            for (ConversationObserver.Message message : messages) {
                GenericRow row = new GenericRow(false);
                row.getCells().add(new GenericCell("<b>" + message.getAuthor() + "</b>"));
                row.getCells().add(new GenericCell(message.getText()));
                genericReport.getRows().add(row);
            }
            conversationObserver.printAndClear();
            confluence.publishPageToDefaultSpace(confluenceRootPage, eachPagePrefix, genericReport);
        }
    }
}
