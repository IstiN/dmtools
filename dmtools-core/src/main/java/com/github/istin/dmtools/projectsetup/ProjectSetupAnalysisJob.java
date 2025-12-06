package com.github.istin.dmtools.projectsetup;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.model.ToText;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.projectsetup.agent.*;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dagger.Component;

public class ProjectSetupAnalysisJob extends AbstractJob<ProjectSetupAnalysisJobParams, JSONObject> {

    @Inject
    TrackerClient<? extends ITicket> trackerClient;

    @Inject
    FinalStatusDetectionAgent finalStatusDetectionAgent;

    @Inject
    ProjectSetupAnalysisAgent projectSetupAnalysisAgent;

    @Inject
    WorkflowAnalysisAgent workflowAnalysisAgent;

    @Inject
    StoryDescriptionWritingRulesAgent storyDescriptionWritingRulesAgent;

    @Inject
    TestCaseWritingRulesAgent testCaseWritingRulesAgent;

    @Getter
    @Inject
    com.github.istin.dmtools.ai.AI ai;

    @Singleton
    @Component(modules = {com.github.istin.dmtools.di.ConfigurationModule.class, 
                          com.github.istin.dmtools.di.JiraModule.class, 
                          com.github.istin.dmtools.di.AIComponentsModule.class, 
                          com.github.istin.dmtools.di.ConfluenceModule.class, 
                          com.github.istin.dmtools.di.AIAgentsModule.class})
    public interface ProjectSetupAnalysisJobComponent {
        void inject(ProjectSetupAnalysisJob projectSetupAnalysisJob);
    }

    @Singleton
    @Component(modules = {ServerManagedIntegrationsModule.class, com.github.istin.dmtools.di.AIAgentsModule.class})
    public interface ServerManagedProjectSetupAnalysisJobComponent {
        void inject(ProjectSetupAnalysisJob projectSetupAnalysisJob);
    }

    public ProjectSetupAnalysisJob() {
    }

    @Override
    protected void initializeStandalone() {
        DaggerProjectSetupAnalysisJob_ProjectSetupAnalysisJobComponent.create().inject(this);
    }

    @Override
    protected void initializeServerManaged(org.json.JSONObject resolvedIntegrations) {
        try {
            ServerManagedIntegrationsModule module = new ServerManagedIntegrationsModule(resolvedIntegrations);
            ServerManagedProjectSetupAnalysisJobComponent component = 
                DaggerProjectSetupAnalysisJob_ServerManagedProjectSetupAnalysisJobComponent.builder()
                    .serverManagedIntegrationsModule(module)
                    .build();
            component.inject(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ProjectSetupAnalysisJob in server-managed mode", e);
        }
    }

    @Override
    protected JSONObject executeJob(ProjectSetupAnalysisJobParams params) throws Exception {
        String projectKey = params.getProjectKey();
        if (projectKey == null || projectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("projectKey is required");
        }

        // Step 1: Get workflow metadata for final status detection
        String workflowMetadata = getWorkflowMetadata(projectKey);
        
        // Step 2: Detect final statuses
        JSONArray finalStatuses = finalStatusDetectionAgent.run(
            new FinalStatusDetectionAgent.Params(projectKey, workflowMetadata)
        );

        // Step 3: Get issue types and fields
        // Cast TrackerClient to JiraClient to access getIssueTypes and getFields methods
        JiraClient<? extends ITicket> jiraClient = (JiraClient<? extends ITicket>) trackerClient;
        List<com.github.istin.dmtools.atlassian.jira.model.IssueType> issueTypes = jiraClient.getIssueTypes(projectKey);
        String fieldsJson = jiraClient.getFields(projectKey);
        
        // Step 4: Analyze project setup
        JSONObject projectSetupResult = projectSetupAnalysisAgent.run(
            new ProjectSetupAnalysisAgent.Params(
                projectKey,
                new JSONArray(issueTypes).toString(),
                fieldsJson
            )
        );

        // Step 5: Get completed tickets (last 50)
        List<? extends ITicket> completedTickets = getCompletedTickets(projectKey, finalStatuses);
        String completedTicketsData = formatTicketsForAnalysis(completedTickets);

        // Step 6: Analyze workflow
        JSONObject workflowAnalysisResult = workflowAnalysisAgent.run(
            new WorkflowAnalysisAgent.Params(
                projectKey,
                finalStatuses,
                completedTicketsData
            )
        );

        // Step 7: Extract story descriptions
        String storyDescriptionsData = extractStoryDescriptions(completedTickets);

        // Step 8: Generate story description writing rules
        JSONObject storyDescriptionRules = storyDescriptionWritingRulesAgent.run(
            new StoryDescriptionWritingRulesAgent.Params(
                projectKey,
                storyDescriptionsData
            )
        );

        // Step 9: Extract test case data
        String testCaseData = extractTestCaseData(completedTickets);

        // Step 10: Generate test case writing rules
        JSONObject testCaseRules = testCaseWritingRulesAgent.run(
            new TestCaseWritingRulesAgent.Params(
                projectKey,
                testCaseData
            )
        );

        // Step 11: Aggregate all results
        JSONObject result = new JSONObject();
        result.put("projectKey", projectKey);
        result.put("projectIssueTypes", projectSetupResult.optJSONArray("issueTypes") != null 
            ? projectSetupResult.getJSONArray("issueTypes") 
            : new JSONArray(issueTypes));
        result.put("projectFields", projectSetupResult.optJSONObject("fields") != null 
            ? projectSetupResult.getJSONObject("fields") 
            : new JSONObject().put("raw", fieldsJson));
        result.put("finalStatuses", finalStatuses);
        result.put("workflowAnalysis", workflowAnalysisResult);
        result.put("storyDescriptionRules", storyDescriptionRules);
        result.put("testCaseRules", testCaseRules);

        return result;
    }

    private String getWorkflowMetadata(String projectKey) throws Exception {
        // Try to get workflow metadata from Jira API
        // For now, return a placeholder - this would need to call Jira workflow API
        try {
            // Attempt to get workflow information
            // This is a simplified approach - in production, you'd call the Jira workflow API
            return "{\"projectKey\":\"" + projectKey + "\",\"note\":\"Workflow metadata retrieval needs Jira workflow API integration\"}";
        } catch (Exception e) {
            return "{\"projectKey\":\"" + projectKey + "\",\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private List<? extends ITicket> getCompletedTickets(String projectKey, JSONArray finalStatuses) throws Exception {
        // Build JQL query for tickets in final statuses
        StringBuilder jql = new StringBuilder("project = ").append(projectKey);
        
        if (finalStatuses != null && finalStatuses.length() > 0) {
            jql.append(" AND status IN (");
            for (int i = 0; i < finalStatuses.length(); i++) {
                if (i > 0) jql.append(", ");
                String status = finalStatuses.getString(i);
                jql.append("\"").append(status).append("\"");
            }
            jql.append(")");
        } else {
            // Fallback to common final statuses if detection failed
            jql.append(" AND status IN (\"Done\", \"Closed\", \"Resolved\", \"Completed\")");
        }
        
        jql.append(" ORDER BY updated DESC");
        
        // Get last 50 tickets
        List<? extends ITicket> allTickets = trackerClient.searchAndPerform(
            jql.toString(), 
            trackerClient.getExtendedQueryFields()
        );
        
        return allTickets.stream()
            .limit(50)
            .collect(Collectors.toList());
    }

    private String formatTicketsForAnalysis(List<? extends ITicket> tickets) {
        List<String> ticketTexts = new ArrayList<>();
        for (ITicket ticket : tickets) {
            try {
                ticketTexts.add(ticket.toText());
            } catch (Exception e) {
                // Fallback to basic ticket info if toText() fails
                try {
                    String title = ticket.getTicketTitle();
                    String description = ticket.getTicketDescription();
                    ticketTexts.add("Issue: " + ticket.getTicketKey() + "\nSummary: " + 
                        (title != null ? title : "") + 
                        "\nDescription: " + (description != null ? description : ""));
                } catch (Exception ex) {
                    // If even fallback fails, just use the key
                    ticketTexts.add("Issue: " + ticket.getTicketKey());
                }
            }
        }
        return String.join("\n\n---\n\n", ticketTexts);
    }

    private String extractStoryDescriptions(List<? extends ITicket> tickets) {
        List<String> descriptions = new ArrayList<>();
        for (ITicket ticket : tickets) {
            try {
                // Include all issue types, not just Story (per DMC-778)
                String description = ticket.getTicketDescription();
                if (description != null && !description.trim().isEmpty()) {
                    descriptions.add("Issue: " + ticket.getTicketKey() + "\nType: " + ticket.getIssueType() + "\nDescription: " + description);
                }
            } catch (Exception e) {
                // Skip tickets that fail to retrieve description
            }
        }
        return String.join("\n\n---\n\n", descriptions);
    }

    private String extractTestCaseData(List<? extends ITicket> tickets) {
        List<String> testCaseData = new ArrayList<>();
        for (ITicket ticket : tickets) {
            try {
                // Include all issue types for test case analysis (per DMC-778)
                String description = ticket.getTicketDescription();
                String summary = ticket.getTicketTitle();
                if ((description != null && !description.trim().isEmpty()) || 
                    (summary != null && !summary.trim().isEmpty())) {
                    testCaseData.add("Issue: " + ticket.getTicketKey() + 
                        "\nType: " + ticket.getIssueType() + 
                        "\nSummary: " + (summary != null ? summary : "") + 
                        "\nDescription: " + (description != null ? description : ""));
                }
            } catch (Exception e) {
                // Skip tickets that fail to retrieve data
            }
        }
        return String.join("\n\n---\n\n", testCaseData);
    }
}
