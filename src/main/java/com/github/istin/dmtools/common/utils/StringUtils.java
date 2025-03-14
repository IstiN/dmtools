package com.github.istin.dmtools.common.utils;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<String>();
        if (text == null) {
            return containedUrls;
        }
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)).replace("</a", ""));
        }

        return containedUrls;
    }

    public static String convertToMarkdown(String input) {
        return MarkdownToJiraConverter.convertToJiraMarkdown(input);
    }

    public static String concatenate(String divider, String ... values) {
        StringBuilder resultsBuilder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            resultsBuilder.append(values[i]);
            if (i != values.length - 1) {
                resultsBuilder.append(divider);
            }
        }
        return resultsBuilder.toString();
    }

    public static String concatenate(String divider, Set<String> values) {
        StringBuilder resultsBuilder = new StringBuilder();
        boolean isFirst = true;
        for (String value : values) {
            if (!isFirst) {
                resultsBuilder.append(divider);
            } else {
                isFirst = false;
            }
            resultsBuilder.append(value);
        }
        return resultsBuilder.toString();
    }

    @Nullable
    public static Integer sortByTwoStrings(String firstString, String secondString) {
        // Null check for iterationName and secondIterationName
        if (firstString !=null && secondString !=null) {
            // Sort by iterationName first
            int nameCompare = firstString.compareTo(secondString);
            if(nameCompare != 0) {
                return nameCompare;
            }
        } else if (firstString != null) {
            return -1;
        } else if (secondString != null) {
            return 1;
        }
        return null;
    }

    public static StringBuilder transformJSONToText(StringBuilder textBuilder, JSONObject fields, boolean ignoreDescription) {
        for (String field : fields.keySet()) {
            // Skip values that contain only self links and IDs
            if (field.toLowerCase().contains("id")
                    || field.toLowerCase().contains("url")
                    || field.equalsIgnoreCase("self")
                    || field.equalsIgnoreCase("accountType")
                    || field.equalsIgnoreCase("key")
                    || field.equalsIgnoreCase("statusCategory")
                    || field.equalsIgnoreCase("subtask")
                    || field.equalsIgnoreCase("timeZone")
                    || field.equalsIgnoreCase("hierarchyLevel")
                    || field.equalsIgnoreCase("active")
                    || ignoreDescription && field.equalsIgnoreCase("description")
            ) {
                continue;
            }

            Object fieldValue = fields.get(field);

            // Skip null or empty values
            if (fieldValue == null || fieldValue.toString().trim().isEmpty()) {
                continue;
            }

            // For nested objects, extract relevant text information
            if (fieldValue instanceof JSONObject) {
                textBuilder.append(field).append(": { \n");
                transformJSONToText(textBuilder, (JSONObject) fieldValue, true);
                textBuilder.append("} \n");
            } else if (fieldValue instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) fieldValue;
                if (jsonArray.isEmpty()) {
                    continue;
                }
                textBuilder.append(field).append(": [");
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object arrayElement = jsonArray.get(i);
                    if (arrayElement instanceof JSONObject) {
                        textBuilder.append("{");
                        transformJSONToText(textBuilder, (JSONObject) arrayElement, true);
                        textBuilder.append("}");
                    } else {
                        textBuilder.append(arrayElement.toString());
                    }
                    if (i < jsonArray.length() - 1) {
                        textBuilder.append(", ");
                    }
                }
                textBuilder.append("]\n");
            } else {
                if (!fieldValue.toString().equalsIgnoreCase("null")) {
                    textBuilder.append(field).append(": ").append(fieldValue).append("\n");
                }
            }
        }

        return textBuilder;
    }
}
