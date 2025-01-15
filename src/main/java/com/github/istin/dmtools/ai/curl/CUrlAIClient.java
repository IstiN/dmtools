package com.github.istin.dmtools.ai.curl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.networking.AbstractRestClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CUrlAIClient extends AbstractRestClient implements AI {

    private final String curlTemplate;
    private final String bodyTemplate;
    private final String responseJsonPath;
    private String model;
    private final ObjectMapper objectMapper;

    public CUrlAIClient(String basePath, String authorization, String curlUrlTemplate, String bodyTemplate, String responseJsonPath, String model) throws IOException {
        super(basePath, authorization);
        this.curlTemplate = curlUrlTemplate;
        this.bodyTemplate = bodyTemplate;
        this.responseJsonPath = responseJsonPath;
        this.model = model;
        this.objectMapper = new ObjectMapper();
        setCachePostRequestsEnabled(true);
    }

    @Override
    public String chat(String message) throws Exception {
        return chat(model, message, (File) null);
    }

    @Override
    public String chat(String model, String message) throws Exception {
        return chat(model, message, (File) null);
    }

    @Override
    public String chat(String model, String message, File imageFile) throws Exception {
        return chat(model, message, imageFile == null ? null : Collections.singletonList(imageFile));
    }

    @Override
    public String chat(String model, String message, List<File> files) throws Exception {
        HashMap<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("bathPath", basePath);
        placeholderValues.put("authorization", authorization);
        placeholderValues.put("model", model);
        placeholderValues.put("message", escapeJsonString(message));
        GenericRequest genericRequest = fromCurlTemplate(curlTemplate, placeholderValues);
        // Replace placeholders in JSON template
        if (bodyTemplate != null && !bodyTemplate.isEmpty()) {
            String jsonBody = replacePlaceholders(bodyTemplate, placeholderValues);

            // Validate JSON (optional, but good practice)
            validateJson(jsonBody);
            genericRequest.setBody(jsonBody);
            // Debug: Print the JSON body for verification
            System.out.println("Prepared JSON Body: " + jsonBody);
        }

        String aiResponse = genericRequest.post();
        return parseResponseByPath(aiResponse, responseJsonPath);
    }

    private String replacePlaceholders(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            template = template.replace(placeholder, entry.getValue());
        }
        return template;
    }

    private void validateJson(String jsonBody) {
        try {
            System.out.println("DEBUG: JSONBODY STARTS");
            System.out.println(jsonBody);
            System.out.println("DEBUG: JSONBODY ENDS");
            JsonParser.parseString(jsonBody);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid JSON after replacement: " + e.getMessage(), e);
        }
    }

    private String escapeJsonString(String input) {
        // Remove surrounding quotes if present
        String trimmedInput = input.replaceAll("^\"|\"$", "");

        try {
            // Try to parse the trimmed input as JSON
            JsonElement jsonElement = JsonParser.parseString(trimmedInput);

            // If it's a valid JSON, we need to return it as is (without additional escaping)
            if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                return trimmedInput;
            } else {
                // If it's not a JSON object or array, treat it as a regular string
                return objectMapper.writeValueAsString(trimmedInput).replaceAll("^\"|\"$", "");
            }
        } catch (JsonSyntaxException e) {
            // If it's not valid JSON, treat it as a regular string
            try {
                return objectMapper.writeValueAsString(trimmedInput).replaceAll("^\"|\"$", "");
            } catch (JsonProcessingException ex) {
                throw new RuntimeException("Failed to escape string", ex);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to escape JSON string", e);
        }
    }

    // Utility method for parsing JSON response using a path
    private String parseResponseByPath(String jsonResponse, String path) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String[] parts = path.split("\\.");
            Object currentObject = jsonObject;

            for (String part : parts) {
                if (part.contains("[")) {
                    String base = part.substring(0, part.indexOf('['));
                    int index = Integer.parseInt(part.substring(part.indexOf('[') + 1, part.indexOf(']')));

                    if (currentObject instanceof JSONObject) {
                        JSONArray array = ((JSONObject) currentObject).getJSONArray(base);
                        currentObject = array.get(index);
                    } else if (currentObject instanceof JSONArray) {
                        currentObject = ((JSONArray) currentObject).get(index);
                    } else {
                        throw new JSONException("Unexpected object type");
                    }
                } else {
                    if (currentObject instanceof JSONObject) {
                        currentObject = ((JSONObject) currentObject).get(part);
                    } else {
                        throw new JSONException("Expected JSONObject, found " + currentObject.getClass().getSimpleName());
                    }
                }
            }

            return currentObject.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Failed to parse response by path: " + path, e);
        }
    }

    @Override
    public String path(String path) {
        return getBasePath() + path;
    }

    @Override
    public Request.Builder sign(Request.Builder builder) {
        return builder;
    }

    private GenericRequest fromCurlTemplate(String curlUrlTemplate, Map<String, String> placeholderValues) {
        // Replace placeholders in the curl template with actual values
        for (Map.Entry<String, String> entry : placeholderValues.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String actualValue = entry.getValue();
            curlUrlTemplate = curlUrlTemplate.replace(placeholder, actualValue);
        }

        // Prepare to parse the command before --data
        List<String> commandParts = new ArrayList<>();
        String dataPart = null;

        // Use regex to separate command components before --data
        Pattern pattern = Pattern.compile("(\"[^\"]*\"|\\S+|--data)");
        Matcher matcher = pattern.matcher(curlUrlTemplate);
        while (matcher.find()) {
            // Check if this is the --data part
            String match = matcher.group();
            if (match.equals("--data")) {
                dataPart = curlUrlTemplate.substring(matcher.end()).trim();
                break;
            } else {
                commandParts.add(match.replace("\"", ""));
            }
        }

        // Debug output
        System.out.println("Curl Command Parts: " + commandParts);
        System.out.println("Data Part: " + dataPart);

        // Extract URL
        if (commandParts.size() < 2) {
            throw new IllegalArgumentException("Invalid curl command format: Missing URL");
        }
        String url = commandParts.get(1);

        // Create an instance of GenericRequest
        GenericRequest request = new GenericRequest(this, url);

        // Parse headers and assign data as body
        for (int i = 2; i < commandParts.size(); i++) {
            if ("-H".equals(commandParts.get(i)) && i + 1 < commandParts.size()) {
                String header = commandParts.get(++i);
                String[] headerParts = header.split(": ", 2);
                if (headerParts.length == 2) {
                    request.header(headerParts[0].trim(), headerParts[1].trim());
                } else {
                    throw new IllegalArgumentException("Invalid header format: " + header);
                }
            }
        }

        // Set the data as the body of the request
        if (dataPart != null) {
            dataPart = dataPart.startsWith("'") && dataPart.endsWith("'")
                    ? dataPart.substring(1, dataPart.length() - 1) : dataPart;
            request.setBody(dataPart);
        }

        return request;
    }

}
