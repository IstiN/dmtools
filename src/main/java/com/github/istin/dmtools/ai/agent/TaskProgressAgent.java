package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.di.DaggerTaskProgressAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class TaskProgressAgent extends AbstractSimpleAgent<TaskProgressAgent.Params, TaskProgressAgent.Result> {

    @AllArgsConstructor
    @Getter
    @Data
    public static class Params implements GetFiles {
        private String steps;
        private String previousAssessment;
        private Boolean requireExplanation;
        private List<File> files;
    }

    @AllArgsConstructor
    @Getter
    @Data
    public static class Result {
        private JSONArray completedSteps;
        private JSONArray nextSteps;
        private String explanation;
    }

    public TaskProgressAgent() {
        super("agents/task_progress");
        DaggerTaskProgressAgentComponent.create().inject(this);
    }

    @Override
    public Result transformAIResponse(Params params, String response) throws Exception {
        System.out.println(response);
        JSONObject jsonResponse = AIResponseParser.parseResponseAsJSONObject(response);
        return new Result(
                jsonResponse.getJSONArray("completedSteps"),
                jsonResponse.getJSONArray("nextSteps"),
                jsonResponse.optString("explanation")
        );
    }
}