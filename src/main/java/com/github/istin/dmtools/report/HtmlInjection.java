package com.github.istin.dmtools.report;

import com.github.istin.dmtools.report.freemarker.DevProductivityReport;

public interface HtmlInjection {

    String getHtmBeforeTimeline(DevProductivityReport productivityReport);
}
