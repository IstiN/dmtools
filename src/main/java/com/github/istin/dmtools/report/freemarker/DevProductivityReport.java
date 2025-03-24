package com.github.istin.dmtools.report.freemarker;

import com.github.istin.dmtools.report.DevChart;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DevProductivityReport extends GenericReport {

    @Setter
    private List<String> headers = new ArrayList<>();

    private int ticketCounter;

    @Setter
    private String htmlBeforeTimeline = "";

    @Setter
    private List<DevChart> listDevCharts = new ArrayList<>();


    public void setTicketsCount(int ticketCounter) {
        this.ticketCounter = ticketCounter;
    }

    public void shiftTimelineStarts(int shiftTimelineStarts) {

    }
}
