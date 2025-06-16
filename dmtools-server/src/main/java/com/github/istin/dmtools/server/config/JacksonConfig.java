package com.github.istin.dmtools.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.istin.dmtools.server.jackson.JSONArrayDeserializer;
import com.github.istin.dmtools.server.jackson.JSONArraySerializer;
import com.github.istin.dmtools.server.jackson.JSONObjectDeserializer;
import com.github.istin.dmtools.server.jackson.JSONObjectSerializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register JavaTimeModule for LocalDateTime support
        objectMapper.registerModule(new JavaTimeModule());
        
        SimpleModule module = new SimpleModule();
        module.addDeserializer(JSONObject.class, new JSONObjectDeserializer());
        module.addDeserializer(JSONArray.class, new JSONArrayDeserializer());
        module.addSerializer(JSONObject.class, new JSONObjectSerializer());
        module.addSerializer(JSONArray.class, new JSONArraySerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
} 