package com.github.istin.dmtools.sync;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.ICommit;
import com.github.istin.dmtools.common.model.IDiffStats;
import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.di.SourceCodeFactory;
import com.github.istin.dmtools.job.AbstractJob;

import java.util.Calendar;
import java.util.List;

public class SourceCodeCommitTrackerSyncJob extends AbstractJob<SourceCodeCommitTrackerSyncParams> {
    @Override
    public void runJob(SourceCodeCommitTrackerSyncParams sourceCodeCommitTrackerSyncParams) throws Exception {
        SourceCodeConfig[] sourceCodeConfigs = sourceCodeCommitTrackerSyncParams.getSourceCodeConfig();
        List<SourceCode> sourceCodes = new SourceCodeFactory().createSourceCodes(sourceCodeConfigs);

        for (SourceCode sourceCode : sourceCodes) {
            SourceCodeConfig sourceCodeConfig = sourceCode.getDefaultConfig();
            String defaultWorkspace = sourceCode.getDefaultWorkspace();
            String defaultRepository = sourceCode.getDefaultRepository();
            SourceCodeCommitTrackerSyncParams.SyncType syncType = sourceCodeCommitTrackerSyncParams.getSyncType();
            List<ICommit> commitsFromBranch = null;
            if (syncType == SourceCodeCommitTrackerSyncParams.SyncType.ALL) {
                commitsFromBranch = sourceCode.getCommitsFromBranch(defaultWorkspace, defaultRepository, sourceCodeConfig.getBranchName(), null, null);
            } else if (syncType == SourceCodeCommitTrackerSyncParams.SyncType.ONE_DAY) {
                Calendar now = Calendar.getInstance();
                String endDate = DateUtils.formatToJiraDate(now);
                now.add(Calendar.DATE, -1);
                String startDate = DateUtils.formatToJiraDate(now);
                commitsFromBranch = sourceCode.getCommitsFromBranch(defaultWorkspace, defaultRepository, sourceCodeConfig.getBranchName(), startDate, endDate);
            } else if (syncType == SourceCodeCommitTrackerSyncParams.SyncType.RANGE) {
                commitsFromBranch = sourceCode.getCommitsFromBranch(defaultWorkspace, defaultRepository, sourceCodeConfig.getBranchName(), sourceCodeCommitTrackerSyncParams.getStartDate(), sourceCodeCommitTrackerSyncParams.getEndDate());
            }
            String[] issueIdCodes = sourceCodeCommitTrackerSyncParams.getIssueIdCodes();
            IssuesIDsParser issuesIDsParser = new IssuesIDsParser(issueIdCodes);
            issuesIDsParser.setParams(sourceCodeCommitTrackerSyncParams.getIssuesIDsParserParams());
            for (ICommit commit : commitsFromBranch) {
                List<String> keys = issuesIDsParser.parseIssues(commit.getMessage());
                if (!keys.isEmpty()) {
                    IDiffStats commitDiffStat = sourceCode.getCommitDiffStat(defaultWorkspace, defaultRepository, commit.getHash());
                    int total = commitDiffStat.getStats().getTotal();
                    if (total == 0) {
                        System.out.println("TICKET Commit: commit url: "+ commit.getUrl() +  " ======== contains issues: " + keys +  " commit diff: EMPTY");
                    } else {
                        System.out.println("TICKET Commit: commit url: "+ commit.getUrl() + " ======== contains issues: " + keys + " commit diff: " + total);
                    }

                } else {
                    System.out.println(commit.getMessage());
                }
            }
        }
        //System.out.println(commitsFromBranch);
    }

    @Override
    public AI getAi() {
        return null;
    }
}
