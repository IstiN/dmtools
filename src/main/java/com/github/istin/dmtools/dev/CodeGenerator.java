package com.github.istin.dmtools.dev;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.atlassian.bitbucket.BasicBitbucket;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.report.freemarker.GenericCell;
import com.github.istin.dmtools.report.freemarker.GenericReport;
import com.github.istin.dmtools.report.freemarker.GenericRow;

import java.util.List;

public class CodeGenerator extends AbstractJob<CodeGeneratorParams> {

    @Override
    public void runJob(CodeGeneratorParams params) throws Exception {
        runJob(params.getConfluenceRootPage(), params.getEachPagePrefix(), params.getInputJQL(), params.getInitiator(), params.getRole());
    }

    public static void runJob(String confluenceRootPage, String eachPagePrefix, String inputJQL, String initiator, String role) throws Exception {
        BasicConfluence confluence = BasicConfluence.getInstance();

        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();
        SourceCode basicSourceCode = BasicBitbucket.getInstance();
        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();

        JAssistant jAssistant = new JAssistant(trackerClient, basicSourceCode, openAI, promptManager);

        trackerClient.searchAndPerform(ticket -> {
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext();
            generateCode(confluenceRootPage, eachPagePrefix, role, ticketContext, jAssistant, conversationObserver, confluence, basicSourceCode);
            trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", code is prepared.");
            return false;
        }, inputJQL, trackerClient.getExtendedQueryFields());
    }

    public static void generateCode(String confluenceRootPage, String eachPagePrefix, String role, TicketContext ticketContext, JAssistant jAssistant, ConversationObserver conversationObserver, BasicConfluence confluence, SourceCode basicSourceCode) throws Exception {
        jAssistant.generateCode(role, ticketContext, basicSourceCode.getDefaultWorkspace(), basicSourceCode.getDefaultRepository(), basicSourceCode.getDefaultBranch());
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
