package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.di.DaggerTestCaseVisualizerAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;

import java.util.List;

public class TestCaseVisualizerAgent extends AbstractSimpleAgent<TestCaseVisualizerAgent.Params, String> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String testCases;
        private String existingDiagram;
        private String diagramType;
    }

    public TestCaseVisualizerAgent() {
        super("agents/test_case_visualizer");
        DaggerTestCaseVisualizerAgentComponent.create().inject(this);
    }

    @Override
    public String transformAIResponse(Params params, String response) throws Exception {
        return response;
    }
}