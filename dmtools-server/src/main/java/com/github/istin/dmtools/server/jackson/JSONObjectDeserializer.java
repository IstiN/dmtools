package com.github.istin.dmtools.server.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;
import java.io.IOException;

public class JSONObjectDeserializer extends JsonDeserializer<JSONObject> {
    @Override
    public JSONObject deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        return new JSONObject(node.toString());
    }
} 