package com.github.istin.dmtools.common.utils;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

public class DateUtilsTest extends TestCase {

    public void testParseRallyDate() {
        Calendar date = DateUtils.parseRallyCalendar("2023-01-03T23:00:00.000Z");
        System.out.println(DateUtils.formatToRallyDate(date));

    }
}