package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.di.DaggerSummaryContextAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class SummaryContextAgent extends AbstractSimpleAgent<SummaryContextAgent.Params, String> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String task;
        private String rawData;
    }

    public SummaryContextAgent() {
        super("agents/summary_context");
        DaggerSummaryContextAgentComponent.create().inject(this);
    }

    @Override
    String transformAIResponse(Params params, String response) throws Exception {
        if (response.length() < 10) {
            try {
                if (!AIResponseParser.parseBooleanResponse(response)) {
                    return "";
                }
            } catch (IllegalArgumentException e) {
                return "";
            }
        }
        return response;
    }
}