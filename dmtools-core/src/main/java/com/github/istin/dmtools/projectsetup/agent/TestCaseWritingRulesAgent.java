package com.github.istin.dmtools.projectsetup.agent;

import com.github.istin.dmtools.ai.agent.AbstractSimpleAgent;
import com.github.istin.dmtools.ai.utils.AIResponseParser;
import com.github.istin.dmtools.di.DaggerTestCaseWritingRulesAgentComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;

public class TestCaseWritingRulesAgent extends AbstractSimpleAgent<TestCaseWritingRulesAgent.Params, JSONObject> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String projectKey;
        private String testCaseData;
    }

    public TestCaseWritingRulesAgent() {
        super("agents/test_case_writing_rules");
        DaggerTestCaseWritingRulesAgentComponent.create().inject(this);
    }

    @Override
    public JSONObject transformAIResponse(Params params, String response) throws Exception {
        return AIResponseParser.parseResponseAsJSONObject(response);
    }
}
