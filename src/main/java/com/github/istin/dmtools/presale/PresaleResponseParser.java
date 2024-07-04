package com.github.istin.dmtools.presale;

import com.github.istin.dmtools.presale.model.Estimation;
import com.github.istin.dmtools.presale.model.StoryEstimation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PresaleResponseParser {

    public static List<StoryEstimation> parse(String text) {
        List<StoryEstimation> storyEstimations = new ArrayList<>();
        String[] lines = text.split("\n");
        String currentTitle = "";
        Estimation androidEstimation = null;
        Estimation iosEstimation = null;
        Estimation flutterEstimation = null;
        Estimation reactEstimation = null;
        Estimation backendEstimation = null;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("**Story:")) {
                currentTitle = lines[i].replaceAll("^\\*\\*\\d+\\.", "").replaceAll("\\*", "").replaceAll("Story:", "").trim();

                for (int j = 1; j <= 5; j++) {
                    Matcher m = Pattern.compile("- (.+): (\\d+)\\|(\\d+)\\|(\\d+)").matcher(lines[i + j].trim());
                    if (m.find()) {
                        int optimistic = Integer.parseInt(m.group(2));
                        int pessimistic = Integer.parseInt(m.group(3));
                        int mostLikely = Integer.parseInt(m.group(4));

                        switch (m.group(1)) {
                            case "Android Native":
                                androidEstimation = new Estimation(optimistic, pessimistic, mostLikely);
                                break;
                            case "iOS Native":
                                iosEstimation = new Estimation(optimistic, pessimistic, mostLikely);
                                break;
                            case "Flutter":
                                flutterEstimation = new Estimation(optimistic, pessimistic, mostLikely);
                                break;
                            case "React Native":
                                reactEstimation = new Estimation(optimistic, pessimistic, mostLikely);
                                break;
                            case "Backend":
                                backendEstimation = new Estimation(optimistic, pessimistic, mostLikely);
                                break;
                        }
                    }
                }
                storyEstimations.add(new StoryEstimation(currentTitle, androidEstimation, iosEstimation, flutterEstimation, reactEstimation, backendEstimation));
                i += 5; // Skip next five lines for estimations
            }
        }

        return storyEstimations;
    }
}
