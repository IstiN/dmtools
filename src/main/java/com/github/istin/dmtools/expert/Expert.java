package com.github.istin.dmtools.expert;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConfluencePagesContext;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.ai.agent.RequestSimplifierAgent;
import com.github.istin.dmtools.ai.agent.SourceImpactAssessmentAgent;
import com.github.istin.dmtools.ai.agent.TeamAssistantAgent;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.di.DaggerExpertComponent;
import com.github.istin.dmtools.di.SourceCodeFactory;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.search.AbstractSearchOrchestrator;
import com.github.istin.dmtools.search.CodebaseSearchOrchestrator;
import com.github.istin.dmtools.search.ConfluenceSearchOrchestrator;
import com.github.istin.dmtools.search.TrackerSearchOrchestrator;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class Expert extends AbstractJob<ExpertParams> {

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
    SourceCodeFactory sourceCodeFactory;

    @Inject
    SourceImpactAssessmentAgent sourceImpactAssessmentAgent;

    @Inject
    RequestSimplifierAgent requestSimplifierAgent;

    @Inject
    TeamAssistantAgent teamAssistantAgent;

    CodebaseSearchOrchestrator codebaseSearchOrchestrator;

    @Inject
    ConfluenceSearchOrchestrator confluenceSearchOrchestrator;

    @Inject
    TrackerSearchOrchestrator trackerSearchOrchestrator;

    public Expert() {
        DaggerExpertComponent.create().inject(this);
    }

    @Override
    public void runJob(ExpertParams expertParams) throws Exception {
        String projectContext = expertParams.getProjectContext();
        String request = expertParams.getRequest();
        String[] confluencePages = expertParams.getConfluencePages();
        ExpertParams.OutputType outputType = expertParams.getOutputType();
        String initiator = expertParams.getInitiator();
        String inputJQL = expertParams.getInputJql();
        String fieldName = expertParams.getFieldName();

        if (projectContext.startsWith("https://")) {
            projectContext = confluence.contentByUrl(projectContext).getStorage().getValue();
        }

        StringBuilder requestWithContext = new StringBuilder();
        requestWithContext.append(request).append("\n");
        if (confluencePages != null) {
            requestWithContext.append(new ConfluencePagesContext(confluencePages, confluence).toText());
        }


        String finalProjectContext = projectContext;
        trackerClient.searchAndPerform(ticket -> {
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext(true);

            if (expertParams.isCodeAsSource() || expertParams.isConfluenceAsSource() || expertParams.isTrackerAsSource()) {
                RequestSimplifierAgent.Result structuredRequest = requestSimplifierAgent.run(new RequestSimplifierAgent.Params(finalProjectContext + "\n" + ticketContext.toText() + "\n" + requestWithContext));
                if (expertParams.isCodeAsSource()) {
                    String fileExtendedContext = extendContextWithCode(ticket.getTicketKey(), expertParams, structuredRequest);
                    trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", detailed information from files. \n" + fileExtendedContext);
                    requestWithContext.append("\n").append(fileExtendedContext);
                }
                if (expertParams.isConfluenceAsSource()) {
                    String confluenceExtendedContext = extendContextWithConfluence(ticket.getTicketKey(), expertParams, structuredRequest);
                    trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", detailed information from confluence. \n" + confluenceExtendedContext);
                    requestWithContext.append("\n").append(confluenceExtendedContext);
                }
                if (expertParams.isTrackerAsSource()) {
                    String trackerExtendedContext = extendContextWithTracker(ticket.getTicketKey(), expertParams, structuredRequest);
                    trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", detailed information from tracker. \n" + trackerExtendedContext);
                    requestWithContext.append("\n").append(trackerExtendedContext);
                }
            }

            String response =  teamAssistantAgent.run(new TeamAssistantAgent.Params(finalProjectContext, requestWithContext.toString(), ticketContext.toText(), ""));
            if (outputType == ExpertParams.OutputType.field) {
                String fieldCustomCode = ((JiraClient) BasicJiraClient.getInstance()).getFieldCustomCode(ticket.getTicketKey().split("-")[0], fieldName);
                trackerClient.updateTicket(ticket.getTicketKey(), fields -> fields.set(fieldCustomCode, response));
                trackerClient.postComment(ticket.getTicketKey(), trackerClient.tag(initiator) + ", there is response in '"+ fieldName + "' on your request: \n" + request);
            } else {
                trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", there is response on your request: \n" + request + "\n\nAI Response is: \n" + response);
            }
            return false;
        }, inputJQL, trackerClient.getExtendedQueryFields());
    }

    private String getKeywordsBlacklist(String keywordsBlacklist) throws Exception {
        if (keywordsBlacklist != null && keywordsBlacklist.startsWith("https://")) {
            return confluence.contentByUrl(keywordsBlacklist).getStorage().getValue();
        }
        if (keywordsBlacklist == null) {
            return "";
        }
        return keywordsBlacklist;
    }

    protected void saveAndAttachStats(String ticketKey, String result, AbstractSearchOrchestrator orchestratorClass) {

        try {
            attachResponse(orchestratorClass, "_stats.json", orchestratorClass.getSearchStats().toJson().toString(2), ticketKey, "application/json");
            attachResponse(orchestratorClass, "_result.txt", result, ticketKey, "text/plain");
        } catch (Exception e) {
            System.err.println("Failed to save and attach stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void attachResponse(AbstractSearchOrchestrator orchestratorClass, String file, String result, String ticketKey, String contentType) throws IOException {
        String fileNameResult = orchestratorClass.getClass().getSimpleName() + file;
        File tempFileResult = File.createTempFile(fileNameResult, null);

        // Write JSON to file using Commons IO
        FileUtils.writeStringToFile(tempFileResult, result, "UTF-8");

        // Attach file to ticket
        trackerClient.attachFileToTicket(
                ticketKey,
                fileNameResult,
                contentType,
                tempFileResult
        );
        // Clean up temp file
        FileUtils.deleteQuietly(tempFileResult);
    }


    private String extendContextWithCode(String ticketKey, ExpertParams expertParams, RequestSimplifierAgent.Result structuredRequest) throws Exception {
        SourceCodeConfig[] sourceCodeConfig = expertParams.getSourceCodeConfig();
        codebaseSearchOrchestrator = new CodebaseSearchOrchestrator(sourceCodeConfig);
        String keywordsBlacklist = getKeywordsBlacklist(expertParams.getKeywordsBlacklist());
        int filesLimit = expertParams.getFilesLimit();
        String result = codebaseSearchOrchestrator.run(expertParams.getSearchOrchestratorType(), structuredRequest.toString(), keywordsBlacklist, filesLimit, expertParams.getFilesLimit());
        saveAndAttachStats(ticketKey, result, codebaseSearchOrchestrator);
        return result;
    }

    private String extendContextWithConfluence(String ticketKey, ExpertParams expertParams, RequestSimplifierAgent.Result structuredRequest) throws Exception {
        String keywordsBlacklist = getKeywordsBlacklist(expertParams.getKeywordsBlacklist());
        int confluenceLimit = expertParams.getConfluenceLimit();
        int confluenceIterations = expertParams.getConfluenceIterations();
        String response = confluenceSearchOrchestrator.run(expertParams.getSearchOrchestratorType(), structuredRequest.toString(), keywordsBlacklist, confluenceLimit, confluenceIterations);
        saveAndAttachStats(ticketKey, response, confluenceSearchOrchestrator);
        return response;
    }

    private String extendContextWithTracker(String ticketKey, ExpertParams expertParams, RequestSimplifierAgent.Result structuredRequest) throws Exception {
        String keywordsBlacklist = getKeywordsBlacklist(expertParams.getKeywordsBlacklist());
        int trackerLimit = expertParams.getTrackerLimit();
        int trackerIterations = expertParams.getTrackerIterations();
        String response = trackerSearchOrchestrator.run(expertParams.getSearchOrchestratorType(), structuredRequest.toString(), keywordsBlacklist, trackerLimit, trackerIterations);
        saveAndAttachStats(ticketKey, response, trackerSearchOrchestrator);
        return response;
    }


}