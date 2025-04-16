package com.github.istin.dmtools.report.presentation;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.utils.StringUtils;
import com.github.istin.dmtools.report.model.TableData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarkdownTableGenerator implements TableGenerator {

    @Override
    public String generateTable(TableData tableData) {
        StringBuilder builder = new StringBuilder();

        // Add title
        if (tableData.getTitle() != null && !tableData.getTitle().isEmpty()) {
            builder.append("## ").append(tableData.getTitle()).append("\n\n");
        }

        // Add description if present
        if (tableData.getDescription() != null && !tableData.getDescription().isEmpty()) {
            builder.append(tableData.getDescription()).append("\n\n");
        }

        // Add table headers
        builder.append("| ");
        for (String header : tableData.getHeaders()) {
            builder.append(header).append(" | ");
        }
        builder.append("\n|");

        // Add header separator
        for (int i = 0; i < tableData.getHeaders().size(); i++) {
            builder.append(" --- |");
        }
        builder.append("\n");

        // Add rows
        for (List<String> row : tableData.getRows()) {
            builder.append("| ");
            for (String cell : row) {
                builder.append(StringUtils.cleanTextForMarkdown(cell)).append(" | ");
            }
            builder.append("\n");
        }

        builder.append("\n");
        return builder.toString();
    }

    @Override
    public String generateTable(List<ITicket> tickets) {
        // Default implementation - create a simple table with key, type, and summary
        String[] defaultColumns = {"Key", "Type", "Priority", "Summary"};
        return generateTable(tickets, defaultColumns, false, false);
    }

    @Override
    public String generateTable(List<ITicket> tickets, String[] columns, boolean includeDescription, boolean includeStoryPoints) {
        // Create TableData from tickets and columns
        List<String> headers = new ArrayList<>(Arrays.asList(columns));

        TableData tableData = new TableData("Tickets", headers);

        for (ITicket ticket : tickets) {
            List<String> row = new ArrayList<>();
            try {
                for (String column : columns) {
                    switch (column.toLowerCase()) {
                        case "key":
                            row.add(ticket.getKey());
                            break;
                        case "type":
                            row.add(ticket.getIssueType());
                            break;
                        case "priority":
                            row.add(ticket.getPriority());
                            break;
                        case "story points":
                            row.add(String.valueOf(ticket.getWeight()));
                            break;
                        case "summary":
                            row.add(ticket.getTicketTitle());
                            break;
                        case "description":
                            if (includeDescription) {
                                row.add(StringUtils.removeUrls(ticket.getTicketDescription()));
                            } else {
                                row.add("");
                            }
                            break;
                        case "closed date":
                            row.add(ticket.getFieldsAsJSON().getString("dateClosed"));
                            break;
                        default:
                            row.add("");
                    }
                }
                tableData.addRow(row);
            } catch (IOException e) {
                System.err.println("Error generating table row: " + e.getMessage());
            }
        }

        return generateTable(tableData);
    }
}