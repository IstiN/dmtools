package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.di.DaggerRequestSimplifierAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONArray;

public class RequestSimplifierAgent extends AbstractSimpleAgent<RequestSimplifierAgent.Params, RequestSimplifierAgent.Result> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String userRequest;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Result {
        private String request;
        private String[] questions;
        private String[] tasks;
        private String knownInfo;
    }

    public RequestSimplifierAgent() {
        super("agents/request_simplifier");
        DaggerRequestSimplifierAgentComponent.create().inject(this);
    }

    @Override
    Result transformAIResponse(Params params, String response) throws Exception {
        JSONObject jsonResponse = AIResponseParser.parseResponseAsJSONObject(response);
        String simplifiedRequest = jsonResponse.getString("request");
        String knownInfo = jsonResponse.getString("knownInfo");
        return new Result(
                simplifiedRequest,
                convertToArray(jsonResponse, "questions"),
                convertToArray(jsonResponse, "tasks"),
                knownInfo
        );
    }

    private static String[] convertToArray(JSONObject jsonResponse, String key) {
        JSONArray questionsArray = jsonResponse.getJSONArray(key);
        String[] questions = new String[questionsArray.length()];
        for (int i = 0; i < questionsArray.length(); i++) {
            questions[i] = questionsArray.getString(i);
        }
        return questions;
    }
}