package com.github.istin.dmtools.common.utils;

import junit.framework.TestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public void testSmartParseDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        // Test ISO_DATE_FORMAT2
        String isoDateFormat2 = "2024-11-07T13:15:32.000Z";
        Date expectedDateIso2 = sdf.parse("2024-11-07T13:15:32.000Z");
        assertEquals(expectedDateIso2, DateUtils.smartParseDate(isoDateFormat2));

        // Test ISO_DATE_FORMAT
        String isoDateFormat = "2024-11-07T13:15:32Z";
        Date expectedDateIso = sdf.parse("2024-11-07T13:15:32.000Z"); // Milliseconds are zero for comparison
        assertEquals(expectedDateIso, DateUtils.smartParseDate(isoDateFormat));

        // Test BITBUCKET_CLOUD_DATE_FORMAT
        String bitbucketCloudDate = "2024-11-07T13:15:32.123";
        Date expectedBBCloudDate = sdf.parse("2024-11-07T13:15:32.123Z");
        assertEquals(expectedBBCloudDate, DateUtils.smartParseDate(bitbucketCloudDate + "Z")); // Adding Z for UTC comparison

        // Test BITBUCKET_DATE_FORMAT
        String bitbucketDate = "2024-11-07T13:15:32-05:00";
        Date expectedBitbucketDate = sdf.parse("2024-11-07T18:15:32.000Z"); // Adjusted to UTC
        assertEquals(expectedBitbucketDate, DateUtils.smartParseDate(bitbucketDate));

        // Test RALLY_DATE_FORMAT
        String rallyDate = "2024-11-07T13:15:32.123Z";
        Date expectedRallyDate = sdf.parse(rallyDate);
        assertEquals(expectedRallyDate, DateUtils.smartParseDate(rallyDate));

        // Test jiraServerDate
        String jiraServerDate = "2024-11-07T08:15:32.000-0500";
        Date date = DateUtils.smartParseDate(jiraServerDate);
        System.out.println(date);
        assertNotNull(date);


    }
}