package com.github.istin.dmtools.projectsetup.agent;

import com.github.istin.dmtools.ai.agent.AbstractSimpleAgent;
import com.github.istin.dmtools.ai.utils.AIResponseParser;
import com.github.istin.dmtools.di.DaggerFinalStatusDetectionAgentComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

public class FinalStatusDetectionAgent extends AbstractSimpleAgent<FinalStatusDetectionAgent.Params, JSONArray> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String projectKey;
        private String workflowMetadata;
    }

    public FinalStatusDetectionAgent() {
        super("agents/final_status_detection");
        DaggerFinalStatusDetectionAgentComponent.create().inject(this);
    }

    @Override
    public JSONArray transformAIResponse(Params params, String response) throws Exception {
        return AIResponseParser.parseResponseAsJSONArray(response);
    }
}
