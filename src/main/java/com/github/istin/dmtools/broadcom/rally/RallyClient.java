package com.github.istin.dmtools.broadcom.rally;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.model.Changelog;
import com.github.istin.dmtools.atlassian.jira.model.Comment;
import com.github.istin.dmtools.broadcom.rally.model.RallyIssue;
import com.github.istin.dmtools.broadcom.rally.model.RallyResponse;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RallyClient extends AbstractRestClient implements TrackerClient<RallyIssue> {

    public RallyClient(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
    }

    @Override
    public Request.Builder sign(Request.Builder builder) {
        return builder
                .header("zsessionid", authorization)
                .header("Content-Type", "application/json");
    }

    @Override
    public String path(String path) {
        return basePath + "/slm/webservice/v2.0/" + path;
    }

    public String search(String query, String[] fields) throws IOException {
        GenericRequest genericRequest = search(query);
        genericRequest.fields(fields);
        return execute(genericRequest);
    }

    @NotNull
    private GenericRequest search(String query) {
        return new GenericRequest(this, path("search/enhanced?search=" + query), "fetch");
    }

    public RallyIssue getIssue(String formattedID, String[] fields) throws IOException {
        List<RallyIssue> issues = new RallyResponse(search(formattedID, fields)).getQueryResult().getIssues();
        if (issues == null || issues.isEmpty()) {
            return null;
        }
        return issues.get(0);
    }

    public String getBrowseUrl(RallyIssue issue) {
        return getTicketBrowseUrl(issue.getRef());
    }

    public static String getLastTwoSegments(String url) {
        if (url == null) {
            return null;
        }

        String[] segments = url.split("/");

        if (segments.length < 2) {
            return url;
        }

        return segments[segments.length - 2] + "/" + segments[segments.length - 1];
    }

    @Override
    public String getTicketBrowseUrl(String ticketKey) {
        return basePath + "/#/?detail=/" + getLastTwoSegments(ticketKey) + "&fdp=true";
    }

    @Override
    public String assignTo(String ticketKey, String userName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Changelog getChangeLog(String ticketKey, ITicket ticket) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLabelIfNotExists(RallyIssue ticket, String label) throws IOException {
        throw new UnsupportedOperationException();
    }


    @Override
    public List<RallyIssue> searchAndPerform(String searchQuery, String[] fields) throws Exception {
        List<RallyIssue> tickets = new ArrayList<>();
        searchAndPerform(ticket -> {
            tickets.add(ticket);
            return false;
        }, searchQuery, fields);
        return tickets;
    }

    private Map<String, Integer> jqlExpirationInHours = new HashMap<>();

    public void setCacheExpirationForJQLInHours(String jql, Integer expirationValueInHours) {
        jqlExpirationInHours.put(jql, expirationValueInHours);
    }

    public RallyResponse search(String searchQuery, int startAt, String[] fields) throws IOException {
        GenericRequest searchQuerySearchRequest = search(searchQuery).
                fields(fields)
                .param("StartIndex", String.valueOf(startAt))
                .param("PageSize", 500);

        Integer expired = jqlExpirationInHours.get(searchQuery);
        if (expired != null) {
            clearRequestIfExpired(searchQuerySearchRequest, System.currentTimeMillis() - expired*60*60*1000);
        }


        try {
            String body = searchQuerySearchRequest.execute();
            return new RallyResponse(body);
        } catch (JSONException e) {
            clearCache(searchQuerySearchRequest);
            String body = searchQuerySearchRequest.execute();
            try {
                return new RallyResponse(body);
            } catch (JSONException e1) {
                System.err.println("response: " + body);
                throw e1;
            }
        }

    }

    protected RallyIssue createTicket(String body) {
        return new RallyIssue(body);
    }

    protected RallyIssue createTicket(RallyIssue ticket) {
        return ticket;
    }

    protected Class<RallyIssue> getTicketClass() {
        return RallyIssue.class;
    }

    @Override
    public void searchAndPerform(JiraClient.Performer<RallyIssue> performer, String searchQuery, String[] fields) throws Exception {
        int startAt = 0;
        RallyResponse searchResults = search(searchQuery, startAt, fields);
        JSONArray errorMessages = searchResults.getQueryResult().getErrors();
        if (errorMessages != null && !errorMessages.isEmpty()) {
            System.err.println(errorMessages);
            return;
        }
        int maxResults = searchResults.getQueryResult().getPageSize();
        int total = searchResults.getQueryResult().getTotalResultCount();
        if (total == 0) {
            return;
        }

        boolean isBreak = false;
        int ticketIndex = 0;
        while (startAt == 0 || startAt < total) {
            startAt = startAt + maxResults;
            List<RallyIssue> tickets = searchResults.getQueryResult().getIssues();
            for (RallyIssue ticket : tickets) {
                isBreak = performer.perform(createTicket(ticket));
                if (isBreak) {
                    break;
                }
                ticketIndex++;
            }
            if (isBreak) {
                break;
            }
            if (total < maxResults || startAt > total) {
                break;
            }
            searchResults = search(searchQuery, startAt, fields);
            maxResults = searchResults.getQueryResult().getPageSize();
            total = searchResults.getQueryResult().getTotalResultCount();
        }

    }

    @Override
    public RallyIssue performTicket(String ticketKey, String[] fields) throws IOException {
        return getIssue(ticketKey, fields);
    }

    @Override
    public void postCommentIfNotExists(String ticketKey, String comment) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Comment> getComments(String ticketKey, RallyIssue ticket) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void postComment(String ticketKey, String comment) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String moveToStatus(String ticketKey, String statusName) throws IOException {
        throw new UnsupportedOperationException();
    }
}
