package com.github.istin.dmtools.metrics.rules;

import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.metrics.TrackerRule;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.Employees;

import java.util.ArrayList;
import java.util.List;

public class TicketFieldsChangesRule implements TrackerRule<ITicket> {

    private String customName;
    private final Employees employees;

    public TicketFieldsChangesRule(Employees employees) {
        this.employees = employees;
    }

    public TicketFieldsChangesRule(String customName, Employees employees) {
        this.customName = customName;
        this.employees = employees;
    }

    @Override
    public List<KeyTime> check(TrackerClient trackerClient, ITicket ticket) throws Exception {
        IChangelog changeLog = trackerClient.getChangeLog(ticket.getKey(), ticket);
        List<IHistory> histories = (List<IHistory>) changeLog.getHistories();
        String lastAssignee = null;
        List<KeyTime> result = new ArrayList<>();
        for (IHistory history : histories) {
            IUser author = history.getAuthor();
            if (author != null && employees.contains(author.getFullName())) {
                List<IHistoryItem> items = (List<IHistoryItem>) history.getHistoryItems();
                for (IHistoryItem historyItem : items) {
                    KeyTime keyTime = new KeyTime(ticket.getKey(), history.getCreated(), employees.transformName(author.getFullName()));
                    keyTime.setWeight(0.03);
                    result.add(keyTime);
                }
            }
        }
        return result;
    }

}
