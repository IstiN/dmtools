package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;

import java.util.List;

public class BASimilarStoriesPrompt extends TicketBasedPrompt {

    private List<? extends ITicket> stories;

    private ITicket similarTicket;

    private String role;

    public BASimilarStoriesPrompt(ITicket ticket, List<? extends ITicket> stories) {
        super(ticket);
        this.stories = stories;
    }

    public BASimilarStoriesPrompt(String role, ITicket ticket, ITicket similarTicket) {
        super(ticket);
        this.similarTicket = similarTicket;
        this.role = role;
    }

    public List<? extends ITicket> getStories() {
        return stories;
    }

    public ITicket getSimilarTicket() {
        return similarTicket;
    }

    public void setSimilarTicket(ITicket similarTicket) {
        this.similarTicket = similarTicket;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
