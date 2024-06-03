package com.github.istin.dmtools.atlassian.jira;

import com.github.istin.dmtools.atlassian.jira.model.Fields;
import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.PropertyReader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class BasicJiraClient extends JiraClient<Ticket> {

    public static final String BASE_PATH;
    public static final String TOKEN;
    public static final String AUTH_TYPE;

    static {
        BASE_PATH = new PropertyReader().getJiraBasePath();
        TOKEN = new PropertyReader().getJiraLoginPassToken();
        AUTH_TYPE = new PropertyReader().getJiraAuthType();
    }


    private static BasicJiraClient instance;

    public static TrackerClient<? extends ITicket> getInstance() throws IOException {
        if (instance == null) {
            BasicJiraClient basicJiraClient = new BasicJiraClient();
            instance = basicJiraClient;
        }
        return instance;
    }

    public BasicJiraClient() throws IOException {
        super(BASE_PATH, TOKEN);
        if (AUTH_TYPE != null) {
            setAuthType(AUTH_TYPE);
        }
    }

    @Override
    public void deleteCommentIfExists(String ticketKey, String comment) throws IOException {

    }

    @Override
    public String[] getDefaultQueryFields() {
        return new String[0];
    }

    @Override
    public String[] getExtendedQueryFields() {
        return new String[] {
                Fields.DESCRIPTION,
                Fields.SUMMARY,
                Fields.STATUS,
                Fields.ATTACHMENT,
                Fields.UPDATED,
                Fields.CREATED,
                Fields.CREATOR,
                Fields.STORY_POINTS
        };
    }

    @Override
    public List<? extends ITicket> getTestCases(ITicket ticket) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public TextType getTextType() {
        return TextType.MARKDOWN;
    }
}
