package com.github.istin.dmtools.common.model;

import com.github.istin.dmtools.common.tracker.model.Status;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

public interface ITicket extends Key {

    String getStatus() throws IOException;

    Status getStatusModel() throws IOException;

    String getTicketKey();

    String getIssueType() throws IOException;

    String getTicketLink();

    String getPriority() throws IOException;

    String getTicketTitle() throws IOException;

    String getTicketDependenciesDescription();

    Date getCreated();

    JSONObject getFieldsAsJSON();

    Long getUpdatedAsMillis();
}
