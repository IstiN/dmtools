package com.github.istin.dmtools.report.productivity;

import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.timeline.Release;
import com.github.istin.dmtools.common.timeline.WeeksReleaseGenerator;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.excel.ExcelMetric;
import com.github.istin.dmtools.excel.model.ExcelMetricConfig;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.metrics.*;
import com.github.istin.dmtools.metrics.rules.TicketMovedToStatusRule;
import com.github.istin.dmtools.report.ProductivityTools;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.report.timeinstatus.TimeInStatus;
import com.github.istin.dmtools.team.Employees;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class DevProductivityReport extends AbstractJob<DevProductivityReportParams> {

    @Override
    public void runJob(DevProductivityReportParams devProductivityReportParams) throws Exception {
        WeeksReleaseGenerator releaseGenerator = new WeeksReleaseGenerator(devProductivityReportParams.getStartDate());
        String formula = devProductivityReportParams.getFormula();
        TrackerClient<? extends ITicket> trackerClient = BasicJiraClient.getInstance();
        String team = devProductivityReportParams.getReportName() + (devProductivityReportParams.isWeight() ? "_sp" : "");
        String inputJQL = devProductivityReportParams.getInputJQL();
        String employeesFile = devProductivityReportParams.getEmployees();
        Employees employees;
        if (employeesFile != null) {
            employees = Employees.getDevelopers(employeesFile);
        } else {
            employees = Employees.getDevelopers();
        }
        ProductivityTools.generate(trackerClient, releaseGenerator, team, formula, inputJQL, generateListOfMetrics(devProductivityReportParams, employees), Release.Style.BY_SPRINTS);
        Set<String> unknownNames = employees.getUnknownNames();
        System.out.println("Unknown Names");
        System.out.println(unknownNames.size());
        System.out.println(unknownNames);
    }


    protected List<Metric> generateListOfMetrics(DevProductivityReportParams devProductivityReportParams, Employees employees) throws IOException {
        List<Metric> listOfCustomMetrics = new ArrayList<>();
        storiesMovedToTesting(devProductivityReportParams, listOfCustomMetrics, false, employees);
        bugsMovedToTesting(devProductivityReportParams, listOfCustomMetrics, false, employees);
        storiesMovedToTesting(devProductivityReportParams, listOfCustomMetrics, true, employees);
        bugsMovedToTesting(devProductivityReportParams, listOfCustomMetrics, true, employees);
        timeSpentOnBugfixing(devProductivityReportParams, listOfCustomMetrics, employees);
        timeSpentOnStoryDevelopment(devProductivityReportParams, listOfCustomMetrics, employees);

        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        if (!basicSourceCodes.isEmpty()) {
            pullRequests(devProductivityReportParams, listOfCustomMetrics, employees);
            pullRequestsChanges(devProductivityReportParams, listOfCustomMetrics, employees);
            pullRequestsComments(devProductivityReportParams, listOfCustomMetrics, employees);
            pullRequestsApprovals(devProductivityReportParams, listOfCustomMetrics, employees);
        }
        ProductivityUtils.vacationDays(listOfCustomMetrics, employees);

        List<ExcelMetricConfig> excelMetricsParams = devProductivityReportParams.getExcelMetricsParams();
        if (excelMetricsParams != null && !excelMetricsParams.isEmpty()) {
            for (ExcelMetricConfig excelMetricConfig : excelMetricsParams) {
                listOfCustomMetrics.add(new ExcelMetric(excelMetricConfig.getMetricName(), employees, excelMetricConfig.getFileName(), excelMetricConfig.getWhoColumn(), excelMetricConfig.getWhenColumn(), excelMetricConfig.getWeightColumn(), excelMetricConfig.getWeightMultiplier()));
            }
        }
        return listOfCustomMetrics;
    }

    protected void pullRequests(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, Employees developers) throws IOException {
        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        if (basicSourceCodes.isEmpty()) {
            return;
        }

        List<Metric> sourceMetrics = new ArrayList<>();
        for (SourceCode sourceCode : basicSourceCodes) {
            sourceMetrics.add(
                    new PullRequestsMetric(true, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, developers)
            );
        }
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsMetric.NAME, sourceMetrics));
    }

    protected void pullRequestsChanges(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, Employees developers) throws IOException {
        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        if (basicSourceCodes.isEmpty()) {
            return;
        }
        List<Metric> sourceMetrics = new ArrayList<>();
        for (SourceCode sourceCode : basicSourceCodes) {
            sourceMetrics.add(
                    new PullRequestsChangesMetric(true, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, developers)
            );
        }
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsChangesMetric.NAME, sourceMetrics));
    }

    protected void pullRequestsComments(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, Employees developers) throws IOException {
        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        if (basicSourceCodes.isEmpty()) {
            return;
        }
        List<Metric> positiveSourceMetrics = new ArrayList<>();
        List<Metric> negativeSourceMetrics = new ArrayList<>();
        for (SourceCode sourceCode : basicSourceCodes) {
            positiveSourceMetrics.add(
                    new PullRequestsCommentsMetric(true, true, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, developers)
            );
            negativeSourceMetrics.add(
                    new PullRequestsCommentsMetric(true, false, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, developers)
            );
        }
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsCommentsMetric.NAME_POSITIVE, positiveSourceMetrics));
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsCommentsMetric.NAME_NEGATIVE, negativeSourceMetrics));


    }

    protected void pullRequestsApprovals(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, Employees developers) throws IOException {
        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        if (basicSourceCodes.isEmpty()) {
            return;
        }
        List<Metric> sourceMetrics = new ArrayList<>();
        for (SourceCode sourceCode : basicSourceCodes) {
            sourceMetrics.add(
                    new PullRequestsApprovalsMetric(true, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, developers)
            );
        }
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsApprovalsMetric.NAME, sourceMetrics));
    }

    protected void storiesMovedToTesting(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, boolean isFirstTimeRight, final Employees developers) {
        listOfCustomMetrics.add(new Metric("Stories Moved To Testing"+ (isFirstTimeRight ? " FTR" : ""), devProductivityReportParams.isWeight(), new TicketMovedToStatusRule(devProductivityReportParams.getStatusesReadyForTesting(), null, false) {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws Exception {
                if (ProductivityUtils.isIgnoreTask(devProductivityReportParams.getIgnoreTicketPrefixes(), ticket)) return null;

                if (ProductivityUtils.isStory(devProductivityReportParams, ticket)) {
                    if (isFirstTimeRight) {
                        if (!ChangelogAssessment.isFirstTimeRight(jiraClient, ticket.getTicketKey(), ticket, devProductivityReportParams.getStatusesInDevelopment(), devProductivityReportParams.getStatusesInTesting())) {
                            return null;
                        }
                    }
                    List<KeyTime> keyTimes = super.check(jiraClient, ticket);
                    if (keyTimes != null && !keyTimes.isEmpty()) {
                        keyTimes = keyTimes.subList(0, 1);
                        KeyTime keyTime = keyTimes.get(0);
                        keyTime.setWho(findResponsibleDev(jiraClient, ticket, developers, devProductivityReportParams.getStatusesInDevelopment()));
                        keyTime.setWeight(calcWeight(devProductivityReportParams, jiraClient, ticket));
                    }
                    return keyTimes;
                } else {
                    return null;
                }
            }

        }));
    }

    protected void bugsMovedToTesting(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, boolean isFirstTimeRight, final Employees developers) {
        listOfCustomMetrics.add(new Metric("Bugs Moved To Testing" + (isFirstTimeRight ? " FTR" : ""), devProductivityReportParams.isWeight(), new TicketMovedToStatusRule(devProductivityReportParams.getStatusesReadyForTesting(), null, false) {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws Exception {
                if (ProductivityUtils.isIgnoreTask(devProductivityReportParams.getIgnoreTicketPrefixes(), ticket)) return null;

                if (ProductivityUtils.isBug(devProductivityReportParams, ticket)) {
                    if (isFirstTimeRight) {
                        if (!ChangelogAssessment.isFirstTimeRight(jiraClient, ticket.getTicketKey(), ticket, devProductivityReportParams.getStatusesInDevelopment(), devProductivityReportParams.getStatusesInTesting())) {
                            return null;
                        }
                    }
                    List<KeyTime> keyTimes = super.check(jiraClient, ticket);
                    if (keyTimes != null && !keyTimes.isEmpty()) {
                        keyTimes = keyTimes.subList(0, 1);
                        KeyTime keyTime = keyTimes.get(0);
                        keyTime.setWho(findResponsibleDev(jiraClient, ticket, developers, devProductivityReportParams.getStatusesInDevelopment()));
                        keyTime.setWeight(calcWeight(devProductivityReportParams, jiraClient, ticket));
                    }
                    return keyTimes;
                } else {
                    return null;
                }
            }

        }));
    }

    public String findResponsibleDev(TrackerClient trackerClient, ITicket ticket, Employees developers, String ... inDevStatuses) throws IOException {
        return ChangelogAssessment.findWhoIsResponsible(trackerClient, developers, ticket, inDevStatuses);
    }

    protected double calcWeight(DevProductivityReportParams devProductivityReportParams, TrackerClient jiraClient, ITicket ticket) throws Exception {
        if (devProductivityReportParams.getCalcWeightType() == DevProductivityReportParams.CalcWeightType.TIME_SPENT) {
            List<KeyTime> keyTimes = null;
            if (IssueType.isSubTask(ticket.getFields().getIssueType().getName())) {
                keyTimes = timeSpentOn(devProductivityReportParams, jiraClient, ticket.getFields().getParent(), devProductivityReportParams.getInitialStatus(), devProductivityReportParams.getStatusesInTesting());
            } else {
                keyTimes = timeSpentOn(devProductivityReportParams, jiraClient, ticket, devProductivityReportParams.getInitialStatus(), devProductivityReportParams.getStatusesInTesting());
            }
            if (keyTimes != null && !keyTimes.isEmpty()) {
                double weight = keyTimes.get(0).getWeight();
                if (weight < 1) {
                    return 1;
                }
                return weight;
            } else {
                return 1;
            }
        } else if (devProductivityReportParams.getCalcWeightType() == DevProductivityReportParams.CalcWeightType.STORY_POINTS) {
            if (ProductivityUtils.isStory(devProductivityReportParams, ticket)) {
                int storyPoints = ticket.getFields().getStoryPoints();
                if (storyPoints == -1) {
                    return 1;
                }
                return storyPoints;
            } else {
                return 1;
            }
        }
        return 1;
    }


    protected void timeSpentOnStoryDevelopment(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, final Employees developers) throws IOException {
        listOfCustomMetrics.add(new Metric("Story Development Time In Days", devProductivityReportParams.isWeight(), new TrackerRule<ITicket>() {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws IOException, Exception {
                if (ProductivityUtils.isIgnoreTask(devProductivityReportParams.getIgnoreTicketPrefixes(), ticket)) return null;

                if (ProductivityUtils.isStory(devProductivityReportParams, ticket)) {
                    List<KeyTime> keyTimes = timeSpentOn(devProductivityReportParams, jiraClient, ticket, devProductivityReportParams.getInitialStatus(), devProductivityReportParams.getStatusesInDevelopment());
                    if (keyTimes != null) {
                        for (KeyTime keyTime : keyTimes) {
                            keyTime.setWho(findResponsibleDev(jiraClient, ticket, developers, devProductivityReportParams.getStatusesInDevelopment()));
                        }
                        return keyTimes;
                    }
                }
                return null;
            }
        }));
    }

    protected void timeSpentOnBugfixing(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, final Employees developers) throws IOException {
        listOfCustomMetrics.add(new Metric("Bugfixing Time In Days", devProductivityReportParams.isWeight(), new TrackerRule<ITicket>() {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws IOException, Exception {
                if (ProductivityUtils.isIgnoreTask(devProductivityReportParams.getIgnoreTicketPrefixes(), ticket)) return null;

                if (ProductivityUtils.isBug(devProductivityReportParams, ticket)) {
                    List<KeyTime> keyTimes = timeSpentOn(devProductivityReportParams, jiraClient, ticket, devProductivityReportParams.getInitialStatus(), devProductivityReportParams.getStatusesInDevelopment());
                    if (keyTimes != null) {
                        for (KeyTime keyTime : keyTimes) {
                            keyTime.setWho(findResponsibleDev(jiraClient, ticket, developers, devProductivityReportParams.getStatusesInDevelopment()));
                        }
                        return keyTimes;
                    }
                }
                return null;
            }
        }));
    }

    protected @Nullable List<KeyTime> timeSpentOn(DevProductivityReportParams params, TrackerClient jiraClient, ITicket ticket, String firstStatus, String... statuses) throws Exception {
        final TimeInStatus timeInStatus = new TimeInStatus(jiraClient);
        if (ticket.getCreated() == null) {
            ticket = jiraClient.performTicket(ticket.getTicketKey(), jiraClient.getDefaultQueryFields());
        }
        List<TimeInStatus.Item> itemList = timeInStatus.check(ticket, Arrays.asList(statuses), firstStatus);
        if (!itemList.isEmpty()) {
            List<KeyTime> keyTimes = new ArrayList<>();
            int days = 0;
            Calendar startDate = null;
            for (TimeInStatus.Item item : itemList) {
                for (String status : statuses) {
                    if (item.getStatusName().equalsIgnoreCase(status)) {
                        int currentTimeInStatusDays = item.getDays();
                        int weekendDaysBetweenTwoDates = DateUtils.getWeekendDaysBetweenTwoDates(item.getStartDate().getTimeInMillis(), item.getEndDate().getTimeInMillis());
                        currentTimeInStatusDays = currentTimeInStatusDays - weekendDaysBetweenTwoDates;
                        days += currentTimeInStatusDays;
                        startDate = item.getStartDate();
                    }
                }
            }
            if (days < 0) {
                days = 1;
            } else if (days > 10) {
                System.out.println("wrong reporting " + ticket.getTicketLink() + " " + days);
            }
            if (startDate != null) {
                List<KeyTime> datesWhenTicketWasInStatus = ChangelogAssessment.findDatesWhenTicketWasInStatus(null, false, jiraClient, ticket.getKey(), ticket, params.getStatusesInDevelopment());
                if (!datesWhenTicketWasInStatus.isEmpty()) {
                    KeyTime e = new KeyTime(ticket.getKey(), startDate, datesWhenTicketWasInStatus.get(0).getWho());
                    e.setWeight(days);
                    keyTimes.add(e);
                    return keyTimes;
                }
            }
        }
        return null;
    }

}
