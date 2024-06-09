package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.utils.HtmlCleaner;

import java.util.ArrayList;
import java.util.List;

public class MultiTicketsPrompt extends TicketBasedPrompt {

    private String role;
    private String projectSpecifics;
    private List<ITicket> extraTickets;

    private String existingContent = "";

    public String getExistingContent() {
        return existingContent;
    }

    public MultiTicketsPrompt(String basePath, String role, String projectSpecifics, ITicket ticket, List<ITicket> extraTickets, String existingContent) {
        super(basePath, ticket);
        this.role = role;
        this.projectSpecifics = projectSpecifics;
        this.extraTickets = extraTickets;
        this.existingContent = HtmlCleaner.cleanAllHtmlTags(basePath, existingContent);
    }

    public ITicket getContent() {
        return content;
    }

    public void setContent(ITicket content) {
        this.content = content;
    }

    private ITicket content;

    public MultiTicketsPrompt(String basePath, String role, String projectSpecifics, ITicket ticket, List<ITicket> extraTickets) {
        super(basePath, ticket);
        this.role = role;
        this.projectSpecifics = projectSpecifics;
        this.extraTickets = new ArrayList<>();
        for (ITicket extraTicket : extraTickets) {
            this.extraTickets.add(new TicketWrapper(extraTicket));
        }
    }

    public String getRole() {
        return role;
    }

    public String getProjectSpecifics() {
        return projectSpecifics;
    }

    public List<ITicket> getExtraTickets() {
        return extraTickets;
    }
}
