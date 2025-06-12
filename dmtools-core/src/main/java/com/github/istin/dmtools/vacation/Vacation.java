package com.github.istin.dmtools.vacation;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Vacation extends JSONModel {

    public static final String EMPLOYEE = "EMPLOYEE";
    public static final String START_DATE = "START DATE";
    public static final String END_DATE = "END DATE";
    public static final String DURATION = "DURATION";

    public static final SimpleDateFormat DEFAULT_FORMATTER = new SimpleDateFormat("MM/dd/yy");


    public Vacation() {
    }

    public Vacation(String json) throws JSONException {
        super(json);
    }

    public Vacation(JSONObject json) {
        super(json);
    }

    public Double getDuration() {
        return Double.parseDouble(getString(DURATION).replace(",","."));
    }

    public String getStartDate() {
        return getString(START_DATE);
    }

    public Date getStartDateAsDate() {
        String startDate = getStartDate();
        Date date = null;
        try {
            date = new SimpleDateFormat("MM/dd/yy").parse(startDate);
        } catch (ParseException e) {
            try {
                date = new SimpleDateFormat("dd.MM.yy").parse(startDate);
            } catch (ParseException ex) {
                return null;
            }
        }
        return date;
    }

    public Calendar getStartDateAsCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getStartDateAsDate());
        return calendar;
    }

    public Calendar getEndDateAsCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getEndDateAsDate());
        return calendar;
    }

    public String getEndDate() {
        return getString(END_DATE);
    }

    public Date getEndDateAsDate() {
        String endDate = getEndDate();
        Date date = null;
        try {
            date = DEFAULT_FORMATTER.parse(endDate);
        } catch (ParseException e) {
            try {
                date = new SimpleDateFormat("dd/MM/yy").parse(endDate);
            } catch (ParseException ex) {
                return null;
            }
        }
        return date;
    }

    public String getName() {
        return getString(EMPLOYEE);
    }


}
