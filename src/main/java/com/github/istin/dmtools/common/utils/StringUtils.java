package com.github.istin.dmtools.common.utils;

import com.github.istin.dmtools.common.model.ToText;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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

    public static StringBuilder transformArrayToText(StringBuilder textBuilder, List<? extends ToText> array, boolean ignoreDescription) {
        textBuilder.append("[");
        for (ToText el : array) {
            try {
                textBuilder.append(el.toText());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return textBuilder.append("]");
    }

    public static StringBuilder transformJSONToText(StringBuilder textBuilder, JSONObject fields, boolean ignoreDescription) {
        for (String field : fields.keySet()) {
            // Skip values that contain only self links and IDs
            if (field.toLowerCase().contains("id")
                    || field.toLowerCase().contains("url")
                    || field.equalsIgnoreCase("self")
                    || field.equalsIgnoreCase("accountType")
                    || field.equalsIgnoreCase("statusCategory")
                    || field.equalsIgnoreCase("subtask")
                    || field.equalsIgnoreCase("timeZone")
                    || field.equalsIgnoreCase("hierarchyLevel")
                    || field.equalsIgnoreCase("thumbnail")
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

    /**
     * Checks if the input string matches the expected Confluence YAML format.
     * @param input The input string to check
     * @return true if the input matches the expected format, false otherwise
     */
    public static boolean isConfluenceYamlFormat(String input) {
        // Check if the content has the expected structure
        if (!input.startsWith("<ac:structured-macro")) {
            return false;
        }

        // Check if it's a YAML content by looking for the language parameter
        Pattern languagePattern = Pattern.compile("<ac:parameter ac:name=\"language\">(.*?)</ac:parameter>", Pattern.DOTALL);
        Matcher languageMatcher = languagePattern.matcher(input);

        if (!languageMatcher.find() || !languageMatcher.group(1).equalsIgnoreCase("yaml")) {
            return false;
        }

        // Check if it has CDATA content
        Pattern pattern = Pattern.compile("<ac:plain-text-body><!\\[CDATA\\[(.*?)\\]\\]></ac:plain-text-body>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        return matcher.find();
    }

    /**
     * Extracts YAML content from the input string if it has the expected structure.
     * @param input The input string that may contain YAML content in a specific structure
     * @return The extracted YAML content if found, otherwise the original input
     */
    public static String extractYamlContentFromConfluence(String input) {
        // First check if the input matches the expected format
        if (!isConfluenceYamlFormat(input)) {
            return input;
        }

        // Extract content between CDATA tags
        Pattern pattern = Pattern.compile("<ac:plain-text-body><!\\[CDATA\\[(.*?)\\]\\]></ac:plain-text-body>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        // We already checked that the pattern matches in isConfluenceYamlFormat,
        // so we can safely assume matcher.find() will return true
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            // This should never happen if isConfluenceYamlFormat returned true
            return input;
        }
    }

    /**
     * Removes URLs from text
     */
    public static String removeUrls(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Regex pattern to match URLs
        String urlPattern = "\\b(https?|ftp)://\\S+|\\bwww\\.\\S+";
        return input.replaceAll(urlPattern, "");
    }

    /**
     * Cleans and formats text for markdown tables
     */
    public static String cleanTextForMarkdown(String text) {
        if (text == null) {
            return "";
        }

        return text.replaceAll("[\\r\\n]", " ")
                .replaceAll("\\|", "\\\\|")  // Escape pipe characters
                .replaceAll("\\s+", " ")
                .trim();
    }
}
