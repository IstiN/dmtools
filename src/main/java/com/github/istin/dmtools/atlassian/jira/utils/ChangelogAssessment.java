package com.github.istin.dmtools.atlassian.jira.utils;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.model.Fields;
import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.broadcom.rally.model.RallyFields;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.report.model.KeyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChangelogAssessment {

    public static List<KeyTime> findCreatedDate(JiraClient jira, String key) throws IOException {
        Ticket ticket = jira.performTicket(key, new String[]{"created", "creator"});
        List<KeyTime> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ticket.getFields().getCreated());
        KeyTime keyTime = new KeyTime(ticket.getKey(), calendar, ticket.getFields().getCreator().getDisplayName());
        result.add(keyTime);
        return result;
    }

    public static List<KeyTime> findDatesWhenTicketWasInStatus(TrackerClient jira, String key, ITicket ticket, String... statuses) throws IOException {
        return findDatesWhenTicketWasInStatus(null, null, false, jira, key, ticket, statuses);
    }

    public static List<KeyTime> findDatesWhenTicketWasInStatus(String customName, boolean isSetWeight, TrackerClient jira, String key, ITicket ticket, String... statuses) throws IOException {
        return findDatesWhenTicketWasInStatus(null, customName, isSetWeight, jira, key, ticket, statuses);
    }


    public static List<KeyTime> findDatesWhenTicketWasInStatus(String ignoreIfStatus, String customName, boolean isSetWeight, TrackerClient trackerClient, String key, ITicket ticket, String... statuses) throws IOException {
        IChangelog changeLog = trackerClient.getChangeLog(key, ticket);
        List<IHistory> histories = (List<IHistory>) changeLog.getHistories();
        List<KeyTime> result = new ArrayList<>();
        boolean ifStatusChanged = false;

        IUser firstAuthor = null;
        for (IHistory history : histories) {
            if (firstAuthor == null) {
                firstAuthor = history.getAuthor();
            }
            List<IHistoryItem> items = (List<IHistoryItem>) history.getHistoryItems();
            for (IHistoryItem historyItem : items) {
                if (historyItem.getField().equalsIgnoreCase(trackerClient.getDefaultStatusField())) {
                    String toString = historyItem.getToAsString();
                    if (toString != null) {
                        ifStatusChanged = true;
                        if (ignoreIfStatus != null && ignoreIfStatus.equalsIgnoreCase(toString)) {
                            result.clear();
                        }
                        for (String status : statuses) {
                            if (toString.equalsIgnoreCase(status)) {
                                Calendar created = history.getCreated();
                                String displayName = history.getAuthor().getFullName();
                                result.add(new KeyTime(key, created, customName != null ? customName : displayName));
                            }
                        }
                    }
                }
            }
        }
        if (!ifStatusChanged) {
            ITicket latestStatusTicket = trackerClient.performTicket(key, trackerClient.getDefaultQueryFields());
            String initialStatus = latestStatusTicket.getStatus();
            Calendar createdCalendar = Calendar.getInstance();
            createdCalendar.setTime(latestStatusTicket.getCreated());
            for (String status : statuses) {
                if (initialStatus.equalsIgnoreCase(status)) {
                    IUser creator = latestStatusTicket.getCreator();
                    if (creator == null) {
                        creator = firstAuthor;
                    }
                    result.add(new KeyTime(key, createdCalendar, creator != null ? creator.getFullName() : null));
                }
            }
        }

        return result;
    }

    public static boolean fieldWasChangedByUser(JiraClient jiraClient, String ticketKey, String field, String user, Ticket ticket) throws IOException {
        IChangelog changeLog = jiraClient.getChangeLog(ticketKey, ticket);
        List<IHistory> histories = (List<IHistory>) changeLog.getHistories();
        for (IHistory history : histories) {
            List<IHistoryItem> items = (List<IHistoryItem>) history.getHistoryItems();
            for (IHistoryItem historyItem : items) {
                if (historyItem.getField().contains("High Level Estimation") && history.getAuthor().toString().contains(user)) {
                    System.out.println(historyItem.getField() + " changed by " + history.getAuthor() + " " + user);
                    return true;
                }
            }
        }
        return false;
    }
}