package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;

public class BAStoryAreaPrompt extends TicketBasedPrompt {

    private String areas;

    public BAStoryAreaPrompt(String basePath, ITicket ticket, String areas) {
        super(basePath, ticket);
        this.areas = areas;
    }

    public String getAreas() {
        return areas;
    }
}
