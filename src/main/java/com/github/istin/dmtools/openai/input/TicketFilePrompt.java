package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.atlassian.bitbucket.model.File;
import com.github.istin.dmtools.common.model.ITicket;

public class TicketFilePrompt extends TicketBasedPrompt {

    private final String role;
    private File file;

    public TicketFilePrompt(String basePath, String role, ITicket ticket, File file) {
        super(basePath, ticket);
        this.role = role;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getRole() {
        return role;
    }
}
