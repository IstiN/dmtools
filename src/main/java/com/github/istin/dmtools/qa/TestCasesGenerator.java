package com.github.istin.dmtools.qa;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
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
        runJob(params.getConfluenceRootPage(), params.getEachPagePrefix(), params.getStoriesJQL(), params.getExistingTestCasesJQL(), params.getOutputType(), params.getTestCasesPriorities());
    }

    public static void runJob(String confluenceRootPage, String eachPagePrefix, String storiesJQL, String existingTestCasesJQL, String outputType, String testCasesPriorities) throws Exception {
        BasicConfluence confluence = BasicConfluence.getInstance();
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();

        List<? extends ITicket> listOfAllTestCases = trackerClient.searchAndPerform(existingTestCasesJQL, new String[]{Fields.SUMMARY});
        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();

        JAssistant jAssistant = new JAssistant(trackerClient, null, openAI, promptManager);

        trackerClient.searchAndPerform(ticket -> {
            generateTestCases(confluenceRootPage, eachPagePrefix, ticket, jAssistant, conversationObserver, confluence, listOfAllTestCases, outputType, testCasesPriorities);
            return false;
        }, storiesJQL, trackerClient.getDefaultQueryFields());
    }

    public static void generateTestCases(String confluenceRootPage, String eachPagePrefix, ITicket ticket, JAssistant jAssistant, ConversationObserver conversationObserver, BasicConfluence confluence, List<? extends ITicket> listOfAllTestCases, String outputType, String testCasesPriorities) throws Exception {
        jAssistant.generateTestCases(ticket, listOfAllTestCases, outputType, testCasesPriorities);
        List<ConversationObserver.Message> messages = conversationObserver.getMessages();
        if (!messages.isEmpty()) {
            GenericReport genericReport = new GenericReport();
            genericReport.setName(eachPagePrefix + " " + ticket.getKey());
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
