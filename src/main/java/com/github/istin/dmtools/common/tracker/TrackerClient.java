package com.github.istin.dmtools.common.tracker;

import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.model.Comment;
import com.github.istin.dmtools.common.model.IChangelog;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.ITicket;

import java.io.IOException;
import java.util.List;

public interface TrackerClient<T extends ITicket> {

    String getBasePath();

    String getTicketBrowseUrl(String ticketKey);

    String assignTo(String ticketKey, String userName) throws IOException;

    IChangelog getChangeLog(String ticketKey, ITicket ticket) throws IOException;

    void addLabelIfNotExists(T ticket, String label) throws IOException;

    List<T> searchAndPerform(String searchQuery, String[] fields) throws Exception;

    void searchAndPerform(JiraClient.Performer<T> performer, String searchQuery, String[] fields) throws Exception;

    T performTicket(String ticketKey, String[] fields) throws IOException;

    void postCommentIfNotExists(String ticketKey, String comment) throws IOException;

    List<? extends IComment> getComments(String ticketKey, T ticket) throws IOException;

    void postComment(String ticketKey, String comment) throws IOException;

    String moveToStatus(String ticketKey, String statusName) throws IOException;

    String[] getDefaultQueryFields();

    String getDefaultStatusField();
}
