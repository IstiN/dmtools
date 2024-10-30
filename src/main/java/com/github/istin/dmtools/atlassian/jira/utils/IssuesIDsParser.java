package com.github.istin.dmtools.atlassian.jira.utils;

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
}
