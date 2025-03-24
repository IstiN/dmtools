package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.di.DaggerSourceImpactAssessmentAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class SourceImpactAssessmentAgent extends AbstractSimpleAgent<SourceImpactAssessmentAgent.Params, Boolean> {

    @AllArgsConstructor
    @Getter
    public static class Params {

        private String sourceType;
        private String task;

    }

    public SourceImpactAssessmentAgent() {
        super("agents/source_impact_assessment");
        DaggerSourceImpactAssessmentAgentComponent.create().inject(this);
    }

    @Override
    public Boolean transformAIResponse(Params params, String response) throws Exception {
        return AIResponseParser.parseBooleanResponse(response);
    }

}
