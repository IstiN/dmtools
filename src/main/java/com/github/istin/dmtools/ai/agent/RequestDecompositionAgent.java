package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.ai.ChunkPreparation;
import com.github.istin.dmtools.di.DaggerRequestSimplifierAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class RequestDecompositionAgent extends AbstractSimpleAgent<RequestDecompositionAgent.Params, RequestDecompositionAgent.Result> {

    @AllArgsConstructor
    @Getter
    public static class Params implements GetFiles, GetChunks {
        private String userRequest;
        private String rawData;
        private List<File> files;
        private List<ChunkPreparation.Chunk> chunks;
    }

    @AllArgsConstructor
    @Getter
    public static class Result {
        private String aiRole;
        private String request;
        private String[] questions;
        private String[] tasks;
        private String[] instructions;
        private String knownInfo;

        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            json.put("aiRole", aiRole);
            json.put("request", request);
            json.put("questions", new JSONArray(questions));
            json.put("tasks", new JSONArray(tasks));
            json.put("instructions", new JSONArray(instructions));
            json.put("knownInfo", knownInfo);
            return json.toString(1);
        }
    }

    public RequestDecompositionAgent() {
        super("agents/request_decomposition");
        DaggerRequestSimplifierAgentComponent.create().inject(this);
    }

    @Override
    public Result transformAIResponse(Params params, String response) throws Exception {
        JSONObject jsonResponse = AIResponseParser.parseResponseAsJSONObject(response);
        String simplifiedRequest = jsonResponse.getString("request");
        String knownInfo = jsonResponse.getString("knownInfo");
        return new Result(
                jsonResponse.getString("aiRole"),
                simplifiedRequest,
                convertToArray(jsonResponse, "questions"),
                convertToArray(jsonResponse, "tasks"),
                convertToArray(jsonResponse, "instructions"),
                knownInfo
        );
    }

    private static String[] convertToArray(JSONObject jsonResponse, String key) {
        JSONArray jsonArray = jsonResponse.getJSONArray(key);
        String[] results = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            results[i] = jsonArray.getString(i);
        }
        return results;
    }
}