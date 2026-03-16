package com.github.istin.dmtools.projectsetup.agent;

import com.github.istin.dmtools.ai.agent.AbstractSimpleAgent;
import com.github.istin.dmtools.ai.utils.AIResponseParser;
import com.github.istin.dmtools.di.DaggerProjectSetupAnalysisAgentComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;

public class ProjectSetupAnalysisAgent extends AbstractSimpleAgent<ProjectSetupAnalysisAgent.Params, JSONObject> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String projectKey;
        private String issueTypesJson;
        private String fieldsJson;
    }

    public ProjectSetupAnalysisAgent() {
        super("agents/project_setup_analysis");
        DaggerProjectSetupAnalysisAgentComponent.create().inject(this);
    }

    @Override
    public JSONObject transformAIResponse(Params params, String response) throws Exception {
        return AIResponseParser.parseResponseAsJSONObject(response);
    }
}
