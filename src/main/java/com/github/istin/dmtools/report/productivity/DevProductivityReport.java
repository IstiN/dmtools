package com.github.istin.dmtools.report.productivity;

import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.IHistoryItem;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.timeline.Release;
import com.github.istin.dmtools.common.timeline.WeeksReleaseGenerator;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.metrics.*;
import com.github.istin.dmtools.report.ProductivityTools;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.report.timeinstatus.TimeInStatus;
import com.github.istin.dmtools.team.Employees;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DevProductivityReport extends AbstractJob<DevProductivityReportParams> {

    @Override
    public void runJob(DevProductivityReportParams devProductivityReportParams) throws Exception {
        WeeksReleaseGenerator releaseGenerator = new WeeksReleaseGenerator(devProductivityReportParams.getStartDate());
        String formula = devProductivityReportParams.getFormula();
        ProductivityTools.generate(BasicJiraClient.getInstance(), releaseGenerator, devProductivityReportParams.getReportName() + (devProductivityReportParams.isWeight() ? "_sp" : ""), formula, devProductivityReportParams.getInputJQL(), generateListOfMetrics(devProductivityReportParams), Release.Style.BY_SPRINTS);
    }


    protected List<Metric> generateListOfMetrics(DevProductivityReportParams devProductivityReportParams) throws IOException {
        List<Metric> listOfCustomMetrics = new ArrayList<>();
        storiesMovedToTesting(devProductivityReportParams, listOfCustomMetrics, false);
        bugsMovedToTesting(devProductivityReportParams, listOfCustomMetrics, false);
        storiesMovedToTesting(devProductivityReportParams, listOfCustomMetrics, true);
        bugsMovedToTesting(devProductivityReportParams, listOfCustomMetrics, true);
        timeSpentOnBugfixing(devProductivityReportParams, listOfCustomMetrics);
        timeSpentOnStoryDevelopment(devProductivityReportParams, listOfCustomMetrics);
        pullRequests(devProductivityReportParams, listOfCustomMetrics);
        pullRequestsChanges(devProductivityReportParams, listOfCustomMetrics);
        pullRequestsComments(devProductivityReportParams, listOfCustomMetrics);
        pullRequestsApprovals(devProductivityReportParams, listOfCustomMetrics);
        vacationDays(listOfCustomMetrics);
        return listOfCustomMetrics;
    }

    protected void pullRequests(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics) throws IOException {
        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        List<Metric> sourceMetrics = new ArrayList<>();
        for (SourceCode sourceCode : basicSourceCodes) {
            sourceMetrics.add(
                    new PullRequestsMetric(true, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, Employees.getDevelopers())
            );
        }
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsMetric.NAME, sourceMetrics));
    }

    protected void pullRequestsChanges(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics) throws IOException {
        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        List<Metric> sourceMetrics = new ArrayList<>();
        for (SourceCode sourceCode : basicSourceCodes) {
            sourceMetrics.add(
                    new PullRequestsChangesMetric(true, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, Employees.getDevelopers())
            );
        }
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsChangesMetric.NAME, sourceMetrics));
    }

    private static List<Metric> vacationDays(List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new VacationMetric(true, true, Employees.getDevelopers()));
        return listOfCustomMetrics;
    }

    protected void pullRequestsComments(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics) throws IOException {
        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        List<Metric> positiveSourceMetrics = new ArrayList<>();
        List<Metric> negativeSourceMetrics = new ArrayList<>();
        for (SourceCode sourceCode : basicSourceCodes) {
            positiveSourceMetrics.add(
                    new PullRequestsCommentsMetric(true, true, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, Employees.getDevelopers())
            );
            negativeSourceMetrics.add(
                    new PullRequestsCommentsMetric(true, false, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, Employees.getDevelopers())
            );
        }
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsCommentsMetric.NAME_POSITIVE, positiveSourceMetrics));
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsCommentsMetric.NAME_NEGATIVE, negativeSourceMetrics));


    }

    protected void pullRequestsApprovals(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics) throws IOException {
        List<SourceCode> basicSourceCodes = SourceCode.Impl.getConfiguredSourceCodes(devProductivityReportParams.getSources());
        List<Metric> sourceMetrics = new ArrayList<>();
        for (SourceCode sourceCode : basicSourceCodes) {
            sourceMetrics.add(
                    new PullRequestsApprovalsMetric(true, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode, Employees.getDevelopers())
            );
        }
        listOfCustomMetrics.add(new CombinedCustomRunnableMetrics(PullRequestsApprovalsMetric.NAME, sourceMetrics));
    }

    protected void storiesMovedToTesting(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, boolean isFirstTimeRight) {
        listOfCustomMetrics.add(new Metric("Stories Moved To Testing"+ (isFirstTimeRight ? " FTR" : ""), devProductivityReportParams.isWeight(), new TicketMovedToStatusRule(devProductivityReportParams.getStatusesReadyForTesting(), null, false) {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws Exception {
                if (isIgnoreTask(devProductivityReportParams, ticket)) return null;

                if (isStory(ticket)) {
                    if (isFirstTimeRight) {
                        if (!ChangelogAssessment.isFirstTimeRight(jiraClient, ticket.getTicketKey(), ticket, devProductivityReportParams.getStatusesInDevelopment(), devProductivityReportParams.getStatusesInTesting())) {
                            return null;
                        }
                    }
                    List<KeyTime> keyTimes = super.check(jiraClient, ticket);
                    if (keyTimes != null && !keyTimes.isEmpty()) {
                        keyTimes = keyTimes.subList(0, 1);
                        KeyTime keyTime = keyTimes.get(0);
                        keyTime.setWho(findResponsibleDev(jiraClient, ticket, devProductivityReportParams.getStatusesInDevelopment()));
                        keyTime.setWeight(calcWeight(devProductivityReportParams, jiraClient, ticket));
                    }
                    return keyTimes;
                } else {
                    return null;
                }
            }

        }));
    }

    protected void bugsMovedToTesting(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics, boolean isFirstTimeRight) {
        listOfCustomMetrics.add(new Metric("Bugs Moved To Testing" + (isFirstTimeRight ? " FTR" : ""), devProductivityReportParams.isWeight(), new TicketMovedToStatusRule(devProductivityReportParams.getStatusesReadyForTesting(), null, false) {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws Exception {
                if (isIgnoreTask(devProductivityReportParams, ticket)) return null;

                if (isBug(ticket)) {
                    if (isFirstTimeRight) {
                        if (!ChangelogAssessment.isFirstTimeRight(jiraClient, ticket.getTicketKey(), ticket, devProductivityReportParams.getStatusesInDevelopment(), devProductivityReportParams.getStatusesInTesting())) {
                            return null;
                        }
                    }
                    List<KeyTime> keyTimes = super.check(jiraClient, ticket);
                    if (keyTimes != null && !keyTimes.isEmpty()) {
                        keyTimes = keyTimes.subList(0, 1);
                        KeyTime keyTime = keyTimes.get(0);
                        keyTime.setWho(findResponsibleDev(jiraClient, ticket, devProductivityReportParams.getStatusesInDevelopment()));
                        keyTime.setWeight(calcWeight(devProductivityReportParams, jiraClient, ticket));
                    }
                    return keyTimes;
                } else {
                    return null;
                }
            }

        }));
    }

    protected boolean isIgnoreTask(DevProductivityReportParams params, ITicket ticket) throws IOException {
        String cleanedText = ticket.getTicketTitle().replace((char)160, ' ').trim();
        String[] ignoreTicketPrefixes = params.getIgnoreTicketPrefixes();
        if (ignoreTicketPrefixes == null) {
            return false;
        }
        for (String ignoreTicketPrefix : ignoreTicketPrefixes) {
            if (cleanedText.startsWith(ignoreTicketPrefix)) {
                return true;
            }
        }
        return false;
    }

    public String findResponsibleDev(TrackerClient jiraClient, ITicket ticket, String ... inDevStatuses) throws IOException {
        Pair<String, IHistoryItem> lastAssigneeForStatus = ChangelogAssessment.findLastAssigneeForStatus(jiraClient, ticket.getKey(), ticket, Employees.getDevelopers(), inDevStatuses);
        String result = lastAssigneeForStatus.getKey();
        if (result == null) {
            result = ChangelogAssessment.findWhoFromEmployeeMovedToStatus(jiraClient, ticket.getKey(), ticket, Employees.getDevelopers(), inDevStatuses);
        }
        if (result == null) {
            return "Unknown";
        }
        return result;
    }

    protected boolean isStory(ITicket ticket) throws IOException {
        return !IssueType.isBug(ticket.getIssueType()) && !IssueType.isSubTask(ticket.getFields().getIssueType().getName()) && thereIsNoSubtasks(ticket) || isSubTaskLinkedToStory(ticket);
    }

    protected boolean isBug(ITicket ticket) throws IOException {
        return IssueType.isBug(ticket.getIssueType()) && thereIsNoSubtasks(ticket) || isSubTaskLinkedToBug(ticket);
    }

    protected boolean isSubTaskLinkedToStory(ITicket ticket) {
        if (IssueType.isSubTask(ticket.getFields().getIssueType().getName())) {
            return IssueType.isStory(ticket.getFields().getParent().getIssueType()) || IssueType.isTask(ticket.getFields().getParent().getIssueType());
        }
        return false;
    }

    protected boolean isSubTaskLinkedToBug(ITicket ticket) {
        if (IssueType.isSubTask(ticket.getFields().getIssueType().getName())) {
            return IssueType.isBug(ticket.getFields().getParent().getIssueType());
        }
        return false;
    }

    protected boolean thereIsNoSubtasks(ITicket ticket) throws IOException {
        List subTasks = ((JiraClient<Ticket>)BasicJiraClient.getInstance()).performGettingSubtask(ticket.getTicketKey());
        return subTasks.isEmpty();
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
            if (isStory(ticket)) {
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


    protected void timeSpentOnStoryDevelopment(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics) throws IOException {
        listOfCustomMetrics.add(new Metric("Story Development Time In Days", devProductivityReportParams.isWeight(), new TrackerRule<ITicket>() {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws IOException, Exception {
                if (isIgnoreTask(devProductivityReportParams, ticket)) return null;

                if (isStory(ticket)) {
                    List<KeyTime> keyTimes = timeSpentOn(devProductivityReportParams, jiraClient, ticket, devProductivityReportParams.getInitialStatus(), devProductivityReportParams.getStatusesInDevelopment());
                    if (keyTimes != null) {
                        for (KeyTime keyTime : keyTimes) {
                            keyTime.setWho(findResponsibleDev(jiraClient, ticket, devProductivityReportParams.getStatusesInDevelopment()));
                        }
                        return keyTimes;
                    }
                }
                return null;
            }
        }));
    }

    protected void timeSpentOnBugfixing(DevProductivityReportParams devProductivityReportParams, List<Metric> listOfCustomMetrics) throws IOException {
        listOfCustomMetrics.add(new Metric("Bugfixing Time In Days", devProductivityReportParams.isWeight(), new TrackerRule<ITicket>() {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws IOException, Exception {
                if (isIgnoreTask(devProductivityReportParams, ticket)) return null;

                if (isBug(ticket)) {
                    List<KeyTime> keyTimes = timeSpentOn(devProductivityReportParams, jiraClient, ticket, devProductivityReportParams.getInitialStatus(), devProductivityReportParams.getStatusesInDevelopment());
                    if (keyTimes != null) {
                        for (KeyTime keyTime : keyTimes) {
                            keyTime.setWho(findResponsibleDev(jiraClient, ticket, devProductivityReportParams.getStatusesInDevelopment()));
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
