package com.github.istin.dmtools.presale.model;

import java.util.Map;

public class StoryEstimation {
    public String title;
    public String link;
    public Map<String, Estimation> estimations;

    public StoryEstimation(String title, String link, Map<String, Estimation> estimations) {
        this.title = title;
        this.link = link;
        this.estimations = estimations;
    }
}
