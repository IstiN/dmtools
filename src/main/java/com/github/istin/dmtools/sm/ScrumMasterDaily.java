package com.github.istin.dmtools.sm;

import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.job.AbstractJob;

import java.util.Calendar;
import java.util.List;

public class ScrumMasterDaily extends AbstractJob<ScrumMasterDailyParams> {

    @Override
    public void runJob(ScrumMasterDailyParams scrumMasterDailyParams) throws Exception {
        BasicConfluence confluence = BasicConfluence.getInstance();
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();
        Calendar yesterday = Calendar.getInstance();

        // Subtract one day to get yesterday
        yesterday.add(Calendar.DATE, -1);

        // Set the time to 00:00:00
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);

        trackerClient.searchAndPerform(ticket -> {
            IChangelog changeLog = trackerClient.getChangeLog(ticket.getTicketKey(), ticket);
            List<IHistory> histories = (List<IHistory>) changeLog.getHistories();
            boolean isFirstChange = true;
            for (IHistory history : histories) {
                IUser author = history.getAuthor();
                String authorName = author == null ? "" : author.getFullName();
                Calendar created = history.getCreated();
                if (history.getCreated().compareTo(yesterday) >= 0 && !authorName.contains("Automation")) {
                    if (isFirstChange) {
                        isFirstChange = false;
                        System.out.println("Changes in ticket " + ticket.getKey()+ ": " + ticket.getTicketTitle());
                    }
                    System.out.println("By Author: " + authorName);
                    System.out.println("When: " + DateUtils.formatToRallyDate(created));

                    for (IHistoryItem item : history.getHistoryItems()) {
                        String field = item.getField();
                        String fromString = item.getFromAsString();
                        String toString = item.getToAsString();

                        System.out.println("Field: " + field);
                        System.out.println("From: " + fromString);
                        System.out.println("To: " + toString);
                        System.out.println();
                    }
                }
            }
            if (!isFirstChange) {
                System.out.println("-----");
            }
            return false;
        }, scrumMasterDailyParams.getJql() + " and updated >= -2", trackerClient.getDefaultQueryFields());
    }

}
