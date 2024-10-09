package com.github.istin.dmtools.report.productivity;

import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.timeline.Release;
import com.github.istin.dmtools.common.timeline.WeeksReleaseGenerator;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.metrics.Metric;
import com.github.istin.dmtools.metrics.rules.TestCasesCreatorsRule;
import com.github.istin.dmtools.metrics.rules.TicketAttachmentRule;
import com.github.istin.dmtools.metrics.rules.TicketFieldsChangesRule;
import com.github.istin.dmtools.report.ProductivityTools;
import com.github.istin.dmtools.team.Employees;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestCasesReport extends AbstractJob<TestCasesReportParams> {

    @Override
    public void runJob(TestCasesReportParams qaProductivityReportParams) throws Exception {
        WeeksReleaseGenerator releaseGenerator = new WeeksReleaseGenerator(qaProductivityReportParams.getStartDate());
        String formula = qaProductivityReportParams.getFormula();
        ProductivityTools.generate(BasicJiraClient.getInstance(), releaseGenerator, qaProductivityReportParams.getReportName() + (qaProductivityReportParams.isWeight() ? "_sp" : ""), formula, qaProductivityReportParams.getInputJQL(), generateListOfMetrics(qaProductivityReportParams), Release.Style.BY_SPRINTS, Employees.getTesters(qaProductivityReportParams.getEmployees()));
    }

    protected List<Metric> generateListOfMetrics(TestCasesReportParams qaProductivityReportParams) throws IOException {
        List<Metric> listOfCustomMetrics = new ArrayList<>();
        numberOfAttachments(qaProductivityReportParams, listOfCustomMetrics);
        createdTests(qaProductivityReportParams, listOfCustomMetrics);
        summaryChanged(qaProductivityReportParams, listOfCustomMetrics);
        descriptionChanged(qaProductivityReportParams, listOfCustomMetrics);
        priorityChanged(qaProductivityReportParams, listOfCustomMetrics);
        return listOfCustomMetrics;
    }

    protected void summaryChanged(TestCasesReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Ticket Summary Changed", qaProductivityReportParams.isWeight(), new TicketFieldsChangesRule(Employees.getTesters(qaProductivityReportParams.getEmployees()), new String[]{"summary"}, true)));
    }

    protected void descriptionChanged(TestCasesReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Ticket Description Changed", qaProductivityReportParams.isWeight(), new TicketFieldsChangesRule(Employees.getTesters(qaProductivityReportParams.getEmployees()), new String[]{"description"}, true)));
    }

    protected void priorityChanged(TestCasesReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Ticket Priority Changed", qaProductivityReportParams.isWeight(), new TicketFieldsChangesRule(Employees.getTesters(qaProductivityReportParams.getEmployees()), new String[]{"priority"}, false)));
    }


    protected void numberOfAttachments(TestCasesReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Number of Attachments", qaProductivityReportParams.isWeight(), new TicketAttachmentRule(null, Employees.getTesters(qaProductivityReportParams.getEmployees()))));
    }


    protected void createdTests(TestCasesReportParams qaProductivityReportParams, List<Metric> listOfCustomMetrics) {
        listOfCustomMetrics.add(new Metric("Created Tests", qaProductivityReportParams.isWeight(), new TestCasesCreatorsRule(qaProductivityReportParams.getTestCasesProjectCode(), Employees.getTesters(qaProductivityReportParams.getEmployees()))));
    }

}