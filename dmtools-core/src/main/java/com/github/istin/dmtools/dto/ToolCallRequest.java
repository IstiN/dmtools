package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ToolCallRequest {
    private final String toolName;
    private final Map<String, Object> arguments;

    @JsonCreator
    public ToolCallRequest(@JsonProperty("toolName") String toolName, @JsonProperty("arguments") Map<String, Object> arguments) {
        this.toolName = toolName;
        this.arguments = arguments;
    }

    public String getToolName() {
        return toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }
} 