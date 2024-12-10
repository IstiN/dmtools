package com.github.istin.dmtools.github;

import com.github.istin.dmtools.common.model.*;
import okhttp3.Request;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class GitHubTest {

    private GitHub gitHub;
    private static final String BASE_PATH = "https://api.github.com";
    private static final String AUTHORIZATION = "token";

    @Before
    public void setUp() throws IOException {
        gitHub = Mockito.mock(GitHub.class, withSettings().useConstructor(BASE_PATH, AUTHORIZATION).defaultAnswer(CALLS_REAL_METHODS));
    }

    @Test
    public void testPath() {
        String path = "repos/test/repo";
        String expected = BASE_PATH + "/" + path;
        assertEquals(expected, gitHub.path(path));
    }

    @Test
    public void testSign() {
        Request.Builder builder = new Request.Builder();
        Request.Builder signedBuilder = gitHub.sign(builder);
        assertNotNull(signedBuilder);
    }


    @Test
    public void testTriggerAction() throws IOException {
        String workspace = "testWorkspace";
        String repository = "testRepo";
        JSONObject params = new JSONObject();

        String response = gitHub.triggerAction(workspace, repository, params);
        assertNotNull(response);
    }


    @Test
    public void testAddPullRequestComment() throws IOException {
        String workspace = "testWorkspace";
        String repository = "testRepo";
        String pullRequestId = "1";
        String text = "Test comment";

        String response = gitHub.addPullRequestComment(workspace, repository, pullRequestId, text);
        assertNotNull(response);
    }


    @Test
    public void testAddPullRequestLabel() throws IOException {
        String workspace = "testWorkspace";
        String repository = "testRepo";
        String pullRequestId = "1";
        String label = "bug";

        gitHub.addPullRequestLabel(workspace, repository, pullRequestId, label);
    }



    @Test
    public void testGetPullRequestDiff() throws IOException {
        String workspace = "testWorkspace";
        String repository = "testRepo";
        String pullRequestID = "1";

        IDiffStats diffStats = gitHub.getPullRequestDiff(workspace, repository, pullRequestID);
        assertNotNull(diffStats);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDefaultRepository() {
        gitHub.getDefaultRepository();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDefaultBranch() {
        gitHub.getDefaultBranch();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDefaultWorkspace() {
        gitHub.getDefaultWorkspace();
    }

    @Test
    public void testGetPullRequestUrl() {
        String workspace = "testWorkspace";
        String repository = "testRepo";
        String id = "1";

        String url = gitHub.getPullRequestUrl(workspace, repository, id);
        assertNotNull(url);
    }
}