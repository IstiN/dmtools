package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.ai.TicketContext;

public class QATestCasesPrompt extends TicketBasedPrompt {

    private String testCasesPriorities;

    public QATestCasesPrompt(String basePath, TicketContext ticketContext, String testCasesPriorities) {
        super(basePath, ticketContext);
        this.testCasesPriorities = testCasesPriorities;
    }

    public String getTestCasesPriorities() {
        return testCasesPriorities;
    }
}
