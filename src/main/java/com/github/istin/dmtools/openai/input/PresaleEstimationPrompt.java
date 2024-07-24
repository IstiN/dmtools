package com.github.istin.dmtools.openai.input;

import java.util.List;

public class PresaleEstimationPrompt {

    public final String input;

    public final List<String> platforms;

    public String getInput() {
        return input;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public PresaleEstimationPrompt(String input, List<String> platforms) {
        this.input = input;
        this.platforms = platforms;
    }
}
