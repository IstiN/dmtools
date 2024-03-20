package com.github.istin.dmtools.broadcom.rally;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.broadcom.rally.model.*;
import com.github.istin.dmtools.common.model.IChangelog;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public abstract class RallyClient extends AbstractRestClient implements TrackerClient<RallyIssue> {

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
        return new GenericRequest(this, path("search/enhanced?query=" + query), "fetch");
    }
    public void clearCacheForIssue(String formattedID, String[] fields) {
        GenericRequest genericRequest = search("(FormattedId = " + formattedID+")");
        genericRequest.fields(fields);
        clearCache(genericRequest);
    }

    public RallyIssue getIssue(String formattedID, String[] fields) throws IOException {
        List<RallyIssue> issues = new RallyResponse(search("(FormattedId = " + formattedID+")", fields)).getQueryResult().getIssues();
        if (issues == null || issues.isEmpty()) {
            return null;
        }
        for (RallyIssue issue : issues) {
            if (issue.getTicketKey().equalsIgnoreCase(formattedID)) {
                return issue;
            }
        }
        return null;
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
    public IChangelog getChangeLog(String ticketKey, ITicket ticket) throws IOException {
        RallyIssue issue = getIssue(ticketKey, getDefaultQueryFields());
        String ref = issue.getRevisionHistory().getRef();
        GenericRequest genericRequest = new GenericRequest(this, ref + "/revisions")
                .param("start", 1)
                .param("pagesize", 500);
        return new RallyResponse(genericRequest.execute()).getQueryResult();
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
                .param("start", String.valueOf(startAt))
                .param("pagesize", 500);

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
        RallyIssueType.QueryAndTypes queryAndTypes = RallyIssueType.parseQuery(searchQuery);
        searchQuery = queryAndTypes.getQuery();

        int startAt = 1;
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
        while (startAt == 1 || startAt < total) {
            startAt = startAt + maxResults;
            List<RallyIssue> tickets = searchResults.getQueryResult().getIssues();
            for (RallyIssue ticket : tickets) {
                List<String> types = queryAndTypes.getTypes();

                if (types != null) {
                    if (types.contains(ticket.getType())) {
                        isBreak = performer.perform(createTicket(ticket));
                    }
                } else {
                    isBreak = performer.perform(createTicket(ticket));
                }

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
    public void postCommentIfNotExists(String ticketKey, String text) throws IOException {
        List<? extends IComment> comments = getComments(ticketKey, null);
        if (comments != null) {
            for (IComment commentObject : comments) {
                if (text.equalsIgnoreCase(commentObject.getBody())) {
                    return;
                }
            }
        }
        postComment(ticketKey, text);
    }

    @Override
    public List<? extends IComment> getComments(String ticketKey, RallyIssue ticket) throws IOException {
        if (ticket == null) {
            ticket = performTicket(ticketKey, getDefaultQueryFields());
        }

        String ref = ticket.getRef();
        String url = ref + "/Discussion";
        GenericRequest genericRequest = new GenericRequest(this, url)
                .param("order", "CreationDate DESC")
                .param("pagesize", 100)
                .param("start", 1);

        clearRequestIfExpired(genericRequest, ticket.getUpdatedAsMillis());
        String response = genericRequest.execute();
        return new RallyResponse(response).getQueryResult().getComments();
    }

    @Override
    public void postComment(String ticketKey, String comment) throws IOException {
        RallyIssue ticket = performTicket(ticketKey, getDefaultQueryFields());
        String ref = ticket.getRef();
        JSONObject body = new JSONObject().put("ConversationPost",
                new JSONObject()
                        .put("Artifact", ref)
                        .put("Text", comment));
        GenericRequest genericRequest = new GenericRequest(this, path("conversationpost/create"));
        genericRequest.setBody(body.toString());
        post(genericRequest);

        String url = ref + "/Discussion";
        clearCache(new GenericRequest(this, url));
    }

    @Override
    public String moveToStatus(String ticketKey, String statusName) throws IOException {
        RallyIssue rallyIssue = performTicket(ticketKey, getDefaultQueryFields());
        if (rallyIssue.getStatus().equalsIgnoreCase(statusName)) {
            return null;
        }

        GenericRequest genericRequest = new GenericRequest(this, path("flowstate"));
        genericRequest
                .param("order", "OrderIndex ASC")
                .param("pagesize", 50)
                .param("start", 1)
                .param("query", "(Project.Name = \""+rallyIssue.getProjectName()+"\")");
        String response = genericRequest.execute();
        List<FlowState> flowStates = new RallyResponse(response).getQueryResult().getFlowStates();
        for (FlowState flowState : flowStates) {
            if (flowState.getRefObjectName().equalsIgnoreCase(statusName)) {
                GenericRequest updateFlowState = new GenericRequest(this, rallyIssue.getRef());
                updateFlowState.setBody(new JSONObject()
                        .put(rallyIssue.getType(),
                                new JSONObject().put(RallyFields.FLOW_STATE, flowState.getRef())
                        ).toString());
                String post = updateFlowState.post();
                clearCacheForIssue(ticketKey, getDefaultQueryFields());
                return post;
            }
        }
        return null;
    }

    public List<Iteration> iterations(String projectName, Calendar startDateCalendar) throws IOException {
        String startDate = DateUtils.formatToRallyDate(startDateCalendar);
//        String endDate = "And (EndDate > 2023-12-20T23:00:00.000Z)";
        String endDate = "";
        GenericRequest genericRequest = new GenericRequest(this, path("iteration?fetch=StartDate,EndDate,Name,Project,State&query=((Project.Name Contains \""+projectName+ "\") And (StartDate > " + startDate + "))" + endDate + "&order=StartDate ASC"))
                .param("start", 1)
                .param("pagesize", 500);
        QueryResult queryResult = new RallyResponse(genericRequest.execute()).getQueryResult();
        System.out.println(queryResult.getJSONObject());
        return queryResult.getIterations();
    }

    @Override
    public String getDefaultStatusField() {
        return "FLOW STATE";
    }
}
