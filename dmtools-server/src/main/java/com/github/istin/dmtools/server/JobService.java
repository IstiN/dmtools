package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.model.Metadata;
import com.github.istin.dmtools.ba.BusinessAnalyticDORGeneration;
import com.github.istin.dmtools.ba.RequirementsCollector;
import com.github.istin.dmtools.ba.UserStoryGenerator;
import com.github.istin.dmtools.dev.CodeGenerator;
import com.github.istin.dmtools.dev.CommitsTriage;
import com.github.istin.dmtools.dev.UnitTestsGenerator;
import com.github.istin.dmtools.diagram.DiagramsCreator;
import com.github.istin.dmtools.documentation.DocumentationGenerator;
import com.github.istin.dmtools.estimations.JEstimator;
import com.github.istin.dmtools.expert.Expert;
import com.github.istin.dmtools.job.Job;
import com.github.istin.dmtools.job.JobParams;
import com.github.istin.dmtools.job.Params;
import com.github.istin.dmtools.presale.PreSaleSupport;
import com.github.istin.dmtools.qa.TestCasesGenerator;
import com.github.istin.dmtools.report.productivity.BAProductivityReport;
import com.github.istin.dmtools.report.productivity.DevProductivityReport;
import com.github.istin.dmtools.report.productivity.QAProductivityReport;
import com.github.istin.dmtools.sa.SolutionArchitectureCreator;
import com.github.istin.dmtools.sm.ScrumMasterDaily;
import com.github.istin.dmtools.sync.SourceCodeCommitTrackerSyncJob;
import com.github.istin.dmtools.sync.SourceCodeTrackerSyncJob;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class JobService {

    protected static List<Job> JOBS = Arrays.asList(
            new PreSaleSupport(),
            new DocumentationGenerator(),
            new RequirementsCollector(),
            new JEstimator(),
            new TestCasesGenerator(),
            new SolutionArchitectureCreator(),
            new DiagramsCreator(),
            new CodeGenerator(),
            new DevProductivityReport(),
            new BAProductivityReport(),
            new BusinessAnalyticDORGeneration(),
            new QAProductivityReport(),
            new ScrumMasterDaily(),
            new Expert(),
            new SourceCodeTrackerSyncJob(),
            new SourceCodeCommitTrackerSyncJob(),
            new UserStoryGenerator(),
            new UnitTestsGenerator(),
            new CommitsTriage()
    );

    public void executeJob(JobParams jobParams) throws Exception {
        for (Job job : JOBS) {
            if (job.getName().equalsIgnoreCase(jobParams.getName())) {
                Object paramsByClass = jobParams.getParamsByClass(job.getParamsClass());
                initMetadata(job, paramsByClass);
                job.runJob(paramsByClass);
                return;
            }
        }
        throw new IllegalArgumentException("Job with name '" + jobParams.getName() + "' not found.");
    }

    private void initMetadata(Job job, Object paramsByClass) {
        if (paramsByClass instanceof Params) {
            Metadata metadata = ((Params) paramsByClass).getMetadata();
            if (metadata != null) {
                metadata.init(job);
                AI ai = job.getAi();
                if (ai != null) {
                    ai.setMetadata(metadata);
                }
            }
        }
    }
} 