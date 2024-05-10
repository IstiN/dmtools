package com.github.istin.dmtools.common.model;

import com.github.istin.dmtools.atlassian.jira.model.Fields;
import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.atlassian.jira.model.Resolution;
import com.github.istin.dmtools.common.tracker.model.Status;
import org.json.JSONArray;
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

    String getTicketDescription();

    String getTicketDependenciesDescription();

    Date getCreated();

    JSONObject getFieldsAsJSON();

    Long getUpdatedAsMillis();

    IUser getCreator();

    Resolution getResolution();

    JSONArray getTicketLabels();

    Fields getFields();

    double getProgress() throws IOException;

    interface ITicketProgress {

        double calc(ITicket ticket) throws IOException;

        static Integer compare(ITicket original, ITicket second) {
            try {
                double progress = original.getProgress();
                double secondaryProgress = second.getProgress();
                int compare = Double.compare(secondaryProgress, progress);
                if (compare != 0) {
                    return compare;
                }
                return null;
            } catch (IOException e) {
                return null;
            }
        };

        class Impl implements ITicketProgress {

            @Override
            public double calc(ITicket ticket) throws IOException {
                if (IssueType.isBug(ticket.getIssueType())) {
                    Status statusModel = ticket.getStatusModel();
                    if (statusModel.isDone() || statusModel.isRejected() || statusModel.isCancelled()) {
                        return 100;
                    } else if (statusModel.isInDev()) {
                        return 50;
                    } else if (statusModel.isInReview() || statusModel.isCodeReview()) {
                        return 60;
                    } else if (statusModel.isReadyForTesting() || statusModel.isReadyForTest()) {
                        return 80;
                    } else if (statusModel.isInTesting()) {
                        return 90;
                    } else {
                        return 0;
                    }
                } else {
                    Status statusModel = ticket.getStatusModel();
                    if (statusModel == null) {
                        return 0;
                    }
                    if (statusModel.isDone() || statusModel.isRejected() || statusModel.isCancelled()) {
                        return 100;
                    } else if (statusModel.isInAcceptance()) {
                        return 95;
                    } else if (statusModel.isInDev()) {
                        return 50;
                    } else if (statusModel.isInReview() || statusModel.isCodeReview()) {
                        return 60;
                    } else if (statusModel.isReadyForTesting() || statusModel.isReadyForTest()) {
                        return 80;
                    } else if (statusModel.isInTesting()) {
                        return 90;
                    } else {
                        return 0;
                    }
                }
            }

        }

    }
}