package com.github.istin.dmtools.atlassian.jira.utils;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.model.*;
import com.github.istin.dmtools.report.model.KeyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChangelogAssessment {

    public static List<KeyTime> findCreatedDate(JiraClient lgiJira, String key) throws IOException {
        Ticket ticket = lgiJira.performTicket(key, new String[]{"created", "creator"});
        List<KeyTime> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ticket.getFields().getCreated());
        KeyTime keyTime = new KeyTime(ticket.getKey(), calendar, ticket.getFields().getCreator().getDisplayName());
        result.add(keyTime);
        return result;
    }

    public static List<KeyTime> findDatesWhenTicketWasInStatus(JiraClient lgiJira, String key, Ticket ticket, String... statuses) throws IOException {
        return findDatesWhenTicketWasInStatus(null, null, false, lgiJira, key, ticket, statuses);
    }

    public static List<KeyTime> findDatesWhenTicketWasInStatus(String customName, boolean isSetWeight, JiraClient lgiJira, String key, Ticket ticket, String... statuses) throws IOException {
        return findDatesWhenTicketWasInStatus(null, customName, isSetWeight, lgiJira, key, ticket, statuses);
    }


    public static List<KeyTime> findDatesWhenTicketWasInStatus(String ignoreIfStatus, String customName, boolean isSetWeight, JiraClient jira, String key, Ticket ticket, String... statuses) throws IOException {
        Changelog changeLog = jira.getChangeLog(key, ticket);
        List<History> histories = changeLog.getHistories();
        List<KeyTime> result = new ArrayList<>();
        boolean ifStatusChanged = false;

        for (History history : histories) {
            List<HistoryItem> items = history.getItems();
            for (HistoryItem historyItem : items) {
                if (historyItem.getField().equalsIgnoreCase("status")) {
                    String toString = historyItem.getToString();
                    if (toString != null) {
                        ifStatusChanged = true;
                        if (ignoreIfStatus != null && ignoreIfStatus.equalsIgnoreCase(toString)) {
                            result.clear();
                        }
                        for (String status : statuses) {
                            if (toString.equalsIgnoreCase(status)) {
                                Calendar created = history.getCreated();
                                String displayName = history.getAuthor().getDisplayName();
                                result.add(new KeyTime(key, created, customName != null ? customName : displayName));
                            }
                        }
                    }
                }
            }
        }
        if (!ifStatusChanged) {
            Ticket latestStatusTicket = jira.performTicket(key, new String[]{Fields.STATUS, Fields.CREATED, Fields.CREATOR});
            String initialStatus = latestStatusTicket.getStatus();
            Calendar createdCalendar = Calendar.getInstance();
            createdCalendar.setTime(latestStatusTicket.getFields().getCreated());
            for (String status : statuses) {
                if (initialStatus.equalsIgnoreCase(status)) {
                    result.add(new KeyTime(key, createdCalendar, latestStatusTicket.getFields().getCreator().getDisplayName()));
                }
            }
        }

        return result;
    }

    public static boolean fieldWasChangedByUser(JiraClient jiraClient, String ticketKey, String field, String user, Ticket ticket) throws IOException {
        Changelog changeLog = jiraClient.getChangeLog(ticketKey, ticket);
        List<History> histories = changeLog.getHistories();
        for (History history : histories) {
            List<HistoryItem> items = history.getItems();
            for (HistoryItem historyItem : items) {
                if (historyItem.getField().contains("High Level Estimation") && history.getAuthor().toString().contains(user)) {
                    System.out.println(historyItem.getField() + " changed by " + history.getAuthor() + " " + user);
                    return true;
                }
            }
        }
        return false;
    }
}