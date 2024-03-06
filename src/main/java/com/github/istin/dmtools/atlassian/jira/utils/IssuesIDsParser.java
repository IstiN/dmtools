package com.github.istin.dmtools.atlassian.jira.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssuesIDsParser {

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
                        System.out.println("double key: " + ticketKey);
                    }
                }
            }
        }
        return result;
    }
}
