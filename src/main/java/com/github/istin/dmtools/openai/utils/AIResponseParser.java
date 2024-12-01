package com.github.istin.dmtools.openai.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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


}
