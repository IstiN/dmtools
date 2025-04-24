package com.github.istin.dmtools.ai;

import lombok.Getter;
import lombok.Setter;

public class AIProvider {
    @Setter
    @Getter
    private static AI customAI;

    public static void reset() {
        customAI = null;
    }

    public static void setCustomAI(AI ai) {
        customAI = ai;
    }
}