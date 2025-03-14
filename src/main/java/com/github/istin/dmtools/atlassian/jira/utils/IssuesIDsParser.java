package com.github.istin.dmtools.atlassian.jira.utils;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssuesIDsParser {
    private static final Logger logger = LogManager.getLogger(IssuesIDsParser.class);
    private Pattern r;

    @Getter
    @Setter
    private IssuesIDsParserParams params;

    public IssuesIDsParser(String... keywords) {
        StringBuilder patternBuilder = new StringBuilder();
        for (String keyword : keywords) {
            if (patternBuilder.length() > 0) {
                patternBuilder.append("|");
            }
            patternBuilder.append(keyword).append("\\d+");
        }
        r = Pattern.compile(patternBuilder.toString());
    }

    public List<String> parseIssues(String... texts) {
        List<String> result = new ArrayList<>();
        for (String text : texts) {
            if (text == null) {
                continue;
            }

            String[] lines = text.split(System.getProperty("line.separator"));
            for (String line : lines) {
                Matcher m = r.matcher(line.trim());
                while (m.find()) {
                    String ticketKey = m.group();
                    ticketKey = transformTicketKey(ticketKey);
                    if (!result.contains(ticketKey)) {
                        result.add(ticketKey);
                    } else {
                        logger.info("double key: {}", ticketKey);
                    }
                }
            }
        }
        return result;
    }

    private String transformTicketKey(String ticketKey) {
        if (params != null) {
            switch (params.getTransformation()) {
                case UPPERCASE:
                    ticketKey = ticketKey.toUpperCase();
                    break;
                case LOWERCASE:
                    ticketKey = ticketKey.toLowerCase();
                    break;
                case NONE:
                default:
                    break;
            }
        }
        if (params != null && params.getReplaceCharacters() != null) {
            for (int i = 0; i < params.getReplaceCharacters().length; i++) {
                ticketKey = ticketKey.replaceAll(params.getReplaceCharacters()[i], params.getReplaceValues()[i]);
            }
        }
        return ticketKey;
    }

    public static Set<String> extractAllJiraIDs(String text) {
        // Enhanced JIRA Key Pattern to match keys in text and URLs
        // It looks for word boundaries or URL prefixes before the JIRA key pattern
        String jiraKeyPattern = "(?:\\b|\\/browse\\/)[A-Z]+-\\d+\\b";

        Set<String> keys = new HashSet<>();
        if (text == null) {
            return keys;
        }

        Pattern pattern = Pattern.compile(jiraKeyPattern);
        Matcher matcher = pattern.matcher(text);


        while (matcher.find()) {
            String found = matcher.group();
            // If the match is part of a URL, extract the JIRA key part after '/browse/'
            if (found.contains("/browse/")) {
                found = found.substring(found.indexOf("/browse/") + 8);
            }
            keys.add(found);
            logger.info("Found JIRA Key: {}", found);
        }
        return keys;
    }

    public static Set<String> extractAttachmentUrls(String basePath, String jiraJson) {
        // Regex pattern to extract filename and content URL
        String pattern = "\"filename\":\"(.*?)\".*?\"content\":\"(https://.*?/attachment/content/\\d+)\"";

        // Compile the regex
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(jiraJson);

        // Set to store unique attachment URLs
        Set<String> attachmentUrls = new HashSet<>();

        // Find matches
        while (matcher.find()) {
            String contentUrl = matcher.group(2); // Captures the content URL
            if (contentUrl.startsWith(basePath)) {
                attachmentUrls.add(contentUrl); // Prepend base path and add to the set
            } else {
                attachmentUrls.add(basePath + contentUrl); // Prepend base path and add to the set
            }
        }

        return attachmentUrls;
    }

    public static Set<String> extractConfluenceUrls(String basePath, String text) {
        // Get the domain part (remove /wiki from basePath)
        String domain = basePath.substring(0, basePath.lastIndexOf("/wiki"));

        // Escape dots and other special characters in the domain for regex
        String escapedDomain = domain.replace(".", "\\.");

        // Create pattern to match both /wiki/ and /l/cp/ URLs
        String pattern = escapedDomain + "/(wiki/|l/cp/)[^\"\\s|\\]\\\\]+";

        System.out.println("Using pattern: " + pattern);

        // Compile the regex
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);

        // Set to store unique URLs
        Set<String> confluenceUrls = new HashSet<>();

        // Find matches
        while (matcher.find()) {
            String url = matcher.group();
            System.out.println("Found match: " + url);

            // Clean up the URL
            url = cleanupUrl(url);

            confluenceUrls.add(url);
        }

        return confluenceUrls;
    }

    private static String cleanupUrl(String url) {
        // Remove everything after the pipe symbol (|) if it exists
        url = url.split("\\|")[0];

        // Remove trailing special characters
        url = url.replaceAll("[\\]\\\\]+$", "");

        // Remove any trailing non-alphanumeric characters except - and _
        url = url.replaceAll("[^\\w\\-/_:.]+$", "");

        return url;
    }

    public static Set<String> extractFigmaUrls(String basePath, String json) {
        // Regex pattern to match Figma URLs
        String pattern = "\"(https://www\\.figma\\.com/[^\"]+)\"";

        // Compile the regex
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(json);

        // Set to store unique Figma URLs
        Set<String> figmaUrls = new HashSet<>();

        // Find matches
        while (matcher.find()) {
            String url = matcher.group(1); // Captures the Figma URL
            figmaUrls.add(url); // Prepend base path and add to the set
        }

        return figmaUrls;
    }
}
