package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.model.ToText;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.openai.input.TicketBasedPrompt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TicketContext implements ToText {

    public static interface OnTicketDetailsRequest {
        ITicket getTicketDetails(String key) throws IOException;
    }

    private final TrackerClient<? extends ITicket> trackerClient;
    private final ITicket ticket;
    private OnTicketDetailsRequest onTicketDetailsRequest;

    private List<ITicket> extraTickets = new ArrayList<>();

    public OnTicketDetailsRequest getOnTicketDetailsRequest() {
        return onTicketDetailsRequest;
    }

    public void setOnTicketDetailsRequest(OnTicketDetailsRequest onTicketDetailsRequest) {
        this.onTicketDetailsRequest = onTicketDetailsRequest;
    }

    public TicketContext(TrackerClient<? extends ITicket> trackerClient, ITicket ticket) {
        this.trackerClient = trackerClient;
        this.ticket = ticket;
    }

    public void prepareContext() throws IOException {
        Set<String> keys = IssuesIDsParser.extractAllJiraIDs(ticket.toText());
        extraTickets = new ArrayList<>();
        if (!keys.isEmpty()) {
            for (String key : keys) {
                if (key.equalsIgnoreCase(ticket.getKey())) {
                    continue;
                }

                try {
                    ITicket e = null;
                    if (onTicketDetailsRequest != null) {
                        e = onTicketDetailsRequest.getTicketDetails(key);
                    }
                    if (e == null) {
                        e = trackerClient.performTicket(key, trackerClient.getExtendedQueryFields());
                    }
                    extraTickets.add(e);
                } catch (AtlassianRestClient.JiraException e) {

                }
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

    @Override
    public String toText() throws IOException {
        StringBuilder text = new StringBuilder(new TicketBasedPrompt.TicketWrapper(trackerClient.getBasePath(), ticket).toText());
        for (ITicket extraTicket : extraTickets) {
            text.append("\n").append(new TicketBasedPrompt.TicketWrapper(trackerClient.getBasePath(), extraTicket).toText());
        }
        return text.toString();
    }
}
