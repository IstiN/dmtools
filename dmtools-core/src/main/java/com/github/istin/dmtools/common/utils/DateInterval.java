package com.github.istin.dmtools.common.utils;

import java.util.Calendar;

public class DateInterval {

    private Calendar from;

    private Calendar to;

    public DateInterval(Calendar from, Calendar to) {
        this.from = from;
        this.to = to;
    }

    public Calendar getFrom() {
        return from;
    }

    public Calendar getTo() {
        return to;
    }
}
