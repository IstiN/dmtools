package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;

import java.util.ArrayList;
import java.util.List;

public class TicketBasedPrompt {

    private ITicket ticket;

    private List<? extends ITicket> testCases = new ArrayList<>();

    public TicketBasedPrompt(ITicket ticket) {
        this.ticket = ticket;
    }

    public ITicket getTicket() {
        return ticket;
    }

    public void setTicket(ITicket ticket) {
        this.ticket = ticket;
    }


    public List<? extends ITicket> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<? extends ITicket> testCases) {
        this.testCases = testCases;
    }

}
