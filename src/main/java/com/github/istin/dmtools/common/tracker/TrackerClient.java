package com.github.istin.dmtools.common.tracker;

import com.github.istin.dmtools.atlassian.confluence.ContentUtils;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.common.model.IChangelog;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.timeline.ReportIteration;
import com.github.istin.dmtools.common.utils.HtmlCleaner;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface TrackerClient<T extends ITicket> extends ContentUtils.UrlToImageFile {

    String linkIssueWithRelationship(String sourceKey, String anotherKey, String relationship) throws IOException;

    enum TextType {
        HTML, MARKDOWN
    }

    String updateDescription(String key, String description) throws IOException;

    String buildUrlToSearch(String query);

    String getBasePath();

    String getTicketBrowseUrl(String ticketKey);

    String assignTo(String ticketKey, String userName) throws IOException;

    IChangelog getChangeLog(String ticketKey, ITicket ticket) throws IOException;

    void deleteLabelInTicket(T ticket, String label) throws IOException;

    void addLabelIfNotExists(ITicket ticket, String label) throws IOException;

    String createTicketInProject(String project, String issueType, String summary, String description, FieldsInitializer fieldsInitializer) throws IOException;

    List<T> searchAndPerform(String searchQuery, String[] fields) throws Exception;

    void searchAndPerform(JiraClient.Performer<T> performer, String searchQuery, String[] fields) throws Exception;

    T performTicket(String ticketKey, String[] fields) throws IOException;

    void postCommentIfNotExists(String ticketKey, String comment) throws IOException;

    List<? extends IComment> getComments(String ticketKey, T ticket) throws IOException;

    void postComment(String ticketKey, String comment) throws IOException;

    void deleteCommentIfExists(String ticketKey, String comment) throws IOException;

    String moveToStatus(String ticketKey, String statusName) throws IOException;

    String[] getDefaultQueryFields();

    String[] getExtendedQueryFields();

    String getDefaultStatusField();

    List<? extends ITicket> getTestCases(ITicket ticket) throws IOException;

    void setLogEnabled(boolean isLogEnabled);

    void setCacheGetRequestsEnabled(boolean isCacheOfGetRequestsEnabled);

    List<? extends ReportIteration> getFixVersions(String projectCode) throws IOException;

    TextType getTextType();

    void attachFileToTicket(String ticketKey, String name, String contentType, File file) throws IOException;

    interface TrackerTicketFields {
        void set(String key, Object object);
    }

    interface FieldsInitializer {

        void init(TrackerTicketFields fields);

    }

    class Utils {
        public static String checkCommentStartedWith(TrackerClient trackerClient, String key, ITicket ticket, String commentPrefix) throws IOException {
            List<IComment> comments = trackerClient.getComments(key, ticket);
            for (IComment comment : comments) {
                String cleanedComment = HtmlCleaner.cleanAllHtmlTags("", comment.getBody());
                String cleanedPrefix = HtmlCleaner.cleanAllHtmlTags("", commentPrefix);
                if (comment.getBody().startsWith(commentPrefix) || comment.getBody().startsWith("<p>"+commentPrefix) || cleanedComment.startsWith(cleanedPrefix)) {
                    return comment.getBody();
                }
            }
            return null;
        }

        public static boolean isLabelExists(ITicket ticket, String label) {
            JSONArray ticketLabels = ticket.getTicketLabels();
            if (ticketLabels == null) {
                return false;
            }
            for (int i = 0; i < ticketLabels.length(); i++) {
                String labelFromArray = ticketLabels.getString(i);
                if (labelFromArray.equalsIgnoreCase(label)) {
                    return true;
                }
            }
            return false;
        }
    }

}
