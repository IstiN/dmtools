package com.github.istin.dmtools.gitlab;

import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.gitlab.model.GitLabPullRequest;
import com.github.istin.dmtools.gitlab.model.GitLabComment;
import com.github.istin.dmtools.gitlab.model.GitLabTag;
import com.github.istin.dmtools.gitlab.model.GitLabProject;
import com.github.istin.dmtools.gitlab.model.GitLabCommit;
import com.github.istin.dmtools.gitlab.model.GitLabFile;
import com.github.istin.dmtools.gitlab.model.GitLabJob;
import com.github.istin.dmtools.common.networking.GenericRequest;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GitLabTest {

    private GitLab gitLab;
    private final String basePath = "http://example.com";

    @Before
    public void setUp() throws IOException {
        gitLab = Mockito.mock(GitLab.class, Mockito.CALLS_REAL_METHODS);
        doReturn(basePath).when(gitLab).getBasePath();
    }

    @Test
    public void testPath() {
        String expectedPath = basePath + "/api/v4/testPath";
        assertEquals(expectedPath, gitLab.path("testPath"));
    }

    @Test
    public void testSign() {
        Request.Builder builder = new Request.Builder();
        Request.Builder signedBuilder = gitLab.sign(builder);
        assertNotNull(signedBuilder);
    }


    @Test
    public void testAddPullRequestLabel() throws IOException {
        try {
            gitLab.addPullRequestLabel("workspace", "repository", "1", "label");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }


    @Test
    public void testPullRequestTasks() throws IOException {
        try {
            gitLab.pullRequestTasks("workspace", "repository", "1");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }

    @Test
    public void testAddTask() throws IOException {
        try {
            gitLab.addTask(1, "task");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }

    @Test
    public void testCreatePullRequestCommentAndTaskIfNotExists() throws IOException {
        try {
            gitLab.createPullRequestCommentAndTaskIfNotExists("workspace", "repository", "1", "comment", "task");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }

    @Test
    public void testCreatePullRequestCommentAndTask() throws IOException {
        try {
            gitLab.createPullRequestCommentAndTask("workspace", "repository", "1", "comment", "task");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetDefaultRepository() {
        try {
            gitLab.getDefaultRepository();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetDefaultBranch() {
        try {
            gitLab.getDefaultBranch();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetDefaultWorkspace() {
        try {
            gitLab.getDefaultWorkspace();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }


    @Test
    public void testGetPullRequestUrl() {
        String url = gitLab.getPullRequestUrl("workspace", "repository", "1");
        assertNotNull(url);
    }
}