package com.github.istin.dmtools.server.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import java.io.IOException;

public class JSONArrayDeserializer extends JsonDeserializer<JSONArray> {
    @Override
    public JSONArray deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        return new JSONArray(node.toString());
    }
} 