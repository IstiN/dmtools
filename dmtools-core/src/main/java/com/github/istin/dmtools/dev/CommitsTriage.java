package com.github.istin.dmtools.dev;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.IBody;
import com.github.istin.dmtools.common.model.ICommit;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.di.DaggerCommitsTriageComponent;
import com.github.istin.dmtools.di.SourceCodeFactory;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.prompt.Prompt;
import com.github.istin.dmtools.prompt.PromptContext;
import kotlin.Pair;
import lombok.Getter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class CommitsTriage extends AbstractJob<CommitsTriageParams> {

    @Inject
    TrackerClient<? extends ITicket> trackerClient;

    @Inject
    @Getter
    AI ai;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    @Inject
    SourceCodeFactory sourceCodeFactory;

    public CommitsTriage() {
        DaggerCommitsTriageComponent.create().inject(this);
    }

    @Override
    public void runJob(CommitsTriageParams params) throws Exception {
        SourceCode sourceCode = sourceCodeFactory.createSourceCodes(params.getSourceType());

        trackerClient.searchAndPerform(ticket -> {
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext();

            PromptContext context = new PromptContext(params);
            context.set("ticket", ticketContext);
            String triageResults = triage(sourceCode, params, context);
            trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(params.getInitiator()) + ", below results of triage. \n" + triageResults);
            return false;
        }, params.getInputJQL(), trackerClient.getExtendedQueryFields());
    }

    public String triage(SourceCode sourceCode, CommitsTriageParams params, PromptContext context) throws Exception {
        List<ICommit> branchCommits = sourceCode.getCommitsFromBranch(params.getWorkspace(),
                params.getRepo(), params.getBranch(), params.getStartDate(), params.getEndDate());
        List<Pair<ICommit,String>> potentialCommits = new ArrayList<>();
        StringBuilder finalResponse = new StringBuilder();
        for (ICommit commit : branchCommits) {

            IBody commitDiff = sourceCode.getCommitDiff(params.getWorkspace(),
                    params.getRepo(), commit.getHash());

            String body = commitDiff.getBody();
            if (body.isEmpty()) {
                continue;
            }
            context.set("diff", body);
            String devCommitsTriagePrompt = new Prompt(promptTemplateReader, "developer_commits_triage", context).prepare();
            String response = ai.chat(params.getModel(), devCommitsTriagePrompt, context.getFiles());
            try {
                if (AIResponseParser.parseBooleanResponse(response)) {
                    potentialCommits.add(new Pair<>(commit, response));
                }
            } catch (IllegalArgumentException e) {
                // just skip it
            }
        }
        for (Pair<ICommit,String> pair :  potentialCommits) {
            ICommit commit = pair.component1();
            String aiResponse = pair.component2();
            finalResponse.append(DateUtils.formatToJiraDate(commit.getCommiterTimestamp()));
            finalResponse.append("\n");
            finalResponse.append(commit.getMessage());
            finalResponse.append("\n");
            finalResponse.append(commit.getHash());
            finalResponse.append("\n");
            finalResponse.append(aiResponse);
            finalResponse.append("\n");
        }
        return finalResponse.toString();
    }
}
