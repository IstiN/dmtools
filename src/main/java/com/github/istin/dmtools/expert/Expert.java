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

import javax.inject.Inject;

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
                    String fileExtendedContext = extendContextWithCode(expertParams, structuredRequest);
                    trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", detailed information from files. \n" + fileExtendedContext);
                    requestWithContext.append("\n").append(fileExtendedContext);
                }
                if (expertParams.isConfluenceAsSource()) {
                    String confluenceExtendedContext = extendContextWithConfluence(expertParams, structuredRequest);
                    trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", detailed information from confluence. \n" + confluenceExtendedContext);
                    requestWithContext.append("\n").append(confluenceExtendedContext);
                }
                if (expertParams.isTrackerAsSource()) {
                    String trackerExtendedContext = extendContextWithTracker(expertParams, structuredRequest);
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

    private String extendContextWithCode(ExpertParams expertParams, RequestSimplifierAgent.Result structuredRequest) throws Exception {
        SourceCodeConfig[] sourceCodeConfig = expertParams.getSourceCodeConfig();
        codebaseSearchOrchestrator = new CodebaseSearchOrchestrator(sourceCodeConfig);
        String keywordsBlacklist = getKeywordsBlacklist(expertParams.getKeywordsBlacklist());
        int filesLimit = expertParams.getFilesLimit();
        return codebaseSearchOrchestrator.run(expertParams.getSearchOrchestratorType(), structuredRequest.toString(), keywordsBlacklist, filesLimit, expertParams.getFilesLimit());
    }

    private String extendContextWithConfluence(ExpertParams expertParams, RequestSimplifierAgent.Result structuredRequest) throws Exception {
        String keywordsBlacklist = getKeywordsBlacklist(expertParams.getKeywordsBlacklist());
        int confluenceLimit = expertParams.getConfluenceLimit();
        return confluenceSearchOrchestrator.run(expertParams.getSearchOrchestratorType(),  structuredRequest.toString(), keywordsBlacklist, confluenceLimit, expertParams.getConfluenceLimit());
    }

    private String extendContextWithTracker(ExpertParams expertParams, RequestSimplifierAgent.Result structuredRequest) throws Exception {
        String keywordsBlacklist = getKeywordsBlacklist(expertParams.getKeywordsBlacklist());
        int trackerLimit = expertParams.getTrackerLimit();
        return trackerSearchOrchestrator.run(expertParams.getSearchOrchestratorType(), structuredRequest.toString(), keywordsBlacklist, trackerLimit, expertParams.getTrackerLimit());
    }


}