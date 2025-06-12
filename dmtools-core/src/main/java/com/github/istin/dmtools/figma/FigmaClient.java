package com.github.istin.dmtools.figma;

import com.github.istin.dmtools.atlassian.confluence.ContentUtils;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.utils.ImageUtils;
import com.github.istin.dmtools.context.UriToObject;
import com.github.istin.dmtools.figma.model.FigmaComment;
import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.Request;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public class FigmaClient extends AbstractRestClient implements ContentUtils.UrlToImageFile, UriToObject {

    private static final Logger logger = LogManager.getLogger(FigmaClient.class);

    public FigmaClient(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
    }

    @Override
    public String path(String path) {
        return getBasePath() + path;
    }

    @Override
    public Request.Builder sign(Request.Builder builder) {
        return builder
                .header("X-Figma-Token", authorization)
                .header("Content-Type", "application/json");
    }

    @Override
    public int getTimeout() {
        return 300;
    }


    public String getImageOfSource(String url) throws Exception {
        GenericRequest getRequest = new GenericRequest(this, path("images/"+ parseFileId(url)));
        String nodeId = extractValueByParameter(url, "node-id");
        getRequest.param("ids", nodeId);
        try {
            String response = execute(getRequest);
            logger.info(response);
//            TODO to think about styling
//             getRequest = new GenericRequest(this, path("files/"+ parseFileId(url)+ "/styles"));
//            getRequest.param("ids", nodeId);
//            logger.info(getRequest.execute());
            return new JSONObject(response).getJSONObject("images").optString(nodeId.replaceAll("-", ":"));
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public File downloadImage(String url) throws IOException {
        GenericRequest genericRequest = new GenericRequest(this, url);
        return Impl.downloadFile(this, genericRequest, getCachedFile(genericRequest));
    }

    @Override
    public File getCachedFile(GenericRequest genericRequest) {
        String url = genericRequest.url();
        String value = DigestUtils.md5Hex(url);
        return new File(getCacheFolderName() + "/" + value + ((url.contains("images") ? ".png" : "")));
    }

    public String downloadImageAsBase64(String path) throws IOException {
        File imageFile = downloadImage(path);
        return ImageUtils.convertToBase64(imageFile, "png");
    }

    protected String parseFileId(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();

            // Extract the file ID from the path
            String[] pathSegments = path.split("/");
            return pathSegments[2]; // Assuming the file ID is always at index 2
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String extractValueByParameter(String url, String paramName) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();

            // Split the query parameters
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];

                    if (key.equals(paramName)) {
                        return value;
                    }
                }
            }
            throw new UnsupportedOperationException("Invalid url");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isValidImageUrl(String url) {
        boolean isDesignUrl = url.contains("design");
        boolean isFigmaLink = url.contains("figma") && url.contains("file") || isDesignUrl;
        try {
            if (isFigmaLink) {
                if (!isDesignUrl) {
                    parseFileId(url);
                }
                extractValueByParameter(url, "node-id");
            }
        } catch (Exception ignored) {
            return false;
        }

        return isFigmaLink;
    }

    @Override
    public File convertUrlToFile(String href) throws Exception {
        href = href.replaceAll("&amp;", "&");
        String imageOfSource = getImageOfSource(href);
        if (imageOfSource == null || !imageOfSource.startsWith("http")) {
            return null;
        }
        return downloadImage(imageOfSource);
    }

    // Mock method to get all teams
    public JSONArray getAllTeams() throws Exception {
        String url = path("teams");  // Replace with actual endpoint
        String response = execute(new GenericRequest(this, url));
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getJSONArray("teams");
    }

    public void getAllCommentsForAllTeams() throws Exception {
        JSONArray teams = getAllTeams();
        for (int i = 0; i < teams.length(); i++) {
            JSONObject team = teams.getJSONObject(i);
            String teamId = team.getString("id");
            logger.info("Processing team: " + team.getString("name"));

            // Call getAllCommentsForTeam for each team
            getAllCommentsForTeam(teamId);
        }
    }

    public void getAllCommentsForTeam(String teamId) throws Exception {
        JSONArray projects = getProjects(teamId);
        for (int i = 0; i < projects.length(); i++) {
            JSONObject project = projects.getJSONObject(i);
            String projectId = project.getString("id");
            JSONArray files = getFiles(projectId);

            for (int j = 0; j < files.length(); j++) {
                JSONObject file = files.getJSONObject(j);
                String fileKey = file.getString("key");
                List<IComment> comments = getComments(fileKey);

                for (int k = 0; k < comments.size(); k++) {
                    IComment comment = comments.get(k);
                    logger.info("Comment: " + comment.toString());
                }
            }
        }
    }

    // Get all projects within a team
    public JSONArray getProjects(String teamId) throws Exception {
        String url = path("teams/" + teamId + "/projects");
        String response = execute(new GenericRequest(this, url));
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getJSONArray("projects");
    }

    // Get all files within a project
    public JSONArray getFiles(String projectId) throws Exception {
        String url = path("projects/" + projectId + "/files");
        String response = execute(new GenericRequest(this, url));
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getJSONArray("files");
    }

    // Get all comments in a file
    public List<IComment> getComments(String fileKey) throws Exception {
        String url = path("files/" + fileKey + "/comments");
        String response = execute(new GenericRequest(this, url));
        JSONObject jsonResponse = new JSONObject(response);
        return JSONModel.convertToModels(FigmaComment.class, jsonResponse.getJSONArray("comments"));
    }

    @Override
    public Set<String> parseUris(String object) throws Exception {
        return IssuesIDsParser.extractFigmaUrls(getBasePath(), object);
    }

    @Override
    public Object uriToObject(String uri) throws Exception {
        if (isValidImageUrl(uri)) {
            return convertUrlToFile(uri);
        }
        return null;
    }
}
