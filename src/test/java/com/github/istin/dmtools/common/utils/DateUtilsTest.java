package com.github.istin.dmtools.common.utils;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

public class DateUtilsTest extends TestCase {

    public void testParseRallyDate() {
        Calendar date = DateUtils.parseRallyCalendar("2023-01-03T23:00:00.000Z");
        System.out.println(DateUtils.formatToRallyDate(date));

    }

    //2024-03-20T14:22:41.123832+00:00
    /*
    String dateString = "2024-03-20T14:22:41.123832+00:00";
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");
OffsetDateTime dateTime = OffsetDateTime.parse(dateString, formatter);
     */
    public void testParseBitbucketCloudDate() {
        Date date = DateUtils.parseCloudBitbucketDate("2024-03-20T14:22:41.123832+00:00");
        System.out.println(date);

    }
}