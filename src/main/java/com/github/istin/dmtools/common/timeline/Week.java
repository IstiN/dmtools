package com.github.istin.dmtools.common.timeline;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Week implements ReportIteration {

    private Date startDate;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

    private Date endDate;

    public static List<Week> createBasedOnSprint(Sprint sprint) {
        Date startDate = sprint.getStartDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startDate.getTime());
        calendar.add(Calendar.DATE, -2);
        Week week1 = new Week();
        week1.startDate = new Date(calendar.getTimeInMillis());
        calendar.add(Calendar.DATE, 7);
        calendar.add(Calendar.SECOND, -1);
        week1.endDate = new Date(calendar.getTimeInMillis());

        Week week2 = new Week();
        calendar.add(Calendar.SECOND, 1);
        week2.startDate = new Date(calendar.getTimeInMillis());
        calendar.add(Calendar.DATE, 7);
        calendar.add(Calendar.SECOND, -1);
        week2.endDate = new Date(calendar.getTimeInMillis());
        return Arrays.asList(week1, week2);
    }

    public String getStartDateAsString() {
        return simpleDateFormat.format(startDate);
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getEndDateAsString() {
        return simpleDateFormat.format(endDate);
    }

    public boolean isMatchedToWeekTimelines(Calendar date) {
        return startDate.getTime() <= date.getTimeInMillis() && endDate.getTime() >= date.getTimeInMillis();
    }

    public boolean isMatchedToWeekTimelinesOrLess(Calendar date) {
        return endDate.getTime() >= date.getTimeInMillis();
    }

    @Override
    public boolean isMatchedToIterationTimeline(Calendar date) {
        return isMatchedToWeekTimelines(date);
    }

    @Override
    public String getIterationName() {
        return getStartDateAsString() + "-"+getEndDateAsString();
    }

    @Override
    public int getId() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }
}
