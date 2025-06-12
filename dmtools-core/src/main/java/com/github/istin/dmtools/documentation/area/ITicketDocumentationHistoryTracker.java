package com.github.istin.dmtools.documentation.area;

import com.github.istin.dmtools.common.model.TicketLink;

import java.io.IOException;

public interface ITicketDocumentationHistoryTracker {

    boolean isTicketWasAddedToPage(TicketLink ticketLink, String pageName) throws IOException;

    void addTicketToPageHistory(TicketLink ticketLink, String pageName) throws IOException;

}
