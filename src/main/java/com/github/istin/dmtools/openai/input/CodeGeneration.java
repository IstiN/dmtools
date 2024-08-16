package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ICommit;
import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.ITicket;

import java.util.ArrayList;
import java.util.List;

public class CodeGeneration extends TicketBasedPrompt {

    private List<ICommit> commits;

    public CodeGeneration(String basePath, String role, ITicket ticket) {
        super(basePath, ticket);
        this.role = role;
    }

    private String role;


    private List<IFile> files = new ArrayList<>();

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<IFile> getFiles() {
        return files;
    }

    public void setFiles(List<IFile> files) {
        this.files = files;
    }

    public void setCommits(List<ICommit> commits) {
        this.commits = commits;
    }

    public List<ICommit> getCommits() {
        return commits;
    }
}
