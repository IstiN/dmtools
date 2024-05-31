package com.github.istin.dmtools.qa;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.report.freemarker.GenericCell;
import com.github.istin.dmtools.report.freemarker.GenericReport;
import com.github.istin.dmtools.report.freemarker.GenericRow;

import java.util.List;

public class TestCasesGenerationJob {

    public static void generateTestCases(ITicket ticket, JAssistant jAssistant, ConversationObserver conversationObserver, BasicConfluence confluence, List<? extends ITicket> listOfAllTestCases) throws Exception {
        jAssistant.generateTestCases(ticket.getKey(), listOfAllTestCases);
        List<ConversationObserver.Message> messages = conversationObserver.getMessages();
        if (!messages.isEmpty()) {
            GenericReport genericReport = new GenericReport();
            genericReport.setName("JAI QA " + ticket.getKey());
            for (ConversationObserver.Message message : messages) {
                GenericRow row = new GenericRow(false);
                row.getCells().add(new GenericCell("<b>" + message.getAuthor() + "</b>"));
                row.getCells().add(new GenericCell(message.getText()));
                genericReport.getRows().add(row);
            }
            conversationObserver.printAndClear();
            confluence.publishPageToDefaultSpace("JAI", "JAI QA", genericReport);
        }
    }
}
