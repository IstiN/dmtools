package com.github.istin.dmtools.report.projectstatus;

import com.github.istin.dmtools.atlassian.jira.utils.ChangelogAssessment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.report.model.KeyTime;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

@AllArgsConstructor
public class ProjectDataManager {

    private final TrackerClient trackerClient;
    private final String[] completedStatuses;
    private final String[] rolePrefixes;
    private final List<String> priorityOrder;
    private final List<String> issueTypeOrder;
    private final Map<String, String> roleDescriptions;

    public List<ITicket> fetchCompletedTickets(String jql, Calendar startDate) throws Exception {

        List<ITicket> collectedWorkForPeriod = new ArrayList<>();

        // Search for tickets and collect those completed in the specified period
        trackerClient.searchAndPerform(ticket -> {
            List<KeyTime> datesWhenTicketWasInStatus = ChangelogAssessment.findDatesWhenTicketWasInStatus(
                    trackerClient, ticket.getKey(), ticket, completedStatuses);

            if (!datesWhenTicketWasInStatus.isEmpty()) {
                KeyTime first = datesWhenTicketWasInStatus.getFirst();
                Calendar when = first.getWhen();

                // Check if it's after start date
                if (when.after(startDate)) {
                    JSONObject fieldsAsJSON = ticket.getFieldsAsJSON();
                    fieldsAsJSON.put("dateClosed", DateUtils.formatToJiraDate(when));
                    collectedWorkForPeriod.add(ticket);
                }
            }
            return false;
        }, jql, trackerClient.getExtendedQueryFields());

        // Sort tickets
        sortTickets(collectedWorkForPeriod);

        return collectedWorkForPeriod;
    }

    /**
     * Sorts tickets by issue type, priority, and title
     */
    private void sortTickets(List<ITicket> tickets) {
        tickets.sort(
                Comparator.comparing((ITicket t) -> {
                            try {
                                return nullToEmpty(t.getIssueType(), "Task");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .thenComparing((ITicket t) -> {
                            try {
                                return nullToEmpty(t.getPriority(), "Trivial");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .thenComparing((ITicket t) -> {
                            try {
                                return nullToEmpty(t.getTicketTitle(), "");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
    }

    /**
     * Splits tickets into bugs and non-bugs
     */
    public Map<Boolean, List<ITicket>> splitTicketsByType(List<ITicket> tickets) {
        Map<Boolean, List<ITicket>> result = new HashMap<>();
        List<ITicket> bugs = new ArrayList<>();
        List<ITicket> storiesTasks = new ArrayList<>();

        for (ITicket ticket : tickets) {
            try {
                if ("Bug".equalsIgnoreCase(ticket.getIssueType())) {
                    bugs.add(ticket);
                } else {
                    storiesTasks.add(ticket);
                }
            } catch (IOException e) {
                System.err.println("Error processing ticket type: " + e.getMessage());
            }
        }

        result.put(true, bugs);
        result.put(false, storiesTasks);
        return result;
    }

    /**
     * Helper method to handle null values in sorting
     */
    private static String nullToEmpty(String value, String defaultValue) {
        return value == null || value.equalsIgnoreCase("null") ? defaultValue : value;
    }

    /**
     * Removes URLs from text
     */
    public static String removeUrls(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Regex pattern to match URLs
        String urlPattern = "\\b(https?|ftp)://\\S+|\\bwww\\.\\S+";
        return input.replaceAll(urlPattern, "");
    }

    /**
     * Generates a summary table showing counts of tickets by issue type and priority.
     */
    public String generateSummaryTable(List<ITicket> tickets) {
        // Get statistics by type and priority
        Map<String, Map<String, Integer>> typePriorityStats = collectTicketStatistics(tickets);

        // Extract all unique priorities and types
        Set<String> allPriorities = new HashSet<>();
        Map<String, Integer> typeTotals = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> typeEntry : typePriorityStats.entrySet()) {
            String type = typeEntry.getKey();
            Map<String, Integer> priorityCounts = typeEntry.getValue();

            // Calculate total for this type
            int typeTotal = priorityCounts.values().stream().mapToInt(Integer::intValue).sum();
            typeTotals.put(type, typeTotal);

            // Collect all priorities
            allPriorities.addAll(priorityCounts.keySet());
        }

        // Sort priorities and issue types
        List<String> sortedPriorities = sortPriorities(allPriorities);
        List<String> sortedTypes = sortIssueTypes(typeTotals.keySet());

        // Build summary table
        StringBuilder summaryTable = new StringBuilder();
        summaryTable.append("\n\n## Summary by Issue Type and Priority\n\n");
        summaryTable.append("| Issue Type | ");
        for (String priority : sortedPriorities) {
            summaryTable.append(priority).append(" | ");
        }
        summaryTable.append("Total |\n| --- |");
        for (int i = 0; i < sortedPriorities.size(); i++) {
            summaryTable.append(" --- |");
        }
        summaryTable.append(" --- |\n");

        // Add data rows
        for (String type : sortedTypes) {
            summaryTable.append("| ").append(type).append(" | ");
            Map<String, Integer> priorityCounts = typePriorityStats.getOrDefault(type, Collections.emptyMap());

            for (String priority : sortedPriorities) {
                int count = priorityCounts.getOrDefault(priority, 0);
                summaryTable.append(count).append(" | ");
            }

            summaryTable.append(typeTotals.get(type)).append(" |\n");
        }

        // Add totals row
        summaryTable.append("| **Total** | ");
        int grandTotal = 0;
        for (String priority : sortedPriorities) {
            int priorityTotal = 0;
            for (String type : sortedTypes) {
                Map<String, Integer> priorityCounts = typePriorityStats.getOrDefault(type, Collections.emptyMap());
                priorityTotal += priorityCounts.getOrDefault(priority, 0);
            }
            summaryTable.append(priorityTotal).append(" | ");
            grandTotal += priorityTotal;
        }
        summaryTable.append(grandTotal).append(" |\n");

        return summaryTable.toString();
    }

    /**
     * Sorts issue types according to predefined order
     */
    private List<String> sortIssueTypes(Set<String> types) {
        List<String> sortedTypes = new ArrayList<>(types);
        sortedTypes.sort((a, b) -> {
            int aIndex = issueTypeOrder.indexOf(a);
            int bIndex = issueTypeOrder.indexOf(b);

            if (aIndex >= 0 && bIndex >= 0) {
                return Integer.compare(aIndex, bIndex);
            } else if (aIndex >= 0) {
                return -1;
            } else if (bIndex >= 0) {
                return 1;
            } else {
                return a.compareTo(b);
            }
        });

        return sortedTypes;
    }

    /**
     * Collects statistics about tickets grouped by issue type and priority.
     */
    public Map<String, Map<String, Integer>> collectTicketStatistics(List<ITicket> tickets) {
        Map<String, Map<String, Integer>> typePriorityCount = new HashMap<>();

        // Count tickets by type and priority
        for (ITicket ticket : tickets) {
            try {
                String type = nullToEmpty(ticket.getIssueType(), "Task");
                String priority = nullToEmpty(ticket.getPriority(), "Trivial");

                // Add to type-priority map
                typePriorityCount.computeIfAbsent(type, k -> new HashMap<>())
                        .merge(priority, 1, Integer::sum);
            } catch (IOException e) {
                System.err.println("Error processing ticket: " + e.getMessage());
            }
        }

        return typePriorityCount;
    }

    /**
     * Generates a table showing story points distribution by issue type.
     */
    public String generateStoryPointsDistributionTable(List<ITicket> tickets) {
        // Calculate story points by issue type
        Map<String, Double> storyPointsByType = new HashMap<>();
        Map<String, Integer> ticketCountByType = new HashMap<>();
        double totalStoryPoints = 0;
        int totalTickets = 0;

        for (ITicket ticket : tickets) {
            try {
                String type = nullToEmpty(ticket.getIssueType(), "Task");
                double storyPoints = ticket.getWeight();

                storyPointsByType.merge(type, storyPoints, Double::sum);
                ticketCountByType.merge(type, 1, Integer::sum);

                totalStoryPoints += storyPoints;
                totalTickets++;
            } catch (IOException e) {
                System.err.println("Error processing ticket for story points: " + e.getMessage());
            }
        }

        // Sort issue types
        List<String> sortedTypes = sortIssueTypes(storyPointsByType.keySet());

        // Build story points distribution table
        StringBuilder storyPointsTable = new StringBuilder();
        storyPointsTable.append("\n\n## Story Points Distribution by Issue Type\n\n");
        storyPointsTable.append("| Issue Type | Ticket Count | Story Points | % of Total Points | Avg. Points per Ticket |\n");
        storyPointsTable.append("|------------|--------------|--------------|-------------------|------------------------|\n");

        // Add data rows
        for (String type : sortedTypes) {
            double points = storyPointsByType.get(type);
            int count = ticketCountByType.get(type);
            double percentage = calculatePercentage(points, totalStoryPoints);
            double avgPointsPerTicket = (count > 0) ? points / count : 0;

            storyPointsTable.append("| ")
                    .append(type).append(" | ")
                    .append(count).append(" | ")
                    .append(String.format("%.1f", points)).append(" | ")
                    .append(String.format("%.1f%%", percentage)).append(" | ")
                    .append(String.format("%.2f", avgPointsPerTicket)).append(" |\n");
        }

        // Add totals row
        double avgPointsOverall = (totalTickets > 0) ? totalStoryPoints / totalTickets : 0;
        storyPointsTable.append("| **Total** | ")
                .append(totalTickets).append(" | ")
                .append(String.format("%.1f", totalStoryPoints)).append(" | ")
                .append("100.0%").append(" | ")
                .append(String.format("%.2f", avgPointsOverall)).append(" |\n");

        return storyPointsTable.toString();
    }

    /**
     * Generates a story points summary table for a specific role
     */
    private String generateRoleStoryPointsSummary(List<ITicket> roleTickets) {
        // Calculate story points by issue type
        Map<String, Double> storyPointsByType = new HashMap<>();
        double totalPoints = 0;

        for (ITicket ticket : roleTickets) {
            try {
                String type = nullToEmpty(ticket.getIssueType(), "Task");
                double points = ticket.getWeight();

                storyPointsByType.merge(type, points, Double::sum);
                totalPoints += points;
            } catch (IOException e) {
                System.err.println("Error processing story points for role table: " + e.getMessage());
            }
        }

        // Build story points summary table
        StringBuilder summary = new StringBuilder();
        summary.append("#### Story Points by Issue Type\n\n");
        summary.append("| Issue Type | Story Points | % of Role Total |\n");
        summary.append("|------------|--------------|----------------|\n");

        List<String> types = new ArrayList<>(storyPointsByType.keySet());
        Collections.sort(types);

        for (String type : types) {
            double points = storyPointsByType.get(type);
            double percentage = calculatePercentage(points, totalPoints);

            summary.append("| ")
                    .append(type).append(" | ")
                    .append(String.format("%.1f", points)).append(" | ")
                    .append(String.format("%.1f%%", percentage)).append(" |\n");
        }

        summary.append("| **Total** | ")
                .append(String.format("%.1f", totalPoints)).append(" | ")
                .append("100.0%").append(" |\n\n");

        return summary.toString();
    }

    /**
     * Calculates percentage for double values
     */
    private double calculatePercentage(double part, double total) {
        return (total > 0) ? (part / total) * 100 : 0;
    }

    /**
     * Generates a table of tickets for a specific role
     */
    private static String generateRoleTicketsTable(List<ITicket> roleTickets) {
        StringBuilder table = new StringBuilder();
        table.append("#### Tickets\n\n");
        table.append("| Key | Type | Priority | Story Points | Closed Date | Summary |\n");
        table.append("|-----|------|----------|-------------|------------|--------|\n");

        for (ITicket ticket : roleTickets) {
            try {
                String summary = cleanTextForMarkdown(ticket.getTicketTitle());

                table.append("| ")
                        .append(ticket.getKey()).append(" | ")
                        .append(ticket.getIssueType()).append(" | ")
                        .append(nullToEmpty(ticket.getPriority(), "Trivial")).append(" | ")
                        .append(ticket.getWeight()).append(" | ")
                        .append(ticket.getFieldsAsJSON().getString("dateClosed")).append(" | ")
                        .append(summary).append(" |\n");
            } catch (IOException e) {
                System.err.println("Error adding ticket to role table: " + e.getMessage());
            }
        }

        return table.toString();
    }

    /**
     * Generates a table showing the distribution of work by role category.
     */
    public String generateRoleBasedDistributionTable(List<ITicket> tickets) {
        // Maps to store data by role
        Map<String, Integer> ticketCountByRole = new HashMap<>();
        Map<String, Double> storyPointsByRole = new HashMap<>();
        Map<String, List<String>> ticketKeysByRole = new HashMap<>();

        double totalStoryPoints = 0;
        int totalTickets = 0;

        // Process each ticket
        for (ITicket ticket : tickets) {
            try {
                String summary = ticket.getTicketTitle();
                String role = determineTicketRole(summary);
                double storyPoints = ticket.getWeight();

                // Update counts and points
                ticketCountByRole.merge(role, 1, Integer::sum);
                storyPointsByRole.merge(role, storyPoints, Double::sum);

                // Store ticket key for reference
                ticketKeysByRole.computeIfAbsent(role, k -> new ArrayList<>()).add(ticket.getKey());

                totalStoryPoints += storyPoints;
                totalTickets++;
            } catch (IOException e) {
                System.err.println("Error processing ticket for role-based analysis: " + e.getMessage());
            }
        }

        // Sort roles
        List<String> sortedRoles = sortRoles(ticketCountByRole.keySet());

        // Build role-based distribution table
        StringBuilder roleTable = new StringBuilder();
        roleTable.append("\n\n## Work Distribution by Role Category\n\n");
        roleTable.append("| Role | Ticket Count | % of Tickets | Story Points | % of Points | Avg. Points per Ticket |\n");
        roleTable.append("|------|--------------|--------------|--------------|-------------|------------------------|\n");

        // Add data rows
        for (String role : sortedRoles) {
            int count = ticketCountByRole.get(role);
            double points = storyPointsByRole.getOrDefault(role, 0.0);
            double ticketPercentage = calculatePercentage(count, totalTickets);
            double pointsPercentage = calculatePercentage(points, totalStoryPoints);
            double avgPointsPerTicket = (count > 0) ? points / count : 0;
            String roleDescription = roleDescriptions.getOrDefault(role, role);

            roleTable.append("| ")
                    .append(roleDescription).append(" | ")
                    .append(count).append(" | ")
                    .append(String.format("%.1f%%", ticketPercentage)).append(" | ")
                    .append(String.format("%.1f", points)).append(" | ")
                    .append(String.format("%.1f%%", pointsPercentage)).append(" | ")
                    .append(String.format("%.2f", avgPointsPerTicket)).append(" |\n");
        }

        // Add totals row
        double avgPointsOverall = (totalTickets > 0) ? totalStoryPoints / totalTickets : 0;
        roleTable.append("| **Total** | ")
                .append(totalTickets).append(" | ")
                .append("100.0%").append(" | ")
                .append(String.format("%.1f", totalStoryPoints)).append(" | ")
                .append("100.0%").append(" | ")
                .append(String.format("%.2f", avgPointsOverall)).append(" |\n");

        // Add ticket references by role
        roleTable.append("\n### Tickets by Role Category\n\n");
        for (String role : sortedRoles) {
            String roleDescription = roleDescriptions.getOrDefault(role, role);
            List<String> ticketKeys = ticketKeysByRole.get(role);

            roleTable.append("**").append(roleDescription).append("**: ");
            roleTable.append(String.join(", ", ticketKeys));
            roleTable.append("\n\n");
        }

        return roleTable.toString();
    }


    /**
     * Generates tables for each role showing their specific tasks and story points
     */
    public String generateRoleSpecificTables(List<ITicket> tickets) {
        // Categorize tickets by role
        Map<String, List<ITicket>> ticketsByRole = categorizeTicketsByRole(tickets);

        // Sort roles for consistent display
        List<String> sortedRoles = sortRoles(ticketsByRole.keySet());

        StringBuilder roleSpecificTables = new StringBuilder();
        roleSpecificTables.append("## Role-Specific Work Details\n\n");

        // Generate table for each role
        for (String role : sortedRoles) {
            List<ITicket> roleTickets = ticketsByRole.get(role);
            String roleDescription = roleDescriptions.getOrDefault(role, role);

            roleSpecificTables.append("### ").append(roleDescription).append(" Work\n\n");

            // Generate story points summary for this role
            roleSpecificTables.append(generateRoleStoryPointsSummary(roleTickets));

            // Generate tickets table for this role
            roleSpecificTables.append(generateRoleTicketsTable(roleTickets));

            roleSpecificTables.append("\n\n");
        }

        return roleSpecificTables.toString();
    }

    /**
     * Categorizes tickets by role based on summary tags
     */
    private Map<String, List<ITicket>> categorizeTicketsByRole(List<ITicket> tickets) {
        Map<String, List<ITicket>> ticketsByRole = new HashMap<>();

        for (ITicket ticket : tickets) {
            try {
                String summary = ticket.getTicketTitle();
                String role = determineTicketRole(summary);

                // Add ticket to appropriate role list
                ticketsByRole.computeIfAbsent(role, k -> new ArrayList<>()).add(ticket);
            } catch (IOException e) {
                System.err.println("Error categorizing ticket by role: " + e.getMessage());
            }
        }

        return ticketsByRole;
    }

    /**
     * Determines the role of a ticket based on its summary
     */
    private String determineTicketRole(String summary) {
        if (summary == null) {
            return "Development";
        }

        for (String tag : rolePrefixes) {
            if (summary.contains(tag)) {
                return tag;
            }
        }

        return "Development"; // Default role if no tag is found
    }

    /**
     * Sorts roles with Development last, others alphabetically
     */
    private static List<String> sortRoles(Set<String> roles) {
        List<String> sortedRoles = new ArrayList<>(roles);
        sortedRoles.sort((a, b) -> {
            // Development should be last
            if (a.equals("Development") && !b.equals("Development")) {
                return 1;
            }
            if (!a.equals("Development") && b.equals("Development")) {
                return -1;
            }
            // Sort other roles alphabetically
            return a.compareTo(b);
        });

        return sortedRoles;
    }


    /**
     * Sorts priorities according to predefined order
     */
    private List<String> sortPriorities(Set<String> priorities) {
        List<String> sortedPriorities = new ArrayList<>(priorities);
        sortedPriorities.sort((a, b) -> {
            int aIndex = priorityOrder.indexOf(a);
            int bIndex = priorityOrder.indexOf(b);

            if (aIndex >= 0 && bIndex >= 0) {
                return Integer.compare(aIndex, bIndex);
            } else if (aIndex >= 0) {
                return -1;
            } else if (bIndex >= 0) {
                return 1;
            } else {
                return a.compareTo(b);
            }
        });

        return sortedPriorities;
    }

    /**
     * Calculates percentage
     */
    private static double calculatePercentage(int part, int total) {
        return (total > 0) ? ((double) part / total) * 100 : 0;
    }

    /**
     * Generates a table showing bug distribution by priority
     */
    public String generateBugOverviewTable(List<ITicket> bugs) {
        // Count bugs by priority
        Map<String, Integer> bugsByPriority = countTicketsByPriority(bugs);

        // Sort priorities by predefined order
        List<String> sortedPriorities = sortPriorities(bugsByPriority.keySet());

        // Build bug overview table
        StringBuilder bugTable = new StringBuilder();
        bugTable.append("## Bug Overview by Priority\n\n");
        bugTable.append("| Priority | Count | Percentage |\n");
        bugTable.append("|----------|-------|------------|\n");

        int totalBugs = bugs.size();
        for (String priority : sortedPriorities) {
            int count = bugsByPriority.get(priority);
            double percentage = calculatePercentage(count, totalBugs);

            bugTable.append("| ")
                    .append(priority).append(" | ")
                    .append(count).append(" | ")
                    .append(String.format("%.1f%%", percentage)).append(" |\n");
        }

        bugTable.append("| **Total** | ")
                .append(totalBugs).append(" | ")
                .append("100.0%").append(" |\n\n");

        return bugTable.toString();
    }

    /**
     * Counts tickets by priority
     */
    private Map<String, Integer> countTicketsByPriority(List<ITicket> tickets) {
        Map<String, Integer> countByPriority = new HashMap<>();

        for (ITicket ticket : tickets) {
            try {
                String priority = nullToEmpty(ticket.getPriority(), "Trivial");
                countByPriority.merge(priority, 1, Integer::sum);
            } catch (IOException e) {
                System.err.println("Error processing ticket priority: " + e.getMessage());
            }
        }

        return countByPriority;
    }

    /**
     * Helper method to append a ticket to a markdown table
     */
    private void appendTicketToTable(ITicket ticket, StringBuilder builder) throws IOException {
        String description = cleanTextForMarkdown(ticket.getTicketDescription());
        String summary = cleanTextForMarkdown(ticket.getTicketTitle());

        // Add row to table
        builder.append("| ")
                .append(ticket.getKey()).append(" | ")
                .append(ticket.getIssueType()).append(" | ")
                .append(nullToEmpty(ticket.getPriority(), "Trivial")).append(" | ")
                .append(ticket.getWeight()).append(" | ")
                .append(ticket.getFieldsAsJSON().getString("dateClosed")).append(" | ")
                .append(summary).append(" | ")
                .append(removeUrls(description)).append(" |\n");
    }

    /**
     * Cleans and formats text for markdown tables
     */
    private static String cleanTextForMarkdown(String text) {
        if (text == null) {
            return "";
        }

        return text.replaceAll("[\\r\\n]", " ")
                .replaceAll("\\|", "\\\\|")  // Escape pipe characters
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Generates a table of bug fixes
     */
    public String generateBugsTable(List<ITicket> bugs) throws IOException {
        StringBuilder table = new StringBuilder();

        table.append("## Bug Fixes\n\n");
        table.append("| Key | Priority | Closed Date | Summary | Description |\n")
                .append("|-----|----------|-------------|---------|-------------|\n");

        for (ITicket ticket : bugs) {
            // Clean and format text fields
            String description = cleanTextForMarkdown(ticket.getTicketDescription());
            String summary = cleanTextForMarkdown(ticket.getTicketTitle());

            // Add bug row to table (without story points column)
            table.append("| ")
                    .append(ticket.getKey()).append(" | ")
                    .append(nullToEmpty(ticket.getPriority(), "Trivial")).append(" | ")
                    .append(ticket.getFieldsAsJSON().getString("dateClosed")).append(" | ")
                    .append(summary).append(" | ")
                    .append(removeUrls(description)).append(" |\n");
        }
        table.append("\n\n");

        return table.toString();
    }

    /**
     * Generates a table of non-bug work items
     */
    public String generateTasksAndStoriesTable(List<ITicket> tasksAndStories) throws IOException {
        StringBuilder table = new StringBuilder();

        table.append("## Tasks And Stories Work Items\n\n");
        table.append("| Key | Type | Priority | Story Points | Closed Date | Summary | Description |\n")
                .append("|-----|------|----------|-------------|---------|---------|-------------|\n");

        for (ITicket ticket : tasksAndStories) {
            appendTicketToTable(ticket, table);
        }
        table.append("\n\n");

        return table.toString();
    }


}
