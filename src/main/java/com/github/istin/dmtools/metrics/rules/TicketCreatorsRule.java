package com.github.istin.dmtools.metrics.rules;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.metrics.TrackerRule;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.Employees;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TicketCreatorsRule implements TrackerRule<ITicket> {

    private String project;

    private String customName;
    private final Employees employees;

    public TicketCreatorsRule(String project, Employees employees) {
        this.project = project;
        this.employees = employees;
    }

    public TicketCreatorsRule(String project, String customName, Employees employees) {
        this.project = project;
        this.customName = customName;
        this.employees = employees;
    }

    @Override
    public List<KeyTime> check(TrackerClient trackerClient, ITicket ticket) throws Exception {
        if (project != null && !ticket.getKey().startsWith(project)) {
            return null;
        }
        Date created = ticket.getFields().getCreated();
        Calendar instance = Calendar.getInstance();
        instance.setTime(created);
        String who = null;
        Assignee creator = ticket.getFields().getCreator();
        Assignee reporter = ticket.getFields().getReporter();
        if (employees != null) {
            String creatorDisplayName = creator.getDisplayName();
            if (employees.contains(creatorDisplayName)) {
                who = creatorDisplayName;
            } else if (reporter != null) {
                String reporterDisplayName = reporter.getDisplayName();
                if (employees.contains(reporterDisplayName)) {
                    who = reporterDisplayName;
                }
            }
        }
        if (who == null && employees != null) {
            who = Employees.UNKNOWN;
        }
        return Arrays.asList(new KeyTime(ticket.getKey(), instance, customName == null ? (employees != null ? employees.transformName(who) : who) : customName) );
    }

}
