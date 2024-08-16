package com.github.istin.dmtools.job;


import com.github.istin.dmtools.ba.RequirementsCollector;
import com.github.istin.dmtools.dev.CodeGenerator;
import com.github.istin.dmtools.diagram.DiagramsCreator;
import com.github.istin.dmtools.documentation.DocumentationGenerator;
import com.github.istin.dmtools.estimations.JEstimator;
import com.github.istin.dmtools.presale.PreSaleSupport;
import com.github.istin.dmtools.qa.TestCasesGenerator;
import com.github.istin.dmtools.report.productivity.DevProductivityReport;
import com.github.istin.dmtools.sa.SolutionArchitectureCreator;
import com.github.istin.dmtools.sm.ScrumMasterDaily;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class JobRunner {

    private static List<Job> JOBS = Arrays.asList(
            new PreSaleSupport(),
            new DocumentationGenerator(),
            new RequirementsCollector(),
            new JEstimator(),
            new TestCasesGenerator(),
            new SolutionArchitectureCreator(),
            new DiagramsCreator(),
            new CodeGenerator(),
            new DevProductivityReport(),
            new ScrumMasterDaily()
    );

    public static void main(String[] args) throws Exception {
        JobParams jobParams = new JobParams(new String(decodeBase64(args[0])));
        for (Job job : JOBS) {
            if (job.getName().equalsIgnoreCase(jobParams.getName())) {
                job.runJob(jobParams.getParamsByClass(job.getParamsClass()));
                return;
            }
        }
    }

    public static String decodeBase64(String input) {
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        // Convert the decoded bytes to a string
        return new java.lang.String(decodedBytes);
    }

    public static String encodeBase64(String input) {
        byte[] decodedBytes = Base64.getEncoder().encode(input.getBytes());
        // Convert the decoded bytes to a string
        return new String(decodedBytes);
    }
}
