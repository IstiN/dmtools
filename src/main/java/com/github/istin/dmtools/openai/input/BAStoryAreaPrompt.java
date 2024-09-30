package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.ai.TicketContext;

public class BAStoryAreaPrompt extends TicketBasedPrompt {

    private String areas;

    public BAStoryAreaPrompt(String basePath, TicketContext ticketContext, String areas) {
        super(basePath, ticketContext);
        this.areas = areas;
    }

    public String getAreas() {
        return areas;
    }
}
