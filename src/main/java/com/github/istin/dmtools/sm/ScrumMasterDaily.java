package com.github.istin.dmtools.sm;

import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.job.AbstractJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.List;

public class ScrumMasterDaily extends AbstractJob<ScrumMasterDailyParams> {
    private static final Logger logger = LogManager.getLogger(ScrumMasterDaily.class);
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
                        logger.info("Changes in ticket {}: {}", ticket.getKey(), ticket.getTicketTitle());
                    }
                    logger.info("By Author: {}", authorName);
                    logger.info("When: {}", DateUtils.formatToRallyDate(created));

                    for (IHistoryItem item : history.getHistoryItems()) {
                        String field = item.getField();
                        String fromString = item.getFromAsString();
                        String toString = item.getToAsString();

                        logger.info("Field: {}", field);
                        logger.info("From: {}", fromString);
                        logger.info("To: {}", toString);
                        logger.info("\n");
                    }
                }
            }
            if (!isFirstChange) {
                logger.info("-----");
            }
            return false;
        }, scrumMasterDailyParams.getJql() + " and updated >= -2", trackerClient.getDefaultQueryFields());
    }

}
