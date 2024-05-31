package com.github.istin.dmtools.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static final String BITBUCKET_CLOUD_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String BITBUCKET_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String JIRA_DATE_FORMAT = "yyyy-MM-dd";
    public static final String JIRA_DATE_FORMAT_V2 = "dd/MMM/yyyy";
    public static final String RALLY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";


    public static Calendar parseCalendar(String date) {
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(parseDate(date));
        return startDate;
    }

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Integer getDaysDuration(Calendar endDate, Calendar startDate) {
        if (endDate != null && startDate != null) {
            long date1 = startDate.getTimeInMillis();
            long date2 = endDate.getTimeInMillis();
            return getDaysDuration(date1, date2);
        }
        return null;
    }

    public static Integer getDaysDuration(long date1, long date2) {
        int days = (int) (Math.abs(date1 - date2) / 1000 / 60 / 60 / 24);
        if (days < 0) {
            return 1;
        }
        return days;
    }

    public static Integer getHoursDuration(long date1, long date2) {
        int hours = (int) ((date1 - date2) / 1000 / 60 / 60);
        if (hours < 0) {
            return 1;
        }
        return hours;
    }

    public static Date parseJiraDate(String date) {
        try {
            return new SimpleDateFormat(JIRA_DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat(JIRA_DATE_FORMAT_V2).parse(date);
            } catch (ParseException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    public static Date parseBitbucketDate(String date) {
        try {
            return new SimpleDateFormat(BITBUCKET_DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Date parseCloudBitbucketDate(String date) {
        try {
            date = date.substring(0, date.lastIndexOf('.')+4);
            return new SimpleDateFormat(BITBUCKET_CLOUD_DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Date parseRallyDate(String date) {
        try {
            return new SimpleDateFormat(RALLY_DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Calendar parseRallyCalendar(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(parseRallyDate(date));
        return startDate;
    }

    public static Calendar calendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Calendar parseJiraCalendar(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(parseJiraDate(date));
        return startDate;
    }

    public static String formatToJiraDate(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new SimpleDateFormat(JIRA_DATE_FORMAT).format(calendar.getTime());
    }

    public static String formatToRallyDate(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new SimpleDateFormat(RALLY_DATE_FORMAT).format(calendar.getTime());
    }

    public static String formatToRallyDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return formatToRallyDate(calendar);
    }

    public static String formatToJiraDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return formatToJiraDate(calendar);
    }

    public static int compareCalendars(Calendar date1, Calendar date2) {
        if (date1 == null) {
            if (date2 == null) {
                return 0;
            }
            return 1;
        }
        if (date2 == null) {
            return -1;
        }
        return date1.compareTo(date2);
    }

    public static Date convertDateToWorkingHoursDate(Date inputDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inputDate);

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay < 8) {
            hourOfDay = 8 + hourOfDay;
        }
        if (hourOfDay > 18) {
            hourOfDay = 8 + (24 - hourOfDay);
        }

        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);

        return calendar.getTime();
    }

    public static int getWeekendDaysBetweenTwoDates(long startDate, long endDate) {
        Calendar startCal;
        Calendar endCal;
        startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startDate);
        endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endDate);
        int weekendDays = 0;

        //Return 0 if start and end are the same
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return 0;
        }

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTimeInMillis(endDate);
            endCal.setTimeInMillis(startDate);
        }

        do {
            startCal.add(Calendar.DAY_OF_MONTH, 1);
            if (startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                ++weekendDays;
            }
        } while (startCal.getTimeInMillis() < endCal.getTimeInMillis());

        return weekendDays;
    }

}
