package com.github.istin.dmtools.openai.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIResponseParser {

    public static boolean parseBooleanResponse(String response) throws IllegalArgumentException {
        // Convert response to lowercase to handle case insensitivity
        String trimmedResponse = response.trim().toLowerCase();

        // Check for a valid boolean value in the response
        if (trimmedResponse.contains("true")) {
            return true;
        } else if (trimmedResponse.contains("false")) {
            return false;
        } else {
            throw new IllegalArgumentException("No valid boolean value found in the response.");
        }
    }

    public static JSONArray parseResponseAsJSONArray(String response) throws JSONException {
        // Find the start position for JSON array
        int startIndex = response.indexOf('[');
        int endIndex = response.lastIndexOf(']');

        // Check if valid indexes are found
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            String jsonString = response.substring(startIndex, endIndex + 1);
            return new JSONArray(jsonString);
        } else {
            throw new JSONException("No valid JSON array found in the response.");
        }
    }

    public static JSONObject parseResponseAsJSONObject(String response) throws JSONException {
        // Find the start position for JSON object
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');

        // Check if valid indexes are found
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            String jsonString = response.substring(startIndex, endIndex + 1);
            return new JSONObject(jsonString);
        } else {
            throw new JSONException("No valid JSON object found in the response.");
        }
    }

    public static List<String> parseCodeExamples(String response, String startDelimiter, String endDelimiter) throws IllegalArgumentException {
        List<String> codeExamples = new ArrayList<>();
        if (response == null || response.isEmpty()) {
            throw new IllegalArgumentException("Response text cannot be null or empty.");
        }
        if (startDelimiter == null || endDelimiter == null) {
            throw new IllegalArgumentException("Delimiters cannot be null.");
        }

        // Pattern to match code blocks including the language identifier
        Pattern pattern = Pattern.compile(Pattern.quote(startDelimiter) + "(?:\\w+\\s*)?(.*?)" + Pattern.quote(endDelimiter), Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            String codeBlock = matcher.group(1).trim();

            // Remove any leading newline if present
            if (codeBlock.startsWith("\n")) {
                codeBlock = codeBlock.substring(1);
            }

            // Remove any trailing newline if present
            if (codeBlock.endsWith("\n")) {
                codeBlock = codeBlock.substring(0, codeBlock.length() - 1);
            }

            // Clean markdowns from start and end
            codeBlock = cleanMarkdowns(codeBlock);

            if (!codeBlock.isEmpty()) {
                codeExamples.add(codeBlock);
            }
        }

        if (codeExamples.isEmpty()) {
            throw new IllegalArgumentException("No valid code blocks found between the specified delimiters.");
        }

        return codeExamples;
    }

    private static String cleanMarkdowns(String codeBlock) {
        // Remove markdown code block indicators from start and end
        codeBlock = codeBlock.replaceAll("^```\\w*\\s*", "");
        codeBlock = codeBlock.replaceAll("```\\s*$", "");

        // Remove any leading or trailing whitespace
        return codeBlock.trim();
    }

    public static List<String> parseCodeExamples(String response) {
        return parseCodeExamples(response, "@jai_generated_code", "@jai_generated_code");
    }

}
