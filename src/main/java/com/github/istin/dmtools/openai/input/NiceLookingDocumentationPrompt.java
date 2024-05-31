package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.utils.HtmlCleaner;

public class NiceLookingDocumentationPrompt extends TicketBasedPrompt {

    private String existingContent = "";

    public String getExistingContent() {
        return existingContent;
    }

    public NiceLookingDocumentationPrompt(String basePath, ITicket ticket, String existingContent) {
        super(basePath, ticket);
        this.existingContent = HtmlCleaner.cleanAllHtmlTags(basePath, existingContent);
    }
}
