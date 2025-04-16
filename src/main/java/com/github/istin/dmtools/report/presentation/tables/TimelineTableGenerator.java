package com.github.istin.dmtools.report.presentation.tables;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.report.data.TicketStatisticsCalculator;
import com.github.istin.dmtools.report.model.TableData;
import com.github.istin.dmtools.report.model.TimelinePeriod;
import com.github.istin.dmtools.report.presentation.TableGenerator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

        StringBuilder result = new StringBuilder();

        // Generate the main timeline table
        result.append(generateDeliverableCountTable(ticketsByPeriodAndType, sortedPeriods, period));

        // Generate the story points timeline table
        result.append("\n");
        result.append(generateStoryPointsTable(ticketsByPeriodAndType, sortedPeriods, period));

        // Generate the averages summary
        result.append("\n");
        result.append(generateAveragesSummary(ticketsByPeriodAndType, sortedPeriods, period));

        // Generate the dual-column chart
        result.append("\n");
        result.append(generateDualColumnChart(ticketsByPeriodAndType, sortedPeriods, period));

        return result.toString();
    }

    /**
     * Generates a table showing the count of deliverables by type over time
     */
    private String generateDeliverableCountTable(Map<String, Map<String, List<ITicket>>> ticketsByPeriodAndType,
                                                 List<String> sortedPeriods, TimelinePeriod period) {
        // Create table headers
        List<String> headers = Arrays.asList(
                "Time Period", "Bugs", "Stories", "Tasks", "Total", "Total Story Points"
        );

        TableData tableData = new TableData(
                "Deliverables Timeline (" + period.getDescription() + ")",
                headers,
                "Shows the number of completed items by type over time periods."
        );

        // Add data rows
        int totalBugs = 0;
        int totalStories = 0;
        int totalTasks = 0;
        int grandTotal = 0;
        double totalStoryPoints = 0;

        for (String periodKey : sortedPeriods) {
            Map<String, List<ITicket>> typeMap = ticketsByPeriodAndType.get(periodKey);

            int bugCount = typeMap.getOrDefault("Bug", Collections.emptyList()).size();
            int storyCount = typeMap.getOrDefault("Story", Collections.emptyList()).size();
            int taskCount = typeMap.getOrDefault("Task", Collections.emptyList()).size();
            int periodTotal = bugCount + storyCount + taskCount;

            // Calculate story points for this period
            double periodStoryPoints = calculateTotalStoryPoints(typeMap);

            List<String> row = Arrays.asList(
                    periodKey,
                    String.valueOf(bugCount),
                    String.valueOf(storyCount),
                    String.valueOf(taskCount),
                    String.valueOf(periodTotal),
                    String.format("%.1f", periodStoryPoints)
            );
            tableData.addRow(row);

            // Update totals
            totalBugs += bugCount;
            totalStories += storyCount;
            totalTasks += taskCount;
            grandTotal += periodTotal;
            totalStoryPoints += periodStoryPoints;
        }

        // Add totals row
        List<String> totalsRow = Arrays.asList(
                "**Total**",
                String.valueOf(totalBugs),
                String.valueOf(totalStories),
                String.valueOf(totalTasks),
                String.valueOf(grandTotal),
                String.format("%.1f", totalStoryPoints)
        );
        tableData.addRow(totalsRow);

        return baseTableGenerator.generateTable(tableData);
    }

    /**
     * Generates a table showing story points by issue type over time
     */
    private String generateStoryPointsTable(Map<String, Map<String, List<ITicket>>> ticketsByPeriodAndType,
                                            List<String> sortedPeriods, TimelinePeriod period) {
        // Create table headers
        List<String> headers = Arrays.asList(
                "Time Period", "Bug Points", "Story Points", "Task Points", "Total Points", "Avg Points per Item"
        );

        TableData tableData = new TableData(
                "Story Points Timeline (" + period.getDescription() + ")",
                headers,
                "Shows the distribution of story points by issue type over time periods."
        );

        // Add data rows
        double totalBugPoints = 0;
        double totalStoryPoints = 0;
        double totalTaskPoints = 0;
        double grandTotalPoints = 0;
        int grandTotalItems = 0;

        for (String periodKey : sortedPeriods) {
            Map<String, List<ITicket>> typeMap = ticketsByPeriodAndType.get(periodKey);

            double bugPoints = calculateStoryPointsForType(typeMap.getOrDefault("Bug", Collections.emptyList()));
            double storyPoints = calculateStoryPointsForType(typeMap.getOrDefault("Story", Collections.emptyList()));
            double taskPoints = calculateStoryPointsForType(typeMap.getOrDefault("Task", Collections.emptyList()));
            double periodTotalPoints = bugPoints + storyPoints + taskPoints;

            int periodTotalItems = typeMap.values().stream().mapToInt(List::size).sum();
            double avgPointsPerItem = periodTotalItems > 0 ? periodTotalPoints / periodTotalItems : 0;

            List<String> row = Arrays.asList(
                    periodKey,
                    String.format("%.1f", bugPoints),
                    String.format("%.1f", storyPoints),
                    String.format("%.1f", taskPoints),
                    String.format("%.1f", periodTotalPoints),
                    String.format("%.2f", avgPointsPerItem)
            );
            tableData.addRow(row);

            // Update totals
            totalBugPoints += bugPoints;
            totalStoryPoints += storyPoints;
            totalTaskPoints += taskPoints;
            grandTotalPoints += periodTotalPoints;
            grandTotalItems += periodTotalItems;
        }

        // Add totals row
        double overallAvgPoints = grandTotalItems > 0 ? grandTotalPoints / grandTotalItems : 0;
        List<String> totalsRow = Arrays.asList(
                "**Total**",
                String.format("%.1f", totalBugPoints),
                String.format("%.1f", totalStoryPoints),
                String.format("%.1f", totalTaskPoints),
                String.format("%.1f", grandTotalPoints),
                String.format("%.2f", overallAvgPoints)
        );
        tableData.addRow(totalsRow);

        return baseTableGenerator.generateTable(tableData);
    }

    /**
     * Generates a summary of average metrics across time periods
     */
    private String generateAveragesSummary(Map<String, Map<String, List<ITicket>>> ticketsByPeriodAndType,
                                           List<String> sortedPeriods, TimelinePeriod period) {
        StringBuilder summary = new StringBuilder();
        summary.append("## Average Metrics per ").append(period.getDescription()).append(" Period\n\n");

        // Calculate totals and averages
        int totalBugs = 0;
        int totalStories = 0;
        int totalTasks = 0;
        int totalFeatureWork = 0; // Stories + Tasks
        double totalStoryPoints = 0;
        double totalBugPoints = 0;
        double totalFeaturePoints = 0; // Story + Task points

        for (String periodKey : sortedPeriods) {
            Map<String, List<ITicket>> typeMap = ticketsByPeriodAndType.get(periodKey);

            int bugCount = typeMap.getOrDefault("Bug", Collections.emptyList()).size();
            int storyCount = typeMap.getOrDefault("Story", Collections.emptyList()).size();
            int taskCount = typeMap.getOrDefault("Task", Collections.emptyList()).size();

            totalBugs += bugCount;
            totalStories += storyCount;
            totalTasks += taskCount;
            totalFeatureWork += (storyCount + taskCount);

            double bugPoints = calculateStoryPointsForType(typeMap.getOrDefault("Bug", Collections.emptyList()));
            double storyPoints = calculateStoryPointsForType(typeMap.getOrDefault("Story", Collections.emptyList()));
            double taskPoints = calculateStoryPointsForType(typeMap.getOrDefault("Task", Collections.emptyList()));

            totalBugPoints += bugPoints;
            totalFeaturePoints += (storyPoints + taskPoints);
            totalStoryPoints += (bugPoints + storyPoints + taskPoints);
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
            double bugPercentage = totalBugs * 100.0 / (totalBugs + totalFeatureWork);
            List<String> bugRow = Arrays.asList(
                    "Bugs Fixed",
                    String.valueOf(totalBugs),
                    String.format("%.1f", avgBugsPerPeriod),
                    String.format("%.1f%%", bugPercentage)
            );
            tableData.addRow(bugRow);

            // Feature work metrics (Stories + Tasks)
            double avgFeatureWorkPerPeriod = (double) totalFeatureWork / periodCount;
            double featureWorkPercentage = totalFeatureWork * 100.0 / (totalBugs + totalFeatureWork);
            List<String> featureWorkRow = Arrays.asList(
                    "Feature Work (Stories + Tasks)",
                    String.valueOf(totalFeatureWork),
                    String.format("%.1f", avgFeatureWorkPerPeriod),
                    String.format("%.1f%%", featureWorkPercentage)
            );
            tableData.addRow(featureWorkRow);

            // Story metrics
            double avgStoriesPerPeriod = (double) totalStories / periodCount;
            double storyPercentage = totalStories * 100.0 / (totalBugs + totalFeatureWork);
            List<String> storyRow = Arrays.asList(
                    "Stories Completed",
                    String.valueOf(totalStories),
                    String.format("%.1f", avgStoriesPerPeriod),
                    String.format("%.1f%%", storyPercentage)
            );
            tableData.addRow(storyRow);

            // Task metrics
            double avgTasksPerPeriod = (double) totalTasks / periodCount;
            double taskPercentage = totalTasks * 100.0 / (totalBugs + totalFeatureWork);
            List<String> taskRow = Arrays.asList(
                    "Tasks Completed",
                    String.valueOf(totalTasks),
                    String.format("%.1f", avgTasksPerPeriod),
                    String.format("%.1f%%", taskPercentage)
            );
            tableData.addRow(taskRow);

            // Total items
            int totalItems = totalBugs + totalFeatureWork;
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
            double bugToFeatureRatio = totalBugs > 0 && totalFeatureWork > 0 ?
                    (double) totalBugs / totalFeatureWork : 0;
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
            double avgPointsPerFeature = totalFeatureWork > 0 ? totalFeaturePoints / totalFeatureWork : 0;
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
                                           List<String> sortedPeriods, TimelinePeriod period) {
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

            int bugCount = typeMap.getOrDefault("Bug", Collections.emptyList()).size();
            double storyPoints = calculateTotalStoryPoints(typeMap);

            bugCounts.add(bugCount);
            storyPointsValues.add(storyPoints);

            maxBugCount = Math.max(maxBugCount, bugCount);
            maxStoryPoints = Math.max(maxStoryPoints, storyPoints);
        }

        // Chart settings
        int chartHeight = 15;

        // Y-axis scale for bugs (left side)
        double bugScale = maxBugCount > 0 ? (double) chartHeight / maxBugCount : 0;

        // Y-axis scale for story points (right side)
        double pointsScale = maxStoryPoints > 0 ? (double) chartHeight / maxStoryPoints : 0;

        // Generate chart rows from top to bottom
        for (int row = chartHeight; row >= 0; row--) {
            // Left Y-axis label (bugs)
            if (row % 3 == 0 || row == chartHeight) {
                int bugValue = (int) (row / bugScale);
                chart.append(String.format("%3d |", bugValue));
            } else {
                chart.append("    |");
            }

            // Chart data
            for (int i = 0; i < sortedPeriods.size(); i++) {
                int bugHeight = (int) (bugCounts.get(i) * bugScale);
                int pointsHeight = (int) (storyPointsValues.get(i) * pointsScale);

                // Bug column (█)
                if (row <= bugHeight) {
                    chart.append("█");
                } else {
                    chart.append(" ");
                }

                // Spacer
                chart.append(" ");

                // Story points column (▓)
                if (row <= pointsHeight) {
                    chart.append("▓");
                } else {
                    chart.append(" ");
                }

                // Column separator
                chart.append(" ");
            }

            // Right Y-axis label (story points)
            if (row % 3 == 0 || row == chartHeight) {
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

    // Existing methods for groupTicketsByPeriodAndType, parseDate, formatDateToPeriod...

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