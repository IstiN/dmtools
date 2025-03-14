package com.github.istin.dmtools.ai;

public class AIProvider {
    private static AI customAI;

    public static void setCustomAI(AI ai) {
        customAI = ai;
    }

    public static AI getCustomAI() {
        return customAI;
    }

    public static void reset() {
        customAI = null;
    }
}