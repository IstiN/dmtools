package com.github.istin.dmtools.documentation.area;

import com.github.istin.dmtools.common.model.ITicket;

import java.io.IOException;

public interface ITicketDocumentationHistoryTracker {

    boolean isTicketWasAddedToPage(ITicket ticket, String pageName) throws IOException;

    void addTicketToPageHistory(ITicket ticket, String pageName) throws IOException;

}
