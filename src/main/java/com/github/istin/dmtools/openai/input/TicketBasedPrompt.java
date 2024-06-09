package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.IAttachment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.utils.HtmlCleaner;

import java.util.ArrayList;
import java.util.List;

public class TicketBasedPrompt {

    private final String basePath;
    private ITicket ticket;
    private String attachmentsDescription;

    private List<ITicket> testCases = new ArrayList<>();

    public TicketBasedPrompt(String basePath, ITicket ticket) {
        this.basePath = basePath;
        this.ticket = new TicketWrapper(ticket);
    }

    public ITicket getTicket() {
        return ticket;
    }

    public void setTicket(ITicket ticket) {
        this.ticket = new TicketWrapper(ticket);
    }


    public List<? extends ITicket> getTestCases() {
        return testCases;
    }

    public void addTestCase(ITicket testCase) {
        testCases.add(new TicketWrapper(testCase));
    }

    public void setTestCases(List<? extends ITicket> testCases) {
        this.testCases.clear();
        for (ITicket testCase : testCases) {
            this.testCases.add(new TicketWrapper(testCase));
        }
    }

    public class TicketWrapper extends ITicket.Wrapper {

        public TicketWrapper(ITicket ticket) {
            super(ticket);
        }

        @Override
        public String getTicketDescription() {
            String ticketDescription = getWrapped().getTicketDescription();
            List<? extends IAttachment> attachments = getWrapped().getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                for (IAttachment attachment : attachments) {
                    ticketDescription = ticketDescription + ("\n" + attachment.getName() + " " + attachment.getUrl());
                }
            }
            ticketDescription = ticketDescription + "\n" + attachmentsDescription;
            return HtmlCleaner.cleanAllHtmlTags(basePath, ticketDescription);
        }

    }

    public String getAttachmentsDescription() {
        return attachmentsDescription;
    }

    public void setAttachmentsDescription(String attachmentsDescription) {
        this.attachmentsDescription = attachmentsDescription;
    }
}
