package com.github.istin.dmtools.report.projectstatus.presentation.tables;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.utils.StringUtils;
import com.github.istin.dmtools.report.projectstatus.data.TicketSorter;
import com.github.istin.dmtools.report.projectstatus.data.TicketStatisticsCalculator;
import com.github.istin.dmtools.report.projectstatus.model.TableData;
import com.github.istin.dmtools.report.projectstatus.presentation.TableGenerator;

import java.io.IOException;
import java.util.*;

public class BugTableGenerator implements TableGenerator {
    private final TableGenerator baseTableGenerator;
    private final TicketStatisticsCalculator statisticsCalculator;
    private final TicketSorter ticketSorter;

    public BugTableGenerator(TableGenerator baseTableGenerator, TicketStatisticsCalculator statisticsCalculator, TicketSorter ticketSorter) {
        this.baseTableGenerator = baseTableGenerator;
        this.statisticsCalculator = statisticsCalculator;
        this.ticketSorter = ticketSorter;
    }

    @Override
    public String generateTable(TableData tableData) {
        return baseTableGenerator.generateTable(tableData);
    }

    @Override
    public String generateTable(List<ITicket> tickets) {
        // Default to bug overview for the simple method
        return generateBugOverviewTable(tickets);
    }

    @Override
    public String generateTable(List<ITicket> tickets, String[] columns, boolean includeDescription, boolean includeStoryPoints) {
        // Default implementation
        return generateTable(tickets);
    }

    public String generateBugOverviewTable(List<ITicket> bugs) {
        // Count bugs by priority
        Map<String, Integer> bugsByPriority = statisticsCalculator.countTicketsByPriority(bugs);

        // Sort priorities by predefined order
        List<String> sortedPriorities = ticketSorter.sortPriorities(bugsByPriority.keySet());

        // Create table headers
        List<String> headers = Arrays.asList("Priority", "Count", "Percentage");
        TableData tableData = new TableData("Bug Overview by Priority", headers);

        // Add data rows
        int totalBugs = bugs.size();
        for (String priority : sortedPriorities) {
            int count = bugsByPriority.get(priority);
            double percentage = statisticsCalculator.calculatePercentage(count, totalBugs);

            List<String> row = Arrays.asList(
                    priority,
                    String.valueOf(count),
                    String.format("%.1f%%", percentage)
            );
            tableData.addRow(row);
        }

        // Add totals row
        List<String> totalsRow = Arrays.asList(
                "**Total**",
                String.valueOf(totalBugs),
                "100.0%"
        );
        tableData.addRow(totalsRow);

        return baseTableGenerator.generateTable(tableData);
    }

    public String generateBugsTable(List<ITicket> bugs) {
        // Create table headers
        List<String> headers = Arrays.asList("Key", "Priority", "Closed Date", "Summary", "Description");
        TableData tableData = new TableData("Bugs", headers);

        // Add bug rows
        for (ITicket ticket : bugs) {
            try {
                List<String> row = Arrays.asList(
                        ticket.getKey(),
                        TicketStatisticsCalculator.nullToEmpty(ticket.getPriority(), "Trivial"),
                        ticket.getFieldsAsJSON().optString("dateClosed"),
                        ticket.getTicketTitle(),
                        StringUtils.cleanTextForMarkdown(ticket.getTicketDescription())
                );
                tableData.addRow(row);
            } catch (IOException e) {
                System.err.println("Error adding bug to table: " + e.getMessage());
            }
        }

        return baseTableGenerator.generateTable(tableData);
    }
}