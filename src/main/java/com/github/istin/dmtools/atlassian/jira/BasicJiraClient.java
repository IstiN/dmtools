package com.github.istin.dmtools.atlassian.jira;

import com.github.istin.dmtools.atlassian.jira.model.Fields;
import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.PropertyReader;
import org.apache.commons.collections.ArrayStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BasicJiraClient extends JiraClient<Ticket> {

    public static final String BASE_PATH;
    public static final String TOKEN;
    public static final String AUTH_TYPE;

    private static final boolean IS_JIRA_LOGGING_ENABLED;
    private static final boolean IS_JIRA_CLEAR_CACHE;

    private static final boolean IS_JIRA_WAIT_BEFORE_PERFORM;

    private static final Long SLEEP_TIME_REQUEST;

    private static final String[] JIRA_EXTRA_FIELDS;

    private static final String JIRA_EXTRA_FIELDS_PROJECT;

    public static final String[] DEFAULT_QUERY_FIELDS = {
            Fields.SUMMARY,
            Fields.STATUS,
            Fields.ATTACHMENT,
            Fields.UPDATED,
            Fields.CREATED,
            Fields.CREATOR,
            Fields.REPORTER,
            Fields.COMPONENTS,
            Fields.ISSUETYPE,
            Fields.STORY_POINTS,
            Fields.LABELS,
            Fields.PARENT
    };
    public static final String[] EXTENDED_QUERY_FIELDS = {
            Fields.DESCRIPTION
    };

    static {
        PropertyReader propertyReader = new PropertyReader();
        BASE_PATH = propertyReader.getJiraBasePath();
        TOKEN = propertyReader.getJiraLoginPassToken();
        AUTH_TYPE = propertyReader.getJiraAuthType();
        IS_JIRA_LOGGING_ENABLED = propertyReader.isJiraLoggingEnabled();
        IS_JIRA_CLEAR_CACHE = propertyReader.isJiraClearCache();
        IS_JIRA_WAIT_BEFORE_PERFORM = propertyReader.isJiraWaitBeforePerform();
        SLEEP_TIME_REQUEST = propertyReader.getSleepTimeRequest();
        JIRA_EXTRA_FIELDS = propertyReader.getJiraExtraFields();
        JIRA_EXTRA_FIELDS_PROJECT = propertyReader.getJiraExtraFieldsProject();
    }


    private static BasicJiraClient instance;
    private final String[] defaultJiraFields;
    private final String[] extendedJiraFields;


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
        setLogEnabled(IS_JIRA_LOGGING_ENABLED);
        setWaitBeforePerform(IS_JIRA_WAIT_BEFORE_PERFORM);
        setSleepTimeRequest(SLEEP_TIME_REQUEST);
        setClearCache(IS_JIRA_CLEAR_CACHE);

        List<String> defaultFields = new ArrayList<>();
        defaultFields.addAll(Arrays.asList(DEFAULT_QUERY_FIELDS));

        if (JIRA_EXTRA_FIELDS_PROJECT != null) {
            for (String extraField : JIRA_EXTRA_FIELDS) {
                defaultFields.add(getFieldCustomCode(JIRA_EXTRA_FIELDS_PROJECT, extraField));
            }
        }

        defaultJiraFields = defaultFields.toArray(new String[0]);

        List<String> extendedFields = new ArrayList<>();
        extendedFields.addAll(Arrays.asList(EXTENDED_QUERY_FIELDS));
        extendedFields.addAll(defaultFields);

        extendedJiraFields = extendedFields.toArray(new String[0]);
    }


    @Override
    public void deleteCommentIfExists(String ticketKey, String comment) throws IOException {

    }

    @Override
    public String[] getDefaultQueryFields() {
        return defaultJiraFields;
    }

    @Override
    public String[] getExtendedQueryFields() {
        return extendedJiraFields;
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
