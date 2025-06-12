package com.github.istin.dmtools.ai;

import lombok.Getter;

public class AIProvider {
    @Getter
    private static AI customAI;

    public static void reset() {
        customAI = null;
    }

    public static void setCustomAI(AI ai) {
        customAI = ai;
    }
}