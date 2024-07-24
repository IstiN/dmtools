package com.github.istin.dmtools.presale;

import com.github.istin.dmtools.presale.model.Estimation;
import com.github.istin.dmtools.presale.model.StoryEstimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PresaleResponseParser {

    public static List<StoryEstimation> parse(String text, String link, List<String> platforms) {
        List<StoryEstimation> storyEstimations = new ArrayList<>();
        String[] lines = Arrays.stream(text.split("\n"))
                .filter(line -> line != null && line.trim().length() > 0)
                .toArray(String[]::new);
        String currentTitle = "";

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("**Story:")) {
                currentTitle = lines[i].replaceAll("^\\*\\*\\d+\\.", "").replaceAll("\\*", "").replaceAll("Story:", "").trim();

                Map<String, Estimation> estimations = new HashMap<>();
                int platformCount = platforms.size();

                for (int j = 1; j <= platformCount; j++) {
                    Matcher m = Pattern.compile("- (.+): (\\d+)\\|(\\d+)\\|(\\d+)").matcher(lines[i + j].trim());

                    if (m.find()) {
                        Estimation e = new Estimation(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
                        estimations.put(m.group(1), e);
                    }
                }
                storyEstimations.add(new StoryEstimation(currentTitle, link, estimations));
                i += platformCount;
            }
        }

        return storyEstimations;
    }
}
