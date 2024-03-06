package com.github.istin.dmtools.common.timeline;

import java.util.Calendar;

public interface ReportIteration {

    boolean isMatchedToIterationTimeline(Calendar date);

    String getIterationName();

    int getId();

}
