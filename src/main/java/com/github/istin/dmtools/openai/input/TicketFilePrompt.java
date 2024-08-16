package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.ITicket;

public class TicketFilePrompt extends TicketBasedPrompt {

    private final String role;
    private IFile file;

    public TicketFilePrompt(String basePath, String role, ITicket ticket, IFile file) {
        super(basePath, ticket);
        this.role = role;
        this.file = file;
    }

    public IFile getFile() {
        return file;
    }

    public void setFile(IFile file) {
        this.file = file;
    }

    public String getRole() {
        return role;
    }
}
