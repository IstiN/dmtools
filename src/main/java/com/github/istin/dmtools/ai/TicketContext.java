package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TicketContext {

    private final TrackerClient<? extends ITicket> trackerClient;
    private final ITicket ticket;

    private List<ITicket> extraTickets = new ArrayList<>();

    public TicketContext(TrackerClient<? extends ITicket> trackerClient, ITicket ticket) {
        this.trackerClient = trackerClient;
        this.ticket = ticket;
    }

    public void prepareContext() throws IOException {
        Set<String> keys = IssuesIDsParser.extractAllJiraIDs(ticket.getTicketDescription());
        extraTickets = new ArrayList<>();
        if (!keys.isEmpty()) {
            for (String key : keys) {
                extraTickets.add(trackerClient.performTicket(key, trackerClient.getExtendedQueryFields()));
            }
        }
    }

    public ITicket getTicket() {
        return ticket;
    }

    public List<ITicket> getExtraTickets() {
        return extraTickets;
    }

    public void setExtraTickets(List<ITicket> extraTickets) {
        this.extraTickets = extraTickets;
    }
}
