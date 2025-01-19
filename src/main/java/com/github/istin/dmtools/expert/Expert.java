package com.github.istin.dmtools.expert;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConfluencePagesContext;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.ai.agent.*;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.ITextMatch;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.di.DaggerExpertComponent;
import com.github.istin.dmtools.di.SourceCodeFactory;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import org.json.JSONArray;

import javax.inject.Inject;
import java.util.*;

public class Expert extends AbstractJob<ExpertParams> {

    @Inject
    TrackerClient<? extends ITicket> trackerClient;

    @Inject
    Confluence confluence;

    @Inject
    AI ai;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    @Inject
    SourceCodeFactory sourceCodeFactory;

    @Inject
    SourceImpactAssessmentAgent sourceImpactAssessmentAgent;

    @Inject
    KeywordGeneratorAgent keywordGeneratorAgent;

    @Inject
    SummaryContextAgent summaryContextAgent;

    @Inject
    SnippetExtensionAgent snippetExtensionAgent;

    @Inject
    RequestSimplifierAgent requestSimplifierAgent;

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

        JAssistant jAssistant = new JAssistant(trackerClient, null, ai, promptTemplateReader);

        String finalProjectContext = projectContext;
        trackerClient.searchAndPerform(ticket -> {
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext();

            if (expertParams.isCodeAsSource()) {
                RequestSimplifierAgent.Result structuredRequest = requestSimplifierAgent.run(new RequestSimplifierAgent.Params(finalProjectContext + "\n" + ticketContext.toText() + "\n" + requestWithContext));
                String fileExtendedContext = extendContextWithCode(expertParams, structuredRequest);
                requestWithContext.append("\n").append(fileExtendedContext);
            }

            String response = jAssistant.makeResponseOnRequest(ticketContext, finalProjectContext, requestWithContext.toString());
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

    private String extendContextWithCode(ExpertParams expertParams, RequestSimplifierAgent.Result structuredRequest) throws Exception {
        String fullTask = structuredRequest.toString();
        StringBuffer filesContextSummary = new StringBuffer();
        if (sourceImpactAssessmentAgent.run(new SourceImpactAssessmentAgent.Params("source codebase", fullTask))) {
            String keywordsBlacklist = expertParams.getKeywordsBlacklist();
            if (keywordsBlacklist.startsWith("https://")) {
                keywordsBlacklist = confluence.contentByUrl(keywordsBlacklist).getStorage().getValue();
            }
            JSONArray keywords = keywordGeneratorAgent.run(new KeywordGeneratorAgent.Params(fullTask, keywordsBlacklist));
            Map<String, List<IFile>> mapping = new HashMap<>();
            List<IFile> allFiles = new ArrayList<>();
            Set<String> checkedFiles = new HashSet<>();
            for (int i = 0; i < keywords.length(); i++) {
                String keyword = keywords.getString(i);
                SourceCodeConfig[] sourceCodeConfigs = expertParams.getSourceCodeConfig();
                List<SourceCode> sourceCodes = new SourceCodeFactory().createSourceCodes(sourceCodeConfigs);
                for (SourceCode sourceCode : sourceCodes) {
                    List<IFile> files = sourceCode.searchFiles(sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), keyword);

                    if (files.isEmpty()) {
                        continue;
                    }

                    int counterOfInvalidResponses = 0;
                    int filesCounter = 0;
                    for (IFile file : files) {
                        String selfLink = file.getSelfLink();
                        if (checkedFiles.contains(selfLink)) {
                            counterOfInvalidResponses = 0;
                            continue;
                        }

                        List<ITextMatch> textMatches = file.getTextMatches();
                        StringBuffer buffer = new StringBuffer();
                        for (ITextMatch textMatch : textMatches) {
                            buffer.append(textMatch.getFragment()).append("\n");
                        }
                        Boolean checkedInDetails = false;
                        String response;
                        System.out.println("file selflink: " + selfLink);
                        if (snippetExtensionAgent.run(new SnippetExtensionAgent.Params(buffer.toString(), fullTask))) {
                            checkedInDetails = true;
                            response = summaryContextAgent.run(new SummaryContextAgent.Params(fullTask, "file " + selfLink + " " + sourceCode.getFileContent(selfLink)));
                        } else {
                            response = summaryContextAgent.run(new SummaryContextAgent.Params(fullTask, "file snippet " + selfLink + " " + buffer));
                        }
                        if (!response.isEmpty()) {
                            counterOfInvalidResponses = 0;
                            filesContextSummary.append(response);
                        } else {
                            counterOfInvalidResponses++;
                        }
                        checkedFiles.add(selfLink);
                        System.out.println("progress: " + i + " " + keyword + " Details " + checkedInDetails + " Checked Files " + checkedFiles.size() + " From " + files.size() + " " + filesContextSummary);
                        filesCounter++;
                        if (i != 0 && counterOfInvalidResponses > 10) {
                            //search query is not valid
                            break;
                        }

                        if (filesCounter >= expertParams.getFilesLimit()) {
                            break;
                        }

                        //if files context it's enough to answer question break the loop
                    }

                    mapping.put(keyword, files);
                    //if list is too long probably need to limit it
                    allFiles.addAll(files);
                }
            }

            //TODO based on filesContextSummary extend keywords

            if (allFiles.isEmpty()) {
                //TODO regenerate query to search in files update blacklist
            } else {

            }
        }
        return filesContextSummary.toString();
    }

}