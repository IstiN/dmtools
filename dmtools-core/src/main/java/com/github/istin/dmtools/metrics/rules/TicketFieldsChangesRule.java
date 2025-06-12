package com.github.istin.dmtools.metrics.rules;

import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.PropertyReader;
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
    private boolean isCollectionIfByCreator = false;
    private PropertyReader propertyReader;

    public TicketFieldsChangesRule(Employees employees) {
        this(employees, null, false, false);
    }

    public TicketFieldsChangesRule(Employees employees, String[] filterFields, boolean isSimilarity, boolean isCollectionIfByCreator) {
        this.employees = employees;
        this.filterFields = filterFields;
        this.isSimilarity = isSimilarity;
        this.isCollectionIfByCreator = isCollectionIfByCreator;
        this.propertyReader = new PropertyReader();
    }

    public TicketFieldsChangesRule(String customName, Employees employees) {
        this(customName, employees, null, false, false);
    }

    public TicketFieldsChangesRule(String customName, Employees employees, String[] filterFields, boolean isSimilarity, boolean isCollectionIfByCreator) {
        this.customName = customName;
        this.employees = employees;
        this.filterFields = filterFields;
        this.isSimilarity = isSimilarity;
        this.isCollectionIfByCreator = isCollectionIfByCreator;
        this.propertyReader = new PropertyReader();
    }

    @Override
    public List<KeyTime> check(TrackerClient trackerClient, ITicket ticket) throws Exception {
        IChangelog changeLog = trackerClient.getChangeLog(ticket.getKey(), ticket);
        String who = ChangelogAssessment.whoReportedTheTicket((Ticket) ticket, employees);

        List<IHistory> histories = (List<IHistory>) changeLog.getHistories();
        String lastAssignee = null;
        List<KeyTime> result = new ArrayList<>();
        for (IHistory history : histories) {
            IUser author = history.getAuthor();
            if (author != null && employees.contains(author.getFullName())) {
                String authorName = employees.transformName(author.getFullName());
                if (isCollectionIfByCreator) {
                    if (!authorName.equalsIgnoreCase(who)) {
                        continue;
                    }
                } else {
                    if (authorName.equalsIgnoreCase(who)) {
                        continue;
                    }
                }
                List<IHistoryItem> items = (List<IHistoryItem>) history.getHistoryItems();
                for (IHistoryItem historyItem : items) {
                    double weight = 1;
                    String fromAsString = historyItem.getFromAsString();
                    if (fromAsString == null) {
                        fromAsString = "";
                    }
                    String toAsString = historyItem.getToAsString();
                    if (toAsString == null) {
                        toAsString = "";
                    }
                    String field = historyItem.getField();
                    if (filterFields != null) {
                        boolean found = false;
                        //System.out.println("Fields: " + field);
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

                            float similarityResult = metric.compare(fromAsString, toAsString);
                            //System.out.println("Similarity Before: " + historyItem.getFromAsString());
                            //System.out.println("Similarity After: " + historyItem.getToAsString());
                            if (similarityResult > 0) {
//                                if (field.equalsIgnoreCase("description")) {
//                                    System.out.println("Similarity Description: " + similarityResult);
//                                }
                                if (field.equalsIgnoreCase("summary")) {
                                    System.out.println("Similarity Summary: " + similarityResult);
                                }
                            } else {
                                continue;
                            }
                            weight = 1-similarityResult;
                        } else {
                            Double ticketFieldsChangedDivider = propertyReader.getTicketFieldsChangedDivider(field);
                            if (ticketFieldsChangedDivider == null) {
                                weight = weight + 1d;
                            } else {
                                weight = weight + 1d/ticketFieldsChangedDivider;
                            }
                        }
                    } else {
                        Double ticketFieldsChangedDivider = propertyReader.getTicketFieldsChangedDivider(field);
                        if (ticketFieldsChangedDivider != null) {
                            weight = weight / ticketFieldsChangedDivider;
                        }
                    }
                    KeyTime keyTime = new KeyTime(ticket.getKey() + fromAsString + toAsString, history.getCreated(), authorName);
                    keyTime.setWeight(weight);
                    result.add(keyTime);
                }
            }
        }
        return result;
    }

}
