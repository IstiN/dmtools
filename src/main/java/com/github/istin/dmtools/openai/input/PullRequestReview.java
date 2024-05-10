package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;

import java.util.List;

public class PullRequestReview extends TicketBasedPrompt {

    public PullRequestReview(String role, ITicket ticket) {
        super(ticket);
        this.role = role;
    }

    private String role;

    private String diff;

    private List<? extends ITicket> testCases;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public List<? extends ITicket> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<? extends ITicket> testCases) {
        this.testCases = testCases;
    }
}
