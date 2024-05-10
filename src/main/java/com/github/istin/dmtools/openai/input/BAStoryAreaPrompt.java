package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;

public class BAStoryAreaPrompt extends TicketBasedPrompt {

    private String[] areas;

    public BAStoryAreaPrompt(ITicket ticket, String[] areas) {
        super(ticket);
        this.areas = areas;
    }

    public String[] getAreas() {
        return areas;
    }
}
