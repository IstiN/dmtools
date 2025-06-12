package com.github.istin.dmtools.github.model;

import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IUser;
import org.junit.Test;
import org.mockito.Mockito;
import org.json.JSONObject;
import org.json.JSONException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class GitHubActivityTest {

    @Test
    public void testGetAction() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("state", "APPROVED");
        GitHubActivity activity = new GitHubActivity(json);

        String action = activity.getAction();

        assertEquals("APPROVED", action);
    }

    @Test
    public void testGetComment() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("body", "This is a comment");
        json.put("id", 12345);
        json.put("user", new JSONObject());

        GitHubActivity activity = new GitHubActivity(json);
        IComment comment = activity.getComment();

        assertNotNull(comment);
        assertEquals("This is a comment", comment.getBody());
        assertEquals("12345", comment.getId());
        assertNotNull(comment.getAuthor());
    }

    @Test
    public void testGetApproval() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("user", new JSONObject());

        GitHubActivity activity = new GitHubActivity(json);
        IUser approval = activity.getApproval();

        assertNotNull(approval);
    }
}