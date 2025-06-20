package com.github.istin.dmtools.ai.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.di.DaggerToolSelectorAgentComponent;
import com.github.istin.dmtools.dto.ToolCallRequest;
import com.github.istin.dmtools.openai.model.Tool;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class ToolSelectorAgent extends AbstractSimpleAgent<ToolSelectorAgent.Params, List<ToolCallRequest>> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Params {
        private String userMessage;
        private String availableTools;
    }

    public ToolSelectorAgent() {
        super("agents/tool-selector");
        DaggerToolSelectorAgentComponent.create().inject(this);
    }

    @Override
    public List<ToolCallRequest> transformAIResponse(Params params, String aiResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(AIResponseParser.parseResponseAsJSONArray(aiResponse).toString(), new TypeReference<>() {});
    }
} 