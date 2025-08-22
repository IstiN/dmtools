package com.github.istin.dmtools.common.utils;

import com.github.istin.dmtools.common.model.ToText;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    // Pre-compiled field blacklist for performance optimization
    private static final Set<String> FIELD_BLACKLIST = new HashSet<>();
    static {
        // Initialize blacklist once to avoid repeated string operations
        FIELD_BLACKLIST.add("id");
        FIELD_BLACKLIST.add("url");
        FIELD_BLACKLIST.add("self");
        FIELD_BLACKLIST.add("accounttype");
        FIELD_BLACKLIST.add("statuscategory");
        FIELD_BLACKLIST.add("subtask");
        FIELD_BLACKLIST.add("timezone");
        FIELD_BLACKLIST.add("hierarchylevel");
        FIELD_BLACKLIST.add("thumbnail");
        FIELD_BLACKLIST.add("active");
        // Add common variations to avoid toLowerCase() calls
        FIELD_BLACKLIST.add("ID");
        FIELD_BLACKLIST.add("URL");
        FIELD_BLACKLIST.add("Self");
        FIELD_BLACKLIST.add("AccountType");
        FIELD_BLACKLIST.add("StatusCategory");
        FIELD_BLACKLIST.add("SubTask");
        FIELD_BLACKLIST.add("TimeZone");
        FIELD_BLACKLIST.add("HierarchyLevel");
        FIELD_BLACKLIST.add("Thumbnail");
        FIELD_BLACKLIST.add("Active");
    }

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

    /**
     * High-performance JSON to text transformation with optimized field filtering and string operations.
     * Performance optimizations:
     * - Pre-compiled field blacklist to avoid repeated string operations
     * - Efficient null/empty checks
     * - Optimized StringBuilder usage
     * - Reduced string creation and comparison overhead
     */
    public static StringBuilder transformJSONToText(StringBuilder textBuilder, JSONObject fields, boolean ignoreDescription) {
        if (fields == null || fields.length() == 0) {
            return textBuilder;
        }

        // Use direct field iteration for better performance
        for (String field : fields.keySet()) {
            // Fast blacklist check - avoid expensive string operations
            if (isFieldBlacklisted(field, ignoreDescription)) {
                continue;
            }

            Object fieldValue = fields.opt(field); // Use opt() for better null handling
            
            // Fast null/empty check
            if (isValueEmpty(fieldValue)) {
                continue;
            }

            // Process field value based on type with optimized operations
            if (fieldValue instanceof JSONObject) {
                processJSONObject(textBuilder, field, (JSONObject) fieldValue);
            } else if (fieldValue instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) fieldValue;
                if (jsonArray.length() > 0) { // Avoid isEmpty() method call
                    processJSONArray(textBuilder, field, jsonArray);
                }
            } else {
                // Direct string append without unnecessary toString() calls
                String valueStr = String.valueOf(fieldValue);
                if (!"null".equals(valueStr)) { // Direct comparison
                    textBuilder.append(field).append(": ").append(valueStr).append('\n');
                }
            }
        }

        return textBuilder;
    }

    /**
     * Optimized field blacklist check to avoid expensive string operations
     */
    private static boolean isFieldBlacklisted(String field, boolean ignoreDescription) {
        // Fast exact match check first (most common case)
        if (FIELD_BLACKLIST.contains(field)) {
            return true;
        }
        
        // Check for id/url substring only if exact match fails
        String lowerField = field.toLowerCase();
        if (lowerField.contains("id") || lowerField.contains("url")) {
            return true;
        }
        
        // Description check
        return ignoreDescription && "description".equalsIgnoreCase(field);
    }

    /**
     * Fast null/empty value check
     */
    private static boolean isValueEmpty(Object value) {
        if (value == null) {
            return true;
        }
        
        // Avoid toString() call for known empty cases
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        
        return false;
    }

    /**
     * Optimized JSON object processing
     */
    private static void processJSONObject(StringBuilder textBuilder, String field, JSONObject jsonObject) {
        textBuilder.append(field).append(": { \n");
        transformJSONToText(textBuilder, jsonObject, true);
        textBuilder.append("} \n");
    }

    /**
     * Optimized JSON array processing with reduced string operations
     */
    private static void processJSONArray(StringBuilder textBuilder, String field, JSONArray jsonArray) {
        textBuilder.append(field).append(": [");
        
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            Object arrayElement = jsonArray.opt(i);
            
            if (arrayElement instanceof JSONObject) {
                textBuilder.append('{');
                transformJSONToText(textBuilder, (JSONObject) arrayElement, true);
                textBuilder.append('}');
            } else if (arrayElement != null) {
                textBuilder.append(arrayElement);
            }
            
            // Avoid unnecessary comparison on last element
            if (i < length - 1) {
                textBuilder.append(", ");
            }
        }
        textBuilder.append("]\n");
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
