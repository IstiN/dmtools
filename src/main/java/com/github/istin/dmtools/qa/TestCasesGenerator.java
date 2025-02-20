package com.github.istin.dmtools.qa;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConfluencePagesContext;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.ai.agent.RelatedTestCaseAgent;
import com.github.istin.dmtools.ai.agent.RelatedTestCasesAgent;
import com.github.istin.dmtools.ai.agent.TestCaseGeneratorAgent;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.jira.model.Fields;
import com.github.istin.dmtools.atlassian.jira.model.Relationship;
import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.StringUtils;
import com.github.istin.dmtools.di.DaggerTestCasesGeneratorComponent;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TestCasesGenerator extends AbstractJob<TestCasesGeneratorParams> {

    @Inject
    TrackerClient<? extends ITicket> trackerClient;

    @Inject
    Confluence confluence;

    @Inject
    @Getter
    AI ai;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    @Inject
    TestCaseGeneratorAgent testCaseGeneratorAgent;

    @Inject
    RelatedTestCasesAgent relatedTestCasesAgent;

    @Inject
    RelatedTestCaseAgent relatedTestCaseAgent;


    public TestCasesGenerator() {
        DaggerTestCasesGeneratorComponent.create().inject(this);
    }

    @Override
    public void runJob(TestCasesGeneratorParams params) throws Exception {
        trackerClient.searchAndPerform(ticket -> {
            List<? extends ITicket> listOfAllTestCases = trackerClient.searchAndPerform(params.getExistingTestCasesJql(), new String[]{Fields.SUMMARY, Fields.DESCRIPTION});
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext();
            String additionalRules = new ConfluencePagesContext(params.getConfluencePages(), confluence).toText();
            generateTestCases(ticketContext, additionalRules, listOfAllTestCases, params);
            trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(params.getInitiator()) + ", similar test cases are linked and new test cases are generated.");
            return false;
        }, params.getInputJql(), trackerClient.getExtendedQueryFields());
    }

    public void generateTestCases(TicketContext ticketContext, String extraRules, List<? extends ITicket> listOfAllTestCases, TestCasesGeneratorParams params) throws Exception {
        ITicket mainTicket = ticketContext.getTicket();
        String key = mainTicket.getTicketKey();

        List<ITicket> finaResults = findAndLinkSimilarTestCasesBySummary(ticketContext, listOfAllTestCases, true, params.getRelatedTestCasesRules(), params.getTestCaseLinkRelationship());

        List<TestCaseGeneratorAgent.TestCase> newTestCases = testCaseGeneratorAgent.run(new TestCaseGeneratorAgent.Params(params.getTestCasesPriorities(), finaResults.toString(), ticketContext.toText(), extraRules));

        if (params.getOutputType().equals(TestCasesGeneratorParams.OutputType.comment)) {
            StringBuilder comment = new StringBuilder();
            for (TestCaseGeneratorAgent.TestCase testCase : newTestCases) {
                comment.append("Summary: ").append(testCase.getSummary()).append("<br>");
                comment.append("Priority: ").append(testCase.getPriority()).append("<br>");
                comment.append("Description: ").append(testCase.getDescription()).append("<br>");
            }
            trackerClient.postComment(key, comment.toString());
        } else {
            for (TestCaseGeneratorAgent.TestCase testCase : newTestCases) {
                String projectCode = key.split("-")[0];
                String description = testCase.getDescription();
                if (trackerClient.getTextType() == TrackerClient.TextType.MARKDOWN) {
                    description = StringUtils.convertToMarkdown(description);
                }
                Ticket createdTestCase = new Ticket(trackerClient.createTicketInProject(projectCode, params.getTestCaseIssueType(), testCase.getSummary(), description, new TrackerClient.FieldsInitializer() {
                    @Override
                    public void init(TrackerClient.TrackerTicketFields fields) {
                        fields.set("priority",
                                new JSONObject().put("name", testCase.getPriority())
                        );
                        fields.set("labels", new JSONArray().put("ai_generated"));
                    }
                }));

                trackerClient.linkIssueWithRelationship(mainTicket.getTicketKey(), createdTestCase.getKey(), params.getTestCaseLinkRelationship());
            }
        }
    }

    @NotNull
    public List<ITicket> findAndLinkSimilarTestCasesBySummary(TicketContext ticketContext, List<? extends ITicket> listOfAllTestCases, boolean isLink, String relatedTestCasesRulesLink, String relationship) throws Exception {
        List<ITicket> finaResults = new ArrayList<>();
        String extraRelatedTestCaseRulesFromConfluence = new ConfluencePagesContext(new String[]{relatedTestCasesRulesLink}, confluence).toText();
        int batchSize = 50;
        for (int i = 0; i < listOfAllTestCases.size(); i += batchSize) {
            List<? extends ITicket> batch = listOfAllTestCases.subList(i, Math.min(i + batchSize, listOfAllTestCases.size()));

            JSONArray testCaseKeys = relatedTestCasesAgent.run(new RelatedTestCasesAgent.Params(ticketContext.toText(), batch.toString(), extraRelatedTestCaseRulesFromConfluence));
            //find relevant test case from batch
            for (int j = 0; j < testCaseKeys.length(); j++) {
                String testCaseKey = testCaseKeys.getString(j);
                ITicket testCase = batch.stream().filter(t -> t.getKey().equals(testCaseKey)).findFirst().orElse(null);
                if (testCase != null) {
                    boolean isConfirmed = relatedTestCaseAgent.run(new RelatedTestCaseAgent.Params(ticketContext.toText(), testCase.toText(), extraRelatedTestCaseRulesFromConfluence));
                    if (isConfirmed) {
                        finaResults.add(testCase);
                        if (isLink) {
                            trackerClient.linkIssueWithRelationship(ticketContext.getTicket().getTicketKey(), testCase.getKey(), relationship);
                        }
                    }
                }
            }
        }
        return finaResults;
    }
}
