package com.github.istin.dmtools.report.projectstatus.presentation.tables;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.report.projectstatus.data.TicketStatisticsCalculator;
import com.github.istin.dmtools.report.projectstatus.model.TableData;
import com.github.istin.dmtools.report.projectstatus.model.TimelinePeriod;
import com.github.istin.dmtools.report.projectstatus.presentation.TableGenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TimelineTableGenerator implements TableGenerator {
    private final TableGenerator baseTableGenerator;

    public TimelineTableGenerator(TableGenerator baseTableGenerator) {
        this.baseTableGenerator = baseTableGenerator;
    }

    @Override
    public String generateTable(TableData tableData) {
        return baseTableGenerator.generateTable(tableData);
    }

    @Override
    public String generateTable(List<ITicket> tickets) {
        // Default to monthly timeline
        return generateTimelineTable(tickets, TimelinePeriod.MONTH);
    }

    @Override
    public String generateTable(List<ITicket> tickets, String[] columns, boolean includeDescription, boolean includeStoryPoints) {
        return generateTable(tickets);
    }

    /**
     * Generates a timeline table showing deliverables over time
     *
     * @param tickets The list of tickets to analyze
     * @param period The time period to group by (WEEK, TWO_WEEKS, MONTH, QUARTER)
     * @return A formatted table showing deliverables over time
     */
    public String generateTimelineTable(List<ITicket> tickets, TimelinePeriod period) {
        // Group tickets by time period and type
        Map<String, Map<String, List<ITicket>>> ticketsByPeriodAndType = groupTicketsByPeriodAndType(tickets, period);

        // Get sorted list of periods
        List<String> sortedPeriods = new ArrayList<>(ticketsByPeriodAndType.keySet());
        Collections.sort(sortedPeriods);

        // Get all unique issue types across all periods
        Set<String> allIssueTypes = new TreeSet<>();
        for (Map<String, List<ITicket>> typeMap : ticketsByPeriodAndType.values()) {
            allIssueTypes.addAll(typeMap.keySet());
        }
        List<String> sortedIssueTypes = new ArrayList<>(allIssueTypes);

        // Identify bug-like and feature-like issue types
        List<String> bugTypes = sortedIssueTypes.stream()
                .filter(type -> type.toLowerCase().contains("bug") || type.toLowerCase().contains("defect"))
                .collect(Collectors.toList());

        List<String> featureTypes = sortedIssueTypes.stream()
                .filter(type -> !bugTypes.contains(type))
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder();

        // Generate the main timeline table
        result.append(generateDeliverableCountTable(ticketsByPeriodAndType, sortedPeriods, sortedIssueTypes, period));

        // Generate the story points timeline table
        result.append("\n");
        result.append(generateStoryPointsTable(ticketsByPeriodAndType, sortedPeriods, sortedIssueTypes, period));

        // Generate the averages summary
        result.append("\n");
        result.append(generateAveragesSummary(ticketsByPeriodAndType, sortedPeriods, bugTypes, featureTypes, period));

        // Generate the dual-column chart
        result.append("\n");
        result.append(generateDualColumnChart(ticketsByPeriodAndType, sortedPeriods, bugTypes, period));

        return result.toString();
    }

    /**
     * Generates a table showing the count of deliverables by type over time
     */
    private String generateDeliverableCountTable(Map<String, Map<String, List<ITicket>>> ticketsByPeriodAndType,
                                                 List<String> sortedPeriods, List<String> issueTypes, TimelinePeriod period) {
        // Create table headers
        List<String> headers = new ArrayList<>();
        headers.add("Time Period");
        headers.addAll(issueTypes);
        headers.add("Total");
        headers.add("Total Story Points");

        TableData tableData = new TableData(
                "Deliverables Timeline (" + period.getDescription() + ")",
                headers,
                "Shows the number of completed items by type over time periods."
        );

        // Add data rows
        Map<String, Integer> typeTotals = new HashMap<>();
        int grandTotal = 0;
        double totalStoryPoints = 0;

        for (String periodKey : sortedPeriods) {
            Map<String, List<ITicket>> typeMap = ticketsByPeriodAndType.get(periodKey);

            List<String> row = new ArrayList<>();
            row.add(periodKey);

            int periodTotal = 0;

            // Add counts for each issue type
            for (String issueType : issueTypes) {
                int count = typeMap.getOrDefault(issueType, Collections.emptyList()).size();
                row.add(String.valueOf(count));
                periodTotal += count;

                // Update type totals
                typeTotals.put(issueType, typeTotals.getOrDefault(issueType, 0) + count);
            }

            // Calculate story points for this period
            double periodStoryPoints = calculateTotalStoryPoints(typeMap);

            row.add(String.valueOf(periodTotal));
            row.add(String.format("%.1f", periodStoryPoints));

            tableData.addRow(row);

            // Update totals
            grandTotal += periodTotal;
            totalStoryPoints += periodStoryPoints;
        }

        // Add totals row
        List<String> totalsRow = new ArrayList<>();
        totalsRow.add("**Total**");

        for (String issueType : issueTypes) {
            totalsRow.add(String.valueOf(typeTotals.getOrDefault(issueType, 0)));
        }

        totalsRow.add(String.valueOf(grandTotal));
        totalsRow.add(String.format("%.1f", totalStoryPoints));

        tableData.addRow(totalsRow);

        return baseTableGenerator.generateTable(tableData);
    }

    /**
     * Generates a table showing story points by issue type over time
     */
    private String generateStoryPointsTable(Map<String, Map<String, List<ITicket>>> ticketsByPeriodAndType,
                                            List<String> sortedPeriods, List<String> issueTypes, TimelinePeriod period) {
        // Create table headers
        List<String> headers = new ArrayList<>();
        headers.add("Time Period");

        // Add points columns for each issue type
        for (String issueType : issueTypes) {
            headers.add(issueType + " Points");
        }

        headers.add("Total Points");
        headers.add("Avg Points per Item");

        TableData tableData = new TableData(
                "Story Points Timeline (" + period.getDescription() + ")",
                headers,
                "Shows the distribution of story points by issue type over time periods."
        );

        // Add data rows
        Map<String, Double> typePointsTotals = new HashMap<>();
        double grandTotalPoints = 0;
        int grandTotalItems = 0;

        for (String periodKey : sortedPeriods) {
            Map<String, List<ITicket>> typeMap = ticketsByPeriodAndType.get(periodKey);

            List<String> row = new ArrayList<>();
            row.add(periodKey);

            double periodTotalPoints = 0;

            // Add story points for each issue type
            for (String issueType : issueTypes) {
                double points = calculateStoryPointsForType(typeMap.getOrDefault(issueType, Collections.emptyList()));
                row.add(String.format("%.1f", points));
                periodTotalPoints += points;

                // Update type totals
                typePointsTotals.put(issueType, typePointsTotals.getOrDefault(issueType, 0.0) + points);
            }

            int periodTotalItems = typeMap.values().stream().mapToInt(List::size).sum();
            double avgPointsPerItem = periodTotalItems > 0 ? periodTotalPoints / periodTotalItems : 0;

            row.add(String.format("%.1f", periodTotalPoints));
            row.add(String.format("%.2f", avgPointsPerItem));

            tableData.addRow(row);

            // Update totals
            grandTotalPoints += periodTotalPoints;
            grandTotalItems += periodTotalItems;
        }

        // Add totals row
        List<String> totalsRow = new ArrayList<>();
        totalsRow.add("**Total**");

        for (String issueType : issueTypes) {
            totalsRow.add(String.format("%.1f", typePointsTotals.getOrDefault(issueType, 0.0)));
        }

        double overallAvgPoints = grandTotalItems > 0 ? grandTotalPoints / grandTotalItems : 0;
        totalsRow.add(String.format("%.1f", grandTotalPoints));
        totalsRow.add(String.format("%.2f", overallAvgPoints));

        tableData.addRow(totalsRow);

        return baseTableGenerator.generateTable(tableData);
    }

    /**
     * Generates a summary of average metrics across time periods
     */
    private String generateAveragesSummary(Map<String, Map<String, List<ITicket>>> ticketsByPeriodAndType,
                                           List<String> sortedPeriods, List<String> bugTypes, List<String> featureTypes,
                                           TimelinePeriod period) {
        StringBuilder summary = new StringBuilder();
        summary.append("## Average Metrics per ").append(period.getDescription()).append(" Period\n\n");

        // Calculate totals and averages
        int totalBugs = 0;
        int totalFeatures = 0;
        double totalBugPoints = 0;
        double totalFeaturePoints = 0;
        double totalStoryPoints = 0;

        for (String periodKey : sortedPeriods) {
            Map<String, List<ITicket>> typeMap = ticketsByPeriodAndType.get(periodKey);

            // Count bugs and features
            for (String bugType : bugTypes) {
                totalBugs += typeMap.getOrDefault(bugType, Collections.emptyList()).size();
                totalBugPoints += calculateStoryPointsForType(typeMap.getOrDefault(bugType, Collections.emptyList()));
            }

            for (String featureType : featureTypes) {
                totalFeatures += typeMap.getOrDefault(featureType, Collections.emptyList()).size();
                totalFeaturePoints += calculateStoryPointsForType(typeMap.getOrDefault(featureType, Collections.emptyList()));
            }

            // Calculate total story points
            for (List<ITicket> tickets : typeMap.values()) {
                totalStoryPoints += calculateStoryPointsForType(tickets);
            }
        }

        int periodCount = sortedPeriods.size();
        if (periodCount > 0) {
            // Create a table for the averages
            List<String> headers = Arrays.asList(
                    "Metric", "Total", "Average per " + period.getDescription(), "% of Total Work"
            );

            TableData tableData = new TableData("Work Distribution Averages", headers);

            // Bug metrics
            double avgBugsPerPeriod = (double) totalBugs / periodCount;
            double bugPercentage = totalBugs * 100.0 / (totalBugs + totalFeatures);
            List<String> bugRow = Arrays.asList(
                    "Bugs Fixed",
                    String.valueOf(totalBugs),
                    String.format("%.1f", avgBugsPerPeriod),
                    String.format("%.1f%%", bugPercentage)
            );
            tableData.addRow(bugRow);

            // Feature work metrics
            double avgFeaturesPerPeriod = (double) totalFeatures / periodCount;
            double featureWorkPercentage = totalFeatures * 100.0 / (totalBugs + totalFeatures);
            List<String> featureWorkRow = Arrays.asList(
                    "Feature Work (" + String.join(", ", featureTypes) + ")",
                    String.valueOf(totalFeatures),
                    String.format("%.1f", avgFeaturesPerPeriod),
                    String.format("%.1f%%", featureWorkPercentage)
            );
            tableData.addRow(featureWorkRow);

            // Add individual feature type rows
            for (String featureType : featureTypes) {
                int typeTotal = 0;
                for (Map<String, List<ITicket>> typeMap : ticketsByPeriodAndType.values()) {
                    typeTotal += typeMap.getOrDefault(featureType, Collections.emptyList()).size();
                }

                double avgPerPeriod = (double) typeTotal / periodCount;
                double percentage = typeTotal * 100.0 / (totalBugs + totalFeatures);

                List<String> typeRow = Arrays.asList(
                        featureType + " Completed",
                        String.valueOf(typeTotal),
                        String.format("%.1f", avgPerPeriod),
                        String.format("%.1f%%", percentage)
                );
                tableData.addRow(typeRow);
            }

            // Total items
            int totalItems = totalBugs + totalFeatures;
            double avgItemsPerPeriod = (double) totalItems / periodCount;
            List<String> totalItemsRow = Arrays.asList(
                    "Total Items Completed",
                    String.valueOf(totalItems),
                    String.format("%.1f", avgItemsPerPeriod),
                    "100.0%"
            );
            tableData.addRow(totalItemsRow);

            // Story points metrics
            double avgPointsPerPeriod = totalStoryPoints / periodCount;
            List<String> pointsRow = Arrays.asList(
                    "Total Story Points",
                    String.format("%.1f", totalStoryPoints),
                    String.format("%.1f", avgPointsPerPeriod),
                    "100.0%"
            );
            tableData.addRow(pointsRow);

            // Bug points vs Feature points
            double bugPointsPercentage = totalBugPoints * 100.0 / totalStoryPoints;
            List<String> bugPointsRow = Arrays.asList(
                    "Bug Story Points",
                    String.format("%.1f", totalBugPoints),
                    String.format("%.1f", totalBugPoints / periodCount),
                    String.format("%.1f%%", bugPointsPercentage)
            );
            tableData.addRow(bugPointsRow);

            double featurePointsPercentage = totalFeaturePoints * 100.0 / totalStoryPoints;
            List<String> featurePointsRow = Arrays.asList(
                    "Feature Story Points",
                    String.format("%.1f", totalFeaturePoints),
                    String.format("%.1f", totalFeaturePoints / periodCount),
                    String.format("%.1f%%", featurePointsPercentage)
            );
            tableData.addRow(featurePointsRow);

            summary.append(baseTableGenerator.generateTable(tableData));

            // Add key insights
            summary.append("### Key Insights\n\n");

            // Bug to feature work ratio
            double bugToFeatureRatio = totalBugs > 0 && totalFeatures > 0 ?
                    (double) totalBugs / totalFeatures : 0;
            summary.append("- **Bug to Feature Work Ratio**: ")
                    .append(String.format("%.2f", bugToFeatureRatio))
                    .append(" bugs per feature work item\n");

            // Average velocity
            summary.append("- **Average Velocity**: ")
                    .append(String.format("%.1f", avgPointsPerPeriod))
                    .append(" story points per ")
                    .append(period.getDescription().toLowerCase())
                    .append("\n");

            // Bug work percentage
            summary.append("- **Bug Work**: ")
                    .append(String.format("%.1f%%", bugPercentage))
                    .append(" of total items, ")
                    .append(String.format("%.1f%%", bugPointsPercentage))
                    .append(" of total story points\n");

            // Feature work percentage
            summary.append("- **Feature Work**: ")
                    .append(String.format("%.1f%%", featureWorkPercentage))
                    .append(" of total items, ")
                    .append(String.format("%.1f%%", featurePointsPercentage))
                    .append(" of total story points\n");

            // Average story points per item type
            double avgPointsPerBug = totalBugs > 0 ? totalBugPoints / totalBugs : 0;
            double avgPointsPerFeature = totalFeatures > 0 ? totalFeaturePoints / totalFeatures : 0;
            summary.append("- **Average Points**: ")
                    .append(String.format("%.2f", avgPointsPerBug))
                    .append(" per bug, ")
                    .append(String.format("%.2f", avgPointsPerFeature))
                    .append(" per feature work item\n");
        }

        return summary.toString();
    }

    /**
     * Generates a dual-column chart showing bug count and story points over time
     */
    private String generateDualColumnChart(Map<String, Map<String, List<ITicket>>> ticketsByPeriodAndType,
                                           List<String> sortedPeriods, List<String> bugTypes, TimelinePeriod period) {
        StringBuilder chart = new StringBuilder();
        chart.append("## Bug Count vs Story Points (" + period.getDescription() + ")\n\n");
        chart.append("```\n");

        // Calculate bug counts and story points for each period
        List<Integer> bugCounts = new ArrayList<>();
        List<Double> storyPointsValues = new ArrayList<>();

        int maxBugCount = 0;
        double maxStoryPoints = 0;

        for (String periodKey : sortedPeriods) {
            Map<String, List<ITicket>> typeMap = ticketsByPeriodAndType.get(periodKey);

            // Count bugs for this period
            int bugCount = 0;
            for (String bugType : bugTypes) {
                bugCount += typeMap.getOrDefault(bugType, Collections.emptyList()).size();
            }

            // Calculate total story points for this period
            double storyPoints = calculateTotalStoryPoints(typeMap);

            bugCounts.add(bugCount);
            storyPointsValues.add(storyPoints);

            maxBugCount = Math.max(maxBugCount, bugCount);
            maxStoryPoints = Math.max(maxStoryPoints, storyPoints);
        }

        // Chart settings
        int chartHeight = 15;

        // Handle case where there are no bugs
        if (maxBugCount == 0) {
            maxBugCount = 1; // Set to 1 to avoid division by zero
        }

        // Y-axis scale for bugs (left side)
        double bugScale = (double) chartHeight / maxBugCount;

        // Handle case where there are no story points
        if (maxStoryPoints == 0) {
            maxStoryPoints = 1.0; // Set to 1.0 to avoid division by zero
        }

        // Y-axis scale for story points (right side)
        double pointsScale = (double) chartHeight / maxStoryPoints;

        // Generate chart rows from top to bottom
        for (int row = chartHeight; row >= 0; row--) {
            // Left Y-axis label (bugs)
            if (row % 3 == 0 || row == chartHeight) {
                // Calculate bug value for this row
                int bugValue = (int) Math.round(row / bugScale);
                chart.append(String.format("%3d |", bugValue));
            } else {
                chart.append("    |");
            }

            // Chart data
            for (int i = 0; i < sortedPeriods.size(); i++) {
                int bugHeight = (int) Math.round(bugCounts.get(i) * bugScale);
                int pointsHeight = (int) Math.round(storyPointsValues.get(i) * pointsScale);

                // Bug column (█)
                if (row <= bugHeight && bugCounts.get(i) > 0) {
                    chart.append("█");
                } else {
                    chart.append(" ");
                }

                // Spacer
                chart.append(" ");

                // Story points column (▓)
                if (row <= pointsHeight && storyPointsValues.get(i) > 0) {
                    chart.append("▓");
                } else {
                    chart.append(" ");
                }

                // Column separator
                chart.append(" ");
            }

            // Right Y-axis label (story points)
            if (row % 3 == 0 || row == chartHeight) {
                // Calculate story points value for this row
                double pointsValue = row / pointsScale;
                chart.append(String.format("| %5.1f", pointsValue));
            } else {
                chart.append("|");
            }

            chart.append("\n");
        }

        // X-axis
        chart.append("    +");
        for (int i = 0; i < sortedPeriods.size(); i++) {
            chart.append("----");
        }
        chart.append("+\n     ");

        // X-axis labels
        for (String sortedPeriod : sortedPeriods) {
            // Extract the last part of the sortedPeriod key (e.g., "01" from "2023-01")
            String label = sortedPeriod.substring(sortedPeriod.length() - 2);
            chart.append(label).append("  ");
        }

        chart.append("\n\n");

        // Add note if no bugs were found
        if (bugCounts.stream().mapToInt(Integer::intValue).sum() == 0) {
            chart.append("Note: No bugs were found in this time period.\n");
        }

        chart.append("Legend: █ = Bug Count   ▓ = Story Points\n");
        chart.append("Left axis: Bug Count   Right axis: Story Points\n");
        chart.append("```\n\n");

        return chart.toString();
    }

    /**
     * Calculates total story points for all tickets in a type map
     */
    private double calculateTotalStoryPoints(Map<String, List<ITicket>> typeMap) {
        double totalPoints = 0;
        for (List<ITicket> tickets : typeMap.values()) {
            totalPoints += calculateStoryPointsForType(tickets);
        }
        return totalPoints;
    }

    /**
     * Calculates story points for a list of tickets
     */
    private double calculateStoryPointsForType(List<ITicket> tickets) {
        double points = 0;
        for (ITicket ticket : tickets) {
            points += ticket.getWeight();
        }
        return points;
    }

    /**
     * Groups tickets by time period and issue type
     */
    private Map<String, Map<String, List<ITicket>>> groupTicketsByPeriodAndType(List<ITicket> tickets, TimelinePeriod period) {
        Map<String, Map<String, List<ITicket>>> result = new TreeMap<>();

        for (ITicket ticket : tickets) {
            try {
                // Get the closed date
                String closedDateStr = ticket.getFieldsAsJSON().getString("dateClosed");
                Date closedDate = parseDate(closedDateStr);

                if (closedDate == null) {
                    System.err.println("Could not parse date: " + closedDateStr);
                    continue;
                }

                // Format the period key
                String periodKey = formatDateToPeriod(closedDate, period);

                // Get the issue type
                String issueType = TicketStatisticsCalculator.nullToEmpty(ticket.getIssueType(), "Task");

                // Add to the appropriate maps
                Map<String, List<ITicket>> typeMap = result.computeIfAbsent(periodKey, k -> new HashMap<>());
                List<ITicket> typeList = typeMap.computeIfAbsent(issueType, k -> new ArrayList<>());
                typeList.add(ticket);

            } catch (Exception e) {
                System.err.println("Error processing ticket for timeline: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Attempts to parse a date string using multiple common formats
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        // List of date formats to try
        List<String> dateFormats = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        );

        for (String format : dateFormats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                return dateFormat.parse(dateStr);
            } catch (ParseException e) {
                // Try next format
            }
        }

        return null; // Could not parse with any format
    }

    /**
     * Formats a date to the appropriate period string
     */
    private String formatDateToPeriod(Date date, TimelinePeriod period) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);

        switch (period) {
            case WEEK:
                // Format as YYYY-Www (e.g., 2023-W01)
                int week = cal.get(Calendar.WEEK_OF_YEAR);
                return String.format("%d-W%02d", year, week);

            case TWO_WEEKS:
                // Group into two-week periods (e.g., 2023-P01 for weeks 1-2)
                int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
                int twoWeekPeriod = (weekOfYear + 1) / 2;
                return String.format("%d-P%02d", year, twoWeekPeriod);

            case MONTH:
                // Format as YYYY-MM
                int month = cal.get(Calendar.MONTH) + 1; // Calendar months are 0-based
                return String.format("%d-%02d", year, month);

            case QUARTER:
                // Calculate quarter (1-4) and format as YYYY-Q# (e.g., 2023-Q1)
                int month0Based = cal.get(Calendar.MONTH);
                int quarter = (month0Based / 3) + 1;
                return String.format("%d-Q%d", year, quarter);

            default:
                // Default to YYYY-MM-DD
                int monthDefault = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);
                return String.format("%d-%02d-%02d", year, monthDefault, day);
        }
    }
}