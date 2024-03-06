package com.github.istin.dmtools.report;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.timeline.Release;
import com.github.istin.dmtools.common.timeline.ReportIteration;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.metrics.Metric;
import com.github.istin.dmtools.report.freemarker.DevProductivityReport;
import com.github.istin.dmtools.report.freemarker.GenericCell;
import com.github.istin.dmtools.report.freemarker.GenericRow;
import com.github.istin.dmtools.report.freemarker.cells.DevItemsSumCell;
import com.github.istin.dmtools.report.freemarker.cells.DevProductivityCell;
import com.github.istin.dmtools.report.freemarker.cells.DevStoriesSPSumCell;
import com.github.istin.dmtools.report.model.KeyTime;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;
import java.io.File;
import java.util.*;
import java.util.stream.Stream;

public class ProductivityTools {

    public static interface IReleaseGenerator {
        List<Release> generate(int typeOfReleases, int startSprint, int startFixVersion, int extraSprintTimeline, Calendar startDate, long maxTime);

    }
    public static final String REPORT_NAME = "Dev Productivity";

    public static File generate(TrackerClient tracker, IReleaseGenerator releaseGenerator, int startSprint, String startDate, int startFixVersion, String team, String formula, String jql, List<Metric> listOfCustomMetrics, Release.Style style, int sprintShift, int typeOfReleases, int defaultCurrentIteration) throws Exception {
        DevProductivityReport productivityReport = buildReport(tracker, releaseGenerator, startSprint, startDate, startFixVersion, team, formula, jql, listOfCustomMetrics, style, sprintShift, 0, typeOfReleases, defaultCurrentIteration);
        return new ReportUtils().write(team + "_" + REPORT_NAME, "dev_productivity", productivityReport, null);
    }

    @NotNull
    public static DevProductivityReport buildReport(final TrackerClient tracker, IReleaseGenerator releaseGenerator, int startSprint, String startDate, int startFixVersion, String team, String formula, String jql, List<Metric> listOfCustomMetrics, Release.Style style, int sprintShift, int extraSprintTimeline, int typeOfReleases, int defaultCurrentIteration) throws Exception {
        final Map<String, Map<String,List<KeyTime>>> customMetricsProductivityMap = new HashMap<>();
        Set<String> combinedMetrics = new HashSet<>();
        DevProductivityReport productivityReport = new DevProductivityReport();
        productivityReport.setName(team + " " + REPORT_NAME);
        String basePath = "";
        if (tracker != null) {
            basePath = tracker.getBasePath();
            productivityReport.setFilter(basePath + "/issues/?jql=" + jql);
        } else {
            productivityReport.setFilter("");
        }
        List<String> headers = productivityReport.getHeaders();

        for (Metric m : listOfCustomMetrics) {
            if (!headers.contains(m.getName())) {
                headers.add(m.getName());
            }
            customMetricsProductivityMap.put(m.getName(), new HashMap<>());
        }

        final int[] ticketCounter = {0};

        if (jql != null && !jql.isEmpty() && tracker != null) {
            tracker.searchAndPerform(new JiraClient.Performer<ITicket>() {

                @Override
                public boolean perform(ITicket ticket) throws Exception {
                    //custom metrics
                    for (Metric m : listOfCustomMetrics) {
                        if (m.getSourceCollector() == null) {
                            List<KeyTime> productivityItem = m.getRule().check(tracker, ticket);
                            if (productivityItem != null && !productivityItem.isEmpty()) {
                                List<KeyTime> items = customMetricsProductivityMap.get(m.getName()).computeIfAbsent(productivityItem.get(0).getWho(), k -> new ArrayList<>());
                                items.addAll(productivityItem);
                                combinedMetrics.add(productivityItem.get(0).getWho());
                            }
                        }
                    }

                    ticketCounter[0]++;
//                    System.out.println(ticketCounter[0]);
                    return false;
                }

            }, jql, tracker.getDefaultQueryFields());

        }
        for (Metric m : listOfCustomMetrics) {
            if (m.getSourceCollector() != null) {
                m.perform(customMetricsProductivityMap, combinedMetrics);
            }
        }

        long maxTime = 0;
        Calendar date = DateUtils.parseCalendar(startDate);

        for (String metricName : combinedMetrics) {
            //custom metrics
            for (Metric m : listOfCustomMetrics) {
                List<KeyTime> productivityItems = customMetricsProductivityMap.get(m.getName()).get(metricName);
                if (productivityItems != null && !productivityItems.isEmpty()) {
                    productivityItems.sort(Comparator.comparing(KeyTime::getWhen));
                    Iterator<KeyTime> productivityItemsIterator = productivityItems.iterator();
                    KeyTime keyTime;
                    while (productivityItemsIterator.hasNext()) {
                        keyTime = productivityItemsIterator.next();
                        if (keyTime.getWhen().compareTo(date) <0) {
                            productivityReport.addUmatched(keyTime, metricName);
                            productivityItemsIterator.remove();
                        }
                    }
                    if (!productivityItems.isEmpty()) {
                        maxTime = Math.max(maxTime, productivityItems.get(productivityItems.size() - 1).getWhen().getTimeInMillis());
                    }
                } else {
                    customMetricsProductivityMap.get(m.getName()).put(metricName, new ArrayList<>());
                }
            }
        }


        List<Release> releases = releaseGenerator.generate(typeOfReleases, startSprint, startFixVersion, extraSprintTimeline, date, maxTime);


        productivityReport.setReleases(releases);

        List<DevChart> listCharts = new ArrayList<>();
        DevChart allMetrics = new DevChart(team);
        listCharts.add(allMetrics);

        for (Release release : releases) {
            for (ReportIteration reportIteration : release.getIterationsByStyle(style)) {
                for (String metricName : combinedMetrics) {
                    Stream<DevChart> stream = listCharts.stream();
                    if (stream == null) {
                        continue;
                    }
                    Optional<DevChart> devChartOptional = stream.filter(employeeChart1 -> {
                        String devName = employeeChart1.getDevName();
                        if (devName == null) {
                            return false;
                        }
                        return devName.equals(metricName);
                    }).findFirst();
                    DevChart devChart = null;
                    if (!devChartOptional.isPresent()) {
                        devChart = new DevChart(metricName);
                        listCharts.add(devChart);
                    } else {
                        devChart = devChartOptional.get();
                    }

                    DevProductivityCell devProductivityCell = new DevProductivityCell(basePath);
                    devProductivityCell.setCountPrint(false);
                    devProductivityCell.setWeightPrint(true);

                    GenericRow genericRow = findRow(productivityReport.getRows(), metricName);
                    List<GenericCell> cells = null;
                    if (genericRow == null) {
                        genericRow = new GenericRow();
                        cells = genericRow.getCells();
                        cells.add(new GenericCell(MockedNames.getInstance().mock(metricName)));
                        for (Metric m : listOfCustomMetrics) {
                            if (m.isWeight()) {
                                cells.add(new DevStoriesSPSumCell(basePath));
                            } else {
                                cells.add(new DevItemsSumCell(basePath));
                            }
                        }

                        productivityReport.getRows().add(genericRow);
                    }
                    cells = genericRow.getCells();

                    //custom metrics

                    for (int i = 0; i < listOfCustomMetrics.size(); i++) {
                        Metric m = listOfCustomMetrics.get(i);
                        List<KeyTime> keyTimeList = customMetricsProductivityMap.get(m.getName()).get(metricName);
                        for (KeyTime keyTime : keyTimeList) {
                            long timeStart = getCurrentTimeForMeasurements();
                            if (reportIteration.isMatchedToIterationTimeline(keyTime.getWhen())) {
                                timeStart = measureTime("isMatchedToIterationTimeline", timeStart);
                                devProductivityCell.add(keyTime);
                                timeStart = measureTime("isMatchedToIterationTimeline add Key Time", timeStart);
                                ((DevProductivityCell) cells.get(i+1)).add(keyTime);
                                timeStart = measureTime("isMatchedToIterationTimeline add Key Time to cells +1", timeStart);
                                addToDevChart(listOfCustomMetrics, reportIteration, sprintShift, m, keyTime, devChart, defaultCurrentIteration, formula);
                                timeStart = measureTime("addToDevChart", timeStart);
                                addToDevChart(listOfCustomMetrics, reportIteration, sprintShift, m, keyTime, allMetrics, defaultCurrentIteration, formula);
                                timeStart = measureTime("addToAllMetrics", timeStart);
                            } else {
                                addToDevChart(listOfCustomMetrics, reportIteration, sprintShift, m, null, devChart, defaultCurrentIteration, formula);
                                timeStart = measureTime("addToDevChart null", timeStart);
                                addToDevChart(listOfCustomMetrics, reportIteration, sprintShift, m, null, allMetrics, defaultCurrentIteration, formula);
                                timeStart = measureTime("addToAllMetrics null", timeStart);
                            }
                        }
                    }
                    cells.add(devProductivityCell);
                }
            }
        }

        productivityReport.setBySprints(style == Release.Style.BY_SPRINTS);
        productivityReport.setByWeeks(style == Release.Style.BY_WEEKS);

        productivityReport.setTicketsCount(ticketCounter[0]);
        productivityReport.setListDevCharts(listCharts);

        Collections.sort(listCharts, new Comparator<DevChart>() {
            @Override
            public int compare(DevChart o1, DevChart o2) {
                int i = o1.getLevel().compareTo(o2.getLevel());
                if (i == 0) {
                    try {
                        return Double.valueOf(o1.getAvgScore()).compareTo(Double.valueOf(o2.getAvgScore()));
                    } catch (ScriptException e) {
                        throw new RuntimeException(e);
                    }
                }
                return i;
            }
        });


        productivityReport.makeSprintShifts(sprintShift);
        productivityReport.shiftTimelineStarts(16);
        return productivityReport;
    }

    private static long getCurrentTimeForMeasurements() {
        return System.currentTimeMillis();
    }

    private static long measureTime(String action, long timeStart) {
        long newTime = getCurrentTimeForMeasurements();
        long timeSpend = newTime - timeStart;
        if (timeSpend > 10) {
            System.out.println(action + " " + timeSpend);
        }
        return newTime;
    }


    private static void addToDevChart(List<Metric> metrics, ReportIteration reportIteration, int sprintShift, Metric metric, KeyTime keyTime, DevChart devChart, int defaultCurrentIteration, String formula) {
        List<DevChart.ReportIterationData> reportIterationDataList = devChart.reportIterationDataList;
        DevChart.ReportIterationData reportIterationData = null;
        long timeStart = getCurrentTimeForMeasurements();
        String iterationName = reportIteration.getIterationName();
        try {
            iterationName = (Integer.parseInt(iterationName) + sprintShift) + "";
        } catch (Exception ignored) {}
        timeStart = measureTime("conversion to int", timeStart);


        for (int i = reportIterationDataList.size()-1; i >= 0; i--) {
            DevChart.ReportIterationData devChartSprint = reportIterationDataList.get(i);
            if (devChartSprint.getIterationName().equalsIgnoreCase(iterationName)) {
                reportIterationData = devChartSprint;
                break;
            }
        }

        //TODO convert to vs search
//        for (DevChart.ReportIterationData devChartSprint : reportIterationDataList) {
//            if (devChartSprint.getIterationName().equalsIgnoreCase(iterationName)) {
//                reportIterationData = devChartSprint;
//                break;
//            }
//        }

        timeStart = measureTime("find reportIterationData", timeStart);

        if (reportIterationData == null) {
            int id = reportIteration.getId() + sprintShift;
            reportIterationData = new DevChart.ReportIterationData(id, iterationName, defaultCurrentIteration, formula);
            reportIterationData.customMetricsHeaders = metrics;
            if (devChart.reportIterationDataList.size() > 0) {
                int prevIteration = devChart.reportIterationDataList.get(devChart.reportIterationDataList.size() - 1).getReportIterationId();
                if (prevIteration != id - 1) {
                    for (int i = prevIteration + 1; i != id; i++) {
                        devChart.reportIterationDataList.add(new DevChart.ReportIterationData(i, iterationName, defaultCurrentIteration, formula));
                    }
                }
            }
            devChart.reportIterationDataList.add(reportIterationData);
        }

        timeStart = measureTime("add to reportIterationDataList", timeStart);

        List<KeyTime> keyTimes = reportIterationData.customMetrics.computeIfAbsent(metric, k -> new ArrayList<>());
        if (keyTime != null) {
            keyTimes.add(keyTime);
        }
        timeStart = measureTime("add all keyTimes", timeStart);
    }

    private static GenericRow findRow(List<GenericRow> rows, String dev) {
        for (GenericRow genericRow : rows) {
            String text = genericRow.getCells().get(0).getText();
            if (text != null && text.equals(dev)) {
                return genericRow;
            }
        }
        return null;
    }
}
