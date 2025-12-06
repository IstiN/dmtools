package com.github.istin.dmtools.projectsetup;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.projectsetup.agent.*;
import org.junit.Before;
import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProjectSetupAnalysisJobTest {

    private ProjectSetupAnalysisJob job;
    private TrackerClient<? extends ITicket> mockTrackerClient;
    private JiraClient<? extends ITicket> mockJiraClient;
    private FinalStatusDetectionAgent mockFinalStatusDetectionAgent;
    private ProjectSetupAnalysisAgent mockProjectSetupAnalysisAgent;
    private WorkflowAnalysisAgent mockWorkflowAnalysisAgent;
    private StoryDescriptionWritingRulesAgent mockStoryDescriptionWritingRulesAgent;
    private TestCaseWritingRulesAgent mockTestCaseWritingRulesAgent;

    @Before
    public void setUp() throws Exception {
        job = new ProjectSetupAnalysisJob();
        
        mockTrackerClient = mock(TrackerClient.class);
        mockJiraClient = mock(JiraClient.class);
        mockFinalStatusDetectionAgent = mock(FinalStatusDetectionAgent.class);
        mockProjectSetupAnalysisAgent = mock(ProjectSetupAnalysisAgent.class);
        mockWorkflowAnalysisAgent = mock(WorkflowAnalysisAgent.class);
        mockStoryDescriptionWritingRulesAgent = mock(StoryDescriptionWritingRulesAgent.class);
        mockTestCaseWritingRulesAgent = mock(TestCaseWritingRulesAgent.class);

        // Use reflection to inject mocks
        java.lang.reflect.Field trackerField = ProjectSetupAnalysisJob.class.getDeclaredField("trackerClient");
        trackerField.setAccessible(true);
        trackerField.set(job, mockTrackerClient);

        java.lang.reflect.Field jiraField = ProjectSetupAnalysisJob.class.getDeclaredField("jiraClient");
        jiraField.setAccessible(true);
        jiraField.set(job, mockJiraClient);

        java.lang.reflect.Field finalStatusField = ProjectSetupAnalysisJob.class.getDeclaredField("finalStatusDetectionAgent");
        finalStatusField.setAccessible(true);
        finalStatusField.set(job, mockFinalStatusDetectionAgent);

        java.lang.reflect.Field projectSetupField = ProjectSetupAnalysisJob.class.getDeclaredField("projectSetupAnalysisAgent");
        projectSetupField.setAccessible(true);
        projectSetupField.set(job, mockProjectSetupAnalysisAgent);

        java.lang.reflect.Field workflowField = ProjectSetupAnalysisJob.class.getDeclaredField("workflowAnalysisAgent");
        workflowField.setAccessible(true);
        workflowField.set(job, mockWorkflowAnalysisAgent);

        java.lang.reflect.Field storyField = ProjectSetupAnalysisJob.class.getDeclaredField("storyDescriptionWritingRulesAgent");
        storyField.setAccessible(true);
        storyField.set(job, mockStoryDescriptionWritingRulesAgent);

        java.lang.reflect.Field testCaseField = ProjectSetupAnalysisJob.class.getDeclaredField("testCaseWritingRulesAgent");
        testCaseField.setAccessible(true);
        testCaseField.set(job, mockTestCaseWritingRulesAgent);
    }

    @Test
    public void testExecuteJobWithValidParams() throws Exception {
        // Setup
        ProjectSetupAnalysisJobParams params = new ProjectSetupAnalysisJobParams();
        params.setProjectKey("TEST");

        List<IssueType> issueTypes = new ArrayList<>();
        org.json.JSONObject storyTypeJson = new org.json.JSONObject();
        storyTypeJson.put("name", "Story");
        storyTypeJson.put("id", "10001");
        IssueType storyType = new IssueType(storyTypeJson);
        issueTypes.add(storyType);

        JSONArray finalStatuses = new JSONArray();
        finalStatuses.put("Done");
        finalStatuses.put("Closed");

        JSONObject projectSetupResult = new JSONObject();
        projectSetupResult.put("issueTypes", new JSONArray());
        projectSetupResult.put("fields", new JSONObject());

        JSONObject workflowResult = new JSONObject();
        workflowResult.put("summary", "Workflow analysis");

        JSONObject storyRules = new JSONObject();
        storyRules.put("rules", "Story writing rules");

        JSONObject testCaseRules = new JSONObject();
        testCaseRules.put("rules", "Test case writing rules");

        @SuppressWarnings("unchecked")
        List<ITicket> completedTickets = new ArrayList<>();

        // Mock behavior
        when(mockJiraClient.getIssueTypes("TEST")).thenReturn(issueTypes);
        when(mockJiraClient.getFields("TEST")).thenReturn("{\"fields\":[]}");
        when(mockFinalStatusDetectionAgent.run(any(FinalStatusDetectionAgent.Params.class))).thenReturn(finalStatuses);
        when(mockProjectSetupAnalysisAgent.run(any(ProjectSetupAnalysisAgent.Params.class))).thenReturn(projectSetupResult);
        doReturn(completedTickets).when(mockTrackerClient).searchAndPerform(anyString(), any(String[].class));
        when(mockTrackerClient.getExtendedQueryFields()).thenReturn(new String[]{"summary", "description"});
        when(mockWorkflowAnalysisAgent.run(any(WorkflowAnalysisAgent.Params.class))).thenReturn(workflowResult);
        when(mockStoryDescriptionWritingRulesAgent.run(any(StoryDescriptionWritingRulesAgent.Params.class))).thenReturn(storyRules);
        when(mockTestCaseWritingRulesAgent.run(any(TestCaseWritingRulesAgent.Params.class))).thenReturn(testCaseRules);

        // Execute
        JSONObject result = job.executeJob(params);

        // Verify
        assertNotNull(result);
        assertEquals("TEST", result.getString("projectKey"));
        assertNotNull(result.get("finalStatuses"));
        assertNotNull(result.get("workflowAnalysis"));
        assertNotNull(result.get("storyDescriptionRules"));
        assertNotNull(result.get("testCaseRules"));

        verify(mockJiraClient).getIssueTypes("TEST");
        verify(mockJiraClient).getFields("TEST");
        verify(mockFinalStatusDetectionAgent).run(any(FinalStatusDetectionAgent.Params.class));
        verify(mockProjectSetupAnalysisAgent).run(any(ProjectSetupAnalysisAgent.Params.class));
        verify(mockWorkflowAnalysisAgent).run(any(WorkflowAnalysisAgent.Params.class));
        verify(mockStoryDescriptionWritingRulesAgent).run(any(StoryDescriptionWritingRulesAgent.Params.class));
        verify(mockTestCaseWritingRulesAgent).run(any(TestCaseWritingRulesAgent.Params.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteJobWithNullProjectKey() throws Exception {
        ProjectSetupAnalysisJobParams params = new ProjectSetupAnalysisJobParams();
        params.setProjectKey(null);

        job.executeJob(params);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteJobWithEmptyProjectKey() throws Exception {
        ProjectSetupAnalysisJobParams params = new ProjectSetupAnalysisJobParams();
        params.setProjectKey("");

        job.executeJob(params);
    }
}
