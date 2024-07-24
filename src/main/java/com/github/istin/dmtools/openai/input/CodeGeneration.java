package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.atlassian.bitbucket.model.Commit;
import com.github.istin.dmtools.atlassian.bitbucket.model.File;
import com.github.istin.dmtools.common.model.ITicket;

import java.util.ArrayList;
import java.util.List;

public class CodeGeneration extends TicketBasedPrompt {

    private List<Commit> commits;

    public CodeGeneration(String basePath, String role, ITicket ticket) {
        super(basePath, ticket);
        this.role = role;
    }

    private String role;


    private List<File> files = new ArrayList<>();

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public List<Commit> getCommits() {
        return commits;
    }
}
