package com.github.istin.dmtools.figma;

import com.github.istin.dmtools.atlassian.confluence.ContentUtils;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.utils.ImageUtils;
import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.Request;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FigmaClient extends AbstractRestClient implements ContentUtils.UrlToImageFile {

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
    public File getCachedFile(GenericRequest jiraRequest) {
        String url = jiraRequest.url();
        String value = DigestUtils.md5Hex(url);
        return new File(getCacheFolderName() + "/" + value + ((url.contains("images") ? ".png" : "")));
    }

    public String downloadImageAsBase64(String path) throws IOException {
        File imageFile = downloadImage(path);
        return ImageUtils.convertToBase64(imageFile, "png");
    }

    private String parseFileId(String url) {
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

    private static String extractValueByParameter(String url, String paramName) {
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
        boolean isFigmaLink = url.contains("figma") && url.contains("file");
        try {
            if (isFigmaLink) {
                parseFileId(url);
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
}
