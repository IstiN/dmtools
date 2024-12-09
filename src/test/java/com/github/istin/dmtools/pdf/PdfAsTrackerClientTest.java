package com.github.istin.dmtools.pdf;

import com.github.istin.dmtools.common.model.IAttachment;
import com.github.istin.dmtools.pdf.model.PdfPageAsTicket;
import junit.framework.TestCase;

import java.util.List;

public class PdfAsTrackerClientTest extends TestCase {

    private PdfAsTrackerClient pdfAsTrackerClient;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        pdfAsTrackerClient = new PdfAsTrackerClient("test_data/test_pdf_sources");
    }

    public void testMain() throws Exception {
        List<PdfPageAsTicket> pdfPageAsTickets = pdfAsTrackerClient.searchAndPerform(null, null);
        for (PdfPageAsTicket tickets : pdfPageAsTickets) {
            System.out.println(tickets.getKey());
            System.out.println(tickets.getTicketDescription());
            System.out.println(tickets.getPageSnapshot());
            List<? extends IAttachment> attachments = tickets.getAttachments();
            System.out.println("attachments");
            for (IAttachment attachment : attachments) {
                System.out.println(attachment.getUrl());
            }
        }
        if (pdfPageAsTickets.isEmpty()) return;

        PdfPageAsTicket pdfPageAsTicket = pdfPageAsTickets.get(0);
        pdfAsTrackerClient.addLabelIfNotExists(pdfPageAsTicket, "test label");
        pdfAsTrackerClient.deleteLabelInTicket(pdfPageAsTicket, "test label");
    }
}