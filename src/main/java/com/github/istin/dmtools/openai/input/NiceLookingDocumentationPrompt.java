package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;

public class NiceLookingDocumentationPrompt extends TicketBasedPrompt {

    private String existingContent = "";

    public String getExistingContent() {
        return existingContent;
    }

    public NiceLookingDocumentationPrompt(ITicket ticket, String existingContent) {
        super(ticket);
        this.existingContent = existingContent;
    }
}
