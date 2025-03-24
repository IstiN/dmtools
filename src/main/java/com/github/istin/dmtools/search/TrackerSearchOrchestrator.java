package com.github.istin.dmtools.search;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrackerSearchOrchestrator extends AbstractSearchOrchestrator {

    private final TrackerClient<?> trackerClient;

    public TrackerSearchOrchestrator(TrackerClient<?> trackerClient) {
        this.trackerClient = trackerClient;
    }

    @Override
    protected void setupDependencyInjection() {

    }

    @Override
    protected String getFullItemContent(Object item, Object platformContext) {
        ITicket ticket = (ITicket) item;
        try {
            ITicket extendedTicket = trackerClient.performTicket(ticket.getTicketKey(), trackerClient.getExtendedQueryFields());
            return extendedTicket.toText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getItemResourceKey(Object item) {
        return ((ITicket) item).getKey();
    }

    @Override
    public List<?> searchItemsWithKeywords(String keyword, Object platformContext, int itemsLimit) throws Exception {
        String jql = String.format("summary ~ \"%s\" OR description ~ \"%s\"", keyword, keyword);
        final List<ITicket> results = new ArrayList<>();
        trackerClient.searchAndPerform((JiraClient.Performer) ticket -> {
            results.add(ticket);
            if (results.size() >= itemsLimit) {
                return true;
            }
            return false;
        }, jql, new String[]{"summary", "description"});
        return results;
    }

    @Override
    public Object createInitialPlatformContext() {
        return trackerClient;
    }

    @Override
    protected Object getItemByKey(Object key, List<?> items) {
        for (Object o : items) {
            ITicket ticket = (ITicket) o;
            if (ticket.getTicketKey().equalsIgnoreCase((String) key)) {
                return ticket;
            }
        }
        try {
            return trackerClient.performTicket(String.valueOf(key), trackerClient.getDefaultQueryFields());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected String getKeyFieldValue() {
        return "key";
    }

    @Override
    protected String getSourceType() {
        return "tracker";
    }
}