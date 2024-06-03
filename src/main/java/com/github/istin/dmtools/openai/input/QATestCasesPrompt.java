package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ITicket;

public class QATestCasesPrompt extends TicketBasedPrompt {

    private String testCasesPriorities;

    public QATestCasesPrompt(String basePath, ITicket ticket, String testCasesPriorities) {
        super(basePath, ticket);
        this.testCasesPriorities = testCasesPriorities;
    }

    public String getTestCasesPriorities() {
        return testCasesPriorities;
    }
}
