package com.github.istin.dmtools.report.productivity;

import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.timeline.Release;
import com.github.istin.dmtools.common.timeline.WeeksReleaseGenerator;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.metrics.Metric;
import com.github.istin.dmtools.metrics.rules.*;
import com.github.istin.dmtools.report.ProductivityTools;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.Employees;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QAProductivityReport extends AbstractJob<QAProductivityReportParams> {

    @Override
    public void runJob(QAProductivityReportParams qaProductivityReportParams) throws Exception {
        WeeksReleaseGenerator releaseGenerator = new WeeksReleaseGenerator(qaProductivityReportParams.getStartDate());
        String formula = qaProductivityReportParams.getFormula();
        ProductivityTools.generate(BasicJiraClient.getInstance(), releaseGenerator, qaProductivityReportParams.getReportName() + (qaProductivityReportParams.isWeight() ? "_sp" : ""), formula, qaProductivityReportParams.getInputJQL(), generateListOfMetrics(qaProductivityReportParams), Release.Style.BY_SPRINTS, Employees.getTesters());
    }

    protected List<Metric> generateListOfMetrics(QAProductivityReportParams qaProductivityReportParams) throws IOException {
        List<Metric> listOfCustomMetrics = new ArrayList<>();
        createdBugs(qaProductivityReportParams, listOfCustomMetrics);
        numberOfAttachments(qaProductivityReportParams, listOfCustomMetrics);
        numberOfComponents(qaProductivityReportParams, listOfCustomMetrics);
        movedToDone(qaProductivityReportParams, listOfCustomMetrics);
        movedToReopened(qaProductivityReportParams, listOfCustomMetrics);
        createdTests(qaProductivityReportParams, listOfCustomMetrics);
        fieldsChanged(qaProductivityReportParams, listOfCustomMetrics);
        ProductivityUtils.vacationDays(listOfCustomMetrics, Employees.getTesters());
//        numberOfRejectedBugs(qaProductivityReportParams, listOfCustomMetrics);
        return listOfCustomMetrics;
    }

    //

    protected void fieldsChanged(QAProductivityReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Ticket Fields Changed", qaProductivityReportParams.isWeight(), new TicketFieldsChangesRule(Employees.getTesters())));
    }

    protected void createdBugs(QAProductivityReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Created Bugs", qaProductivityReportParams.isWeight(), new BugsCreatorsRule(qaProductivityReportParams.getBugsProjectCode(), Employees.getTesters())));
    }

    protected void numberOfAttachments(QAProductivityReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Number of Attachments", qaProductivityReportParams.isWeight(), new TicketAttachmentRule(null, Employees.getTesters())));
    }

    protected void numberOfComponents(QAProductivityReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Number of Components", qaProductivityReportParams.isWeight(), new ComponentsRule(qaProductivityReportParams.getBugsProjectCode(), Employees.getTesters())));
    }

    protected void movedToDone(QAProductivityReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        Boolean weight = qaProductivityReportParams.isWeight();
        listOfCustomMetrics.add(new Metric("Stories Moved To Done", weight, new TicketMovedToStatusRule(qaProductivityReportParams.getStatusesDone(), null, false) {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws Exception {
                if (ProductivityUtils.isIgnoreTask(qaProductivityReportParams.getIgnoreTicketPrefixes(), ticket)) return null;
                if (ProductivityUtils.isStory(qaProductivityReportParams, ticket)) {
                    List<KeyTime> keyTimes = super.check(jiraClient, ticket);
                    if (keyTimes != null && !keyTimes.isEmpty()) {
                        keyTimes = keyTimes.subList(0, 1);
                        KeyTime keyTime = keyTimes.get(0);
                        keyTime.setWho(findResponsibleQA(jiraClient, ticket, qaProductivityReportParams.getStatusesInTesting(), qaProductivityReportParams.getStatusesDone()));
                    }
                    return keyTimes;
                } else {
                    return null;
                }
            }

        }));
    }

    public String findResponsibleQA(TrackerClient trackerClient, ITicket ticket, String[] inProgressStatuses, String[] doneStatuses) throws IOException {
        String whoIsResponsible = ChangelogAssessment.findWhoIsResponsible(trackerClient, Employees.getTesters(), ticket, inProgressStatuses);
        if (whoIsResponsible.equalsIgnoreCase(Employees.UNKNOWN)) {
            whoIsResponsible = ChangelogAssessment.findWhoIsResponsible(trackerClient, Employees.getTesters(), ticket, doneStatuses);
        }
        return whoIsResponsible;
    }

    protected void movedToReopened(QAProductivityReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {

        listOfCustomMetrics.add(new Metric("Items Moved to Reopened", qaProductivityReportParams.isWeight(), new TicketMovedToStatusRule(qaProductivityReportParams.getStatusesInTesting(), false) {
            @Override
            public List<KeyTime> check(TrackerClient jiraClient, ITicket ticket) throws Exception {
                if (ProductivityUtils.isIgnoreTask(qaProductivityReportParams.getIgnoreTicketPrefixes(), ticket)) return null;
                List<KeyTime> keyTimes = super.check(jiraClient, ticket);
                if (keyTimes != null && !keyTimes.isEmpty()) {
                    if (!ChangelogAssessment.isFirstTimeRight(jiraClient, ticket.getTicketKey(), ticket, qaProductivityReportParams.getStatusesInDevelopment(), qaProductivityReportParams.getStatusesInTesting())) {
                        return keyTimes;
                    } else {
                        return null;
                    }
                }
                return keyTimes;
            }
        }));
    }

    protected void createdTests(QAProductivityReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Created Tests", qaProductivityReportParams.isWeight(), new TestCasesCreatorsRule(qaProductivityReportParams.getTestCasesProjectCode(), Employees.getTesters())));
    }

//    protected void numberOfRejectedBugs(QAProductivityReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
//        listOfCustomMetrics.add(new Metric("Number of Rejected Bugs", qaProductivityReportParams.isWeight(), new QAReportRules.NumberOfRejectedBugsRule()));
//    }
}