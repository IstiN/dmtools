package com.github.istin.dmtools.common.model;

import com.github.istin.dmtools.atlassian.jira.model.Status;

import java.io.IOException;

public interface IBlocker extends Key {

    String getStatus() throws IOException;

    Status getStatusModel() throws IOException;

    String getTicketKey();

    String getIssueType() throws IOException;

    String getTicketLink();

    String getPriority() throws IOException;

    String getTicketTitle() throws IOException;

    String getTicketDependenciesDescription();

}
