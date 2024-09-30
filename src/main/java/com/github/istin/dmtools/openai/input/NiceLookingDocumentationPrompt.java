package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.common.utils.HtmlCleaner;

public class NiceLookingDocumentationPrompt extends TicketBasedPrompt {

    private String existingContent = "";

    public String getExistingContent() {
        return existingContent;
    }

    public NiceLookingDocumentationPrompt(String basePath, TicketContext ticketContext, String existingContent) {
        super(basePath, ticketContext);
        this.existingContent = HtmlCleaner.cleanAllHtmlTags(basePath, existingContent);
    }
}
