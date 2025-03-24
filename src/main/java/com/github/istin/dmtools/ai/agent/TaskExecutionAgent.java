package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.di.DaggerTaskExecutionAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

public class TaskExecutionAgent extends AbstractSimpleAgent<TaskExecutionAgent.Params, TaskExecutionAgent.Result> {

    @AllArgsConstructor
    @Getter
    @Data
    public static class Params {
        private String taskDescription;
    }

    @AllArgsConstructor
    @Getter
    @Data
    public static class Result {
        private JSONArray steps;
        private JSONObject knownData;
    }

    public TaskExecutionAgent() {
        super("agents/task_execution");
        DaggerTaskExecutionAgentComponent.create().inject(this);
    }

    @Override
    public Result transformAIResponse(Params params, String response) throws Exception {
        JSONObject jsonResponse = AIResponseParser.parseResponseAsJSONObject(response);
        return new Result(
                jsonResponse.getJSONArray("steps"),
                jsonResponse.getJSONObject("knownData")
        );
    }
}