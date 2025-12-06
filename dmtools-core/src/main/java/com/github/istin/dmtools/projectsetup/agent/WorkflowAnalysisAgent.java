package com.github.istin.dmtools.projectsetup.agent;

import com.github.istin.dmtools.ai.agent.AbstractSimpleAgent;
import com.github.istin.dmtools.ai.utils.AIResponseParser;
import com.github.istin.dmtools.di.DaggerWorkflowAnalysisAgentComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

public class WorkflowAnalysisAgent extends AbstractSimpleAgent<WorkflowAnalysisAgent.Params, JSONObject> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String projectKey;
        private JSONArray finalStatuses;
        private String completedTicketsData;
    }

    public WorkflowAnalysisAgent() {
        super("agents/workflow_analysis");
        DaggerWorkflowAnalysisAgentComponent.create().inject(this);
    }

    @Override
    public JSONObject transformAIResponse(Params params, String response) throws Exception {
        return AIResponseParser.parseResponseAsJSONObject(response);
    }
}
