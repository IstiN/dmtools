package com.github.istin.dmtools.codereview;

import javax.inject.Inject;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.di.DaggerGuidelinesAgentComponent;
import com.github.istin.dmtools.di.SourceCodeFactory;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;

import lombok.Getter;

public class GuidelinesAgent extends AbstractJob<GuidelinesAgentParams> {

    @Inject
    @Getter
    AI ai;

    @Inject
    SourceCodeFactory sourceCodeFactory;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    public GuidelinesAgent() {
        DaggerGuidelinesAgentComponent.create().inject(this);
    }

    @Override
    public void runJob(GuidelinesAgentParams guidelinesAgentParams) {

        // params
        SourceCodeConfig sourceCodeConfig = guidelinesAgentParams.getSourceCodeConfig();
        var projectName = guidelinesAgentParams.getProjectName();
        var historyStartDate = guidelinesAgentParams.getHistoryStartDate();

        // artifacts
        SourceCode sourceCode = sourceCodeFactory.createSourceCodes(sourceCodeConfig.getType().toString());
        var guideCreator = new GuidelinesCreator(ai, sourceCode, promptTemplateReader);

        // generate the Guide
        guideCreator.generateCodeReviewGuide(sourceCodeConfig, projectName, historyStartDate);

    }

}
