package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.di.DaggerAutomationTestingGeneratorAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.List;

public class AutomationTestingGeneratorAgent extends AbstractSimpleAgent<AutomationTestingGeneratorAgent.Params, String> {

    @AllArgsConstructor
    @Getter
    public static class Params implements GetFiles {
        private String currentUri;
        private String screenSource;
        private String task;
        private String lastError;
        private String previousJavascript;
        private List<File> files;
    }

    public AutomationTestingGeneratorAgent() {
        super("agents/automation_testing_js_generator");
        DaggerAutomationTestingGeneratorAgentComponent.create().inject(this);
    }

    @Override
    public String transformAIResponse(Params params, String response) throws Exception {
        return AIResponseParser.parseCodeResponse(response);
    }
}