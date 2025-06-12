package com.github.istin.dmtools.server.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.json.JSONArray;

import java.io.IOException;

public class JSONArraySerializer extends JsonSerializer<JSONArray> {
    @Override
    public void serialize(JSONArray value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeRawValue(value.toString());
    }
} 