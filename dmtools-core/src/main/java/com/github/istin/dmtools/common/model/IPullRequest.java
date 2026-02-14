package com.github.istin.dmtools.common.model;

import com.github.istin.dmtools.common.utils.DateUtils;

import java.util.Calendar;

public interface IPullRequest {

    String getTitle();

    String getDescription();

    Integer getId();

    IUser getAuthor();

    String getTargetBranchName();

    String getSourceBranchName();

    Long getCreatedDate();

    Long getClosedDate();

    Long getUpdatedDate();

    default IUser getMergedBy() { return null; }

    class Utils {

        public static String upgradeTitleIfWip(IPullRequest pullRequest, String newTitle) {
            return (IPullRequest.Utils.isWIP(pullRequest) ? IPullRequest.Utils.upgradeTitleToWIP(newTitle) : newTitle).trim();
        }

        public static boolean isWIP(IPullRequest pullRequest) {
            return pullRequest.getTitle().toLowerCase().contains("[wip]");
        }

        public static String upgradeTitleToWIP(String newTitle) {
            return newTitle.startsWith("[WIP]") ? newTitle : "[WIP] "+ newTitle.trim();
        }

        public static Calendar getCreatedDateAsCalendar(IPullRequest pullRequest) {
            Long createdDate = pullRequest.getCreatedDate();
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(createdDate);
            return instance;
        }

        public static Calendar getClosedDateAsCalendar(IPullRequest pullRequest) {
            Long closedDate = pullRequest.getClosedDate();
            if (closedDate == null) {
                System.err.println(pullRequest);
            }
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(closedDate);
            return instance;
        }

        public static int getWorkingHoursOpened(IPullRequest pullRequest) {
            Integer hoursDuration = DateUtils.getHoursDuration(pullRequest.getClosedDate(), pullRequest.getCreatedDate());
            int weekendDaysBetweenTwoDates = DateUtils.getWeekendDaysBetweenTwoDates(pullRequest.getCreatedDate(), pullRequest.getClosedDate());
            hoursDuration = hoursDuration - weekendDaysBetweenTwoDates * 24;
            if (hoursDuration < 0) {
                hoursDuration = 0;
            }
            if (hoursDuration > 24) {
                hoursDuration = hoursDuration - (hoursDuration / 24) * 8;
            }
            return hoursDuration;
        }

        public static Calendar getUpdatedDateAsCalendar(IPullRequest pullRequest) {
            Long updatedDate = pullRequest.getUpdatedDate();
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(updatedDate);
            return instance;
        }
    }

    class PullRequestState {
        public static String STATE_MERGED = "merged";
        public static String STATE_OPEN = "open";
        public static String STATE_DECLINED = "declined";
    }
}
