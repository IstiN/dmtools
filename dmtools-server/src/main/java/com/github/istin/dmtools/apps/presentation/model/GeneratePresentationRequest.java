package com.github.istin.dmtools.apps.presentation.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class GeneratePresentationRequest {
    private String jsScript;
    private JsonNode paramsForJs;
} 