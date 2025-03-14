package com.github.istin.dmtools.ai.curl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.model.Metadata;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.utils.HtmlCleaner;
import com.github.istin.dmtools.common.utils.ImageUtils;
import com.github.istin.dmtools.common.utils.RetryUtil;
import com.github.istin.dmtools.networking.AbstractRestClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.Setter;
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
    private final String bodyTemplateWithImage;
    private final String responseJsonPath;
    private String model;
    private final ObjectMapper objectMapper;
    @Setter
    private Metadata metadata;

    public CUrlAIClient(String basePath, String authorization, String curlUrlTemplate, String bodyTemplate, String bodyTemplateWithImage, String responseJsonPath, String model) throws IOException {
        super(basePath, authorization);
        this.curlTemplate = curlUrlTemplate;
        this.bodyTemplate = bodyTemplate;
        this.bodyTemplateWithImage = bodyTemplateWithImage;
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

    /**
     * Converts Base64 image string to valid JSON string value
     * @param base64Image Base64 encoded image string
     * @return JSON-escaped string value
     */
    public static String convertBase64ToJsonValue(String base64Image) {
        if (base64Image == null) {
            return null;
        }

        // Escape special characters for JSON
        return base64Image
                .replace("\\", "\\\\") // escape backslashes
                .replace("\"", "\\\"") // escape quotes
                .replace("\b", "\\b")  // escape backspace
                .replace("\f", "\\f")  // escape form feed
                .replace("\n", "\\n")  // escape new line
                .replace("\r", "\\r")  // escape carriage return
                .replace("\t", "\\t"); // escape tab
    }

    @Override
    public String chat(String model, String message, List<File> files) throws Exception {
        HashMap<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("bathPath", basePath);
        placeholderValues.put("authorization", authorization);
        placeholderValues.put("model", model == null ? this.model : model);
        placeholderValues.put("message", escapeJsonString(message));
        if (files != null && !files.isEmpty()) {
            File file = files.get(0);
            //placeholderValues.put("imageExtension", escapeJsonString("image/png"));
            //placeholderValues.put("imageBase64", escapeJsonString("data:image/png;base64," + ImageUtils.convertToBase64(file, "png")));
        }
        GenericRequest genericRequest = fromCurlTemplate(curlTemplate, placeholderValues);
        // Replace placeholders in JSON template
        if (bodyTemplate != null && !bodyTemplate.isEmpty()) {
            String jsonBody = replacePlaceholders(files == null || files.isEmpty() ? bodyTemplate : bodyTemplateWithImage, placeholderValues);

            validateJson(jsonBody);
            System.out.println("Request to AI: " + jsonBody);
            if (files != null && !files.isEmpty()) {
                JSONObject imageObject = new JSONObject().put("type", "image").put("source", new JSONObject().put("type", "base64").put("media_type", "image/png").put("data",  ImageUtils.convertToBase64(files.get(0), "png")));
                String imageObjectAsString = imageObject.toString();
                jsonBody = jsonBody.replace("\"__IMAGE_OBJECT__\"", imageObjectAsString);
            }
            if (metadata != null) {
                JSONObject jsonObject = new JSONObject(jsonBody);
                jsonObject.put("metadata", new JSONObject(new Gson().toJson(metadata)));
                jsonBody = jsonObject.toString();
            }
            genericRequest.setBody(jsonBody);

        }

        return RetryUtil.executeWithRetry(() -> {
            String aiResponse = genericRequest.post();
            System.out.println("Response From AI: " + aiResponse);
            return parseResponseByPath(aiResponse, responseJsonPath);
        });
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
        JSONObject jsonObject = new JSONObject(jsonResponse);
        Object currentObject = findObjectByPath(path, jsonObject);

        return currentObject.toString();
    }

    private static Object findObjectByPath(String path, JSONObject jsonObject) {
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
        return currentObject;
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
