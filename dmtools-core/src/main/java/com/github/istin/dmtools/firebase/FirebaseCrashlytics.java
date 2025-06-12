package com.github.istin.dmtools.firebase;

import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.networking.AbstractRestClient;
import com.google.auth.oauth2.GoogleCredentials;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirebaseCrashlytics extends AbstractRestClient {

    private static final Logger LOGGER = Logger.getLogger(FirebaseCrashlytics.class.getName());
    private final String projectId;
    private final GoogleCredentials credentials;

    public FirebaseCrashlytics(String projectId, String serviceAccountJsonContent) throws IOException {
        super("https://firebase.googleapis.com/", null);
        this.projectId = projectId;
        this.credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(serviceAccountJsonContent.getBytes(StandardCharsets.UTF_8)))
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.readonly"));
    }

    public List<CrashError> getErrors() throws IOException {
        String url = path(String.format("v1beta1/projects/%s/crashlytics/issues", projectId));
        LOGGER.info("Request URL: " + url);
        GenericRequest request = new GenericRequest(this, url);

        try {
            String response = request.execute();
            LOGGER.info("Response: " + response);
            return parseErrors(response);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error executing request", e);
            throw e;
        }
    }

    private List<CrashError> parseErrors(String jsonResponse) {
        List<CrashError> errors = new ArrayList<>();
        JSONObject responseJson = new JSONObject(jsonResponse);
        JSONArray issuesArray = responseJson.optJSONArray("issues");

        if (issuesArray == null) {
            LOGGER.warning("No 'issues' array found in the response");
            return errors;
        }

        for (int i = 0; i < issuesArray.length(); i++) {
            JSONObject issueJson = issuesArray.getJSONObject(i);
            CrashError error = new CrashError();
            error.setErrorId(issueJson.optString("name", ""));
            error.setTitle(issueJson.optString("title", ""));
            error.setSubtitle(issueJson.optString("subtitle", ""));
            error.setAppVersion(issueJson.optString("appVersion", ""));
            error.setTimeReported(issueJson.optString("createTime", ""));

            JSONObject customKeysJson = issueJson.optJSONObject("customKeys");
            if (customKeysJson != null) {
                Map<String, String> metadata = new HashMap<>();
                for (String key : customKeysJson.keySet()) {
                    metadata.put(key, customKeysJson.getString(key));
                }
                error.setMetadata(metadata);
            }

            errors.add(error);
        }

        return errors;
    }

    @Override
    public String path(String path) {
        return getBasePath() + path;
    }

    @Override
    public Request.Builder sign(Request.Builder builder) {
        try {
            if (credentials.getAccessToken() == null || credentials.getAccessToken().getExpirationTime().getTime() < System.currentTimeMillis()) {
                credentials.refresh();
            }
            String token = credentials.getAccessToken().getTokenValue();
            builder.header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error refreshing credentials", e);
            throw new RuntimeException("Failed to refresh credentials", e);
        }
        return builder;
    }

    public static class CrashError {
        private String errorId;
        private String title;
        private String subtitle;
        private String appVersion;
        private String timeReported;
        private Map<String, String> metadata;

        public String getErrorId() {
            return errorId;
        }

        public void setErrorId(String errorId) {
            this.errorId = errorId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getTimeReported() {
            return timeReported;
        }

        public void setTimeReported(String timeReported) {
            this.timeReported = timeReported;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }

        @Override
        public String toString() {
            return "CrashError{" +
                    "errorId='" + errorId + '\'' +
                    ", title='" + title + '\'' +
                    ", subtitle='" + subtitle + '\'' +
                    ", appVersion='" + appVersion + '\'' +
                    ", timeReported='" + timeReported + '\'' +
                    ", metadata=" + metadata +
                    '}';
        }
    }

    // Optional: Add methods to get more detailed information about specific errors
    public CrashError getErrorDetails(String errorId) throws IOException {
        String url = path(String.format("v1/projects/%s/issues/%s", projectId, errorId));
        GenericRequest request = new GenericRequest(this, url);

        String response = request.execute();
        JSONObject errorJson = new JSONObject(response);

        CrashError error = new CrashError();
        error.setErrorId(errorJson.getString("name"));
        error.setTitle(errorJson.getString("title"));
        error.setSubtitle(errorJson.optString("subtitle", ""));
        error.setAppVersion(errorJson.optString("appVersion", ""));
        error.setTimeReported(errorJson.getString("createTime"));

        JSONObject customKeysJson = errorJson.optJSONObject("customKeys");
        if (customKeysJson != null) {
            Map<String, String> metadata = new HashMap<>();
            for (String key : customKeysJson.keySet()) {
                metadata.put(key, customKeysJson.getString(key));
            }
            error.setMetadata(metadata);
        }

        return error;
    }
}