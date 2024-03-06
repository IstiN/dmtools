package com.github.istin.dmtools.report.freemarker;

import com.github.istin.dmtools.report.DevChart;

import java.util.ArrayList;
import java.util.List;

public class DevProductivityReport extends GenericReport {

    private List<String> headers = new ArrayList<>();
    private int ticketCounter;

    public List<DevChart> getListDevCharts() {
        return listDevCharts;
    }

    public void setListDevCharts(List<DevChart> listDevCharts) {
        this.listDevCharts = listDevCharts;
    }

    private List<DevChart> listDevCharts = new ArrayList<>();

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public void setTicketsCount(int ticketCounter) {
        this.ticketCounter = ticketCounter;
    }

    public int getTicketCounter() {
        return ticketCounter;
    }

    public void shiftTimelineStarts(int shiftTimelineStarts) {

    }
}
