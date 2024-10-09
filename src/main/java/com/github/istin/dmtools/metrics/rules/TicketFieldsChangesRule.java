package com.github.istin.dmtools.metrics.rules;

import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.metrics.TrackerRule;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.Employees;
import java.util.ArrayList;
import java.util.List;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

public class TicketFieldsChangesRule implements TrackerRule<ITicket> {

    private String customName;
    private final Employees employees;
    private final String[] filterFields;
    private boolean isSimilarity = false;

    public TicketFieldsChangesRule(Employees employees) {
        this(employees, null, false);
    }

    public TicketFieldsChangesRule(Employees employees, String[] filterFields, boolean isSimilarity) {
        this.employees = employees;
        this.filterFields = filterFields;
        this.isSimilarity = isSimilarity;
    }

    public TicketFieldsChangesRule(String customName, Employees employees) {
        this(customName, employees, null, false);
    }

    public TicketFieldsChangesRule(String customName, Employees employees, String[] filterFields, boolean isSimilarity) {
        this.customName = customName;
        this.employees = employees;
        this.filterFields = filterFields;
        this.isSimilarity = isSimilarity;
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
                    double weight = 0.03;
                    if (filterFields != null) {
                        boolean found = false;
                        String field = historyItem.getField();
                        for (String fieldToMap : filterFields) {
                            if (fieldToMap.equalsIgnoreCase(field)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            continue;
                        }
                        if (isSimilarity) {
                            StringMetric metric = StringMetrics.levenshtein();
                            float similarityResult = metric.compare(historyItem.getFromAsString(), historyItem.getToAsString());
                            System.out.println("Similarity Before: " + historyItem.getFromAsString());
                            System.out.println("Similarity After: " + historyItem.getToAsString());
                            System.out.println("Similarity: " + similarityResult);
                            weight = 1-similarityResult;
                        }
                    }
                    KeyTime keyTime = new KeyTime(ticket.getKey(), history.getCreated(), employees.transformName(author.getFullName()));
                    keyTime.setWeight(weight);
                    result.add(keyTime);
                }
            }
        }
        return result;
    }

}
