package com.github.istin.dmtools.report.productivity;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.timeline.Release;
import com.github.istin.dmtools.common.timeline.WeeksReleaseGenerator;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.metrics.Metric;
import com.github.istin.dmtools.metrics.rules.CommentsWrittenRule;
import com.github.istin.dmtools.metrics.rules.TestCasesCreatorsRule;
import com.github.istin.dmtools.metrics.rules.TicketAttachmentRule;
import com.github.istin.dmtools.metrics.rules.TicketFieldsChangesRule;
import com.github.istin.dmtools.report.ProductivityTools;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.Employees;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AIAgentsReport extends AbstractJob<AIAgentsReportParams> {

    @Override
    public void runJob(AIAgentsReportParams qaProductivityReportParams) throws Exception {
        WeeksReleaseGenerator releaseGenerator = new WeeksReleaseGenerator(qaProductivityReportParams.getStartDate());
        String formula = qaProductivityReportParams.getFormula();
        ProductivityTools.generate(BasicJiraClient.getInstance(), releaseGenerator, qaProductivityReportParams.getReportName() + (qaProductivityReportParams.isWeight() ? "_sp" : ""), formula, qaProductivityReportParams.getInputJQL(), generateListOfMetrics(qaProductivityReportParams), Release.Style.BY_SPRINTS, Employees.getTesters(qaProductivityReportParams.getEmployees()), qaProductivityReportParams.getIgnoreTicketPrefixes());
    }

    @Override
    public AI getAi() {
        return null;
    }

    protected List<Metric> generateListOfMetrics(AIAgentsReportParams qaProductivityReportParams) throws IOException {
        List<Metric> listOfCustomMetrics = new ArrayList<>();
        numberOfAttachments(qaProductivityReportParams, listOfCustomMetrics);
        createdTests(qaProductivityReportParams, listOfCustomMetrics);
//        summaryChanged(qaProductivityReportParams, listOfCustomMetrics);
//        descriptionChanged(qaProductivityReportParams, listOfCustomMetrics);
//        priorityChanged(qaProductivityReportParams, listOfCustomMetrics);
        commentsWritten(qaProductivityReportParams, listOfCustomMetrics);
        ticketLinksChanged(qaProductivityReportParams, listOfCustomMetrics);
        return listOfCustomMetrics;
    }

    private void commentsWritten(AIAgentsReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Ticket Comments Written", qaProductivityReportParams.isWeight(), new CommentsWrittenRule(Employees.getTesters(qaProductivityReportParams.getEmployees()), qaProductivityReportParams.getCommentsRegex())));
    }

//    protected void summaryChanged(AIAgentsReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
//        listOfCustomMetrics.add(new Metric("Ticket Summary Changed", qaProductivityReportParams.isWeight(), new TicketFieldsChangesRule(Employees.getTesters(qaProductivityReportParams.getEmployees()), new String[]{"summary"}, true, false)));
//    }
//
//    protected void descriptionChanged(AIAgentsReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
//        listOfCustomMetrics.add(new Metric("Ticket Description Changed", qaProductivityReportParams.isWeight(), new TicketFieldsChangesRule(Employees.getTesters(qaProductivityReportParams.getEmployees()), new String[]{"description"}, true, false)));
//    }
//
//    protected void priorityChanged(AIAgentsReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
//        listOfCustomMetrics.add(new Metric("Ticket Priority Changed", qaProductivityReportParams.isWeight(), new TicketFieldsChangesRule(Employees.getTesters(qaProductivityReportParams.getEmployees()), new String[]{"priority"}, false, false)));
//    }

    protected void ticketLinksChanged(AIAgentsReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Test Ticket Linked", qaProductivityReportParams.isWeight(), new TicketFieldsChangesRule(Employees.getTesters(qaProductivityReportParams.getEmployees()), new String[]{"link"}, false, false) {
            @Override
            public List<KeyTime> check(TrackerClient trackerClient, ITicket ticket) throws Exception {
                if (IssueType.isTestCase(ticket.getIssueType())) {
                    return super.check(trackerClient, ticket);
                } else {
                    return Collections.emptyList();
                }
            }
        }));
    }

    protected void numberOfAttachments(AIAgentsReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Number of Attachments", qaProductivityReportParams.isWeight(), new TicketAttachmentRule(null, Employees.getTesters(qaProductivityReportParams.getEmployees()))));
    }

    protected void createdTests(AIAgentsReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Created Tests", qaProductivityReportParams.isWeight(), new TestCasesCreatorsRule(null, Employees.getTesters(qaProductivityReportParams.getEmployees()))));
    }

}