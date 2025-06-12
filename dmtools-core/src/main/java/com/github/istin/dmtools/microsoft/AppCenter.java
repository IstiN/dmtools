package com.github.istin.dmtools.microsoft;

import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.utils.StringUtils;
import com.github.istin.dmtools.microsoft.model.ACApp;
import com.github.istin.dmtools.microsoft.model.ACAppVersion;
import com.github.istin.dmtools.microsoft.model.App;
import com.github.istin.dmtools.microsoft.model.AppVersion;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class AppCenter {

    private static final Logger logger = LogManager.getLogger(AppCenter.class);

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static final String BASE_URL = "https://api.appcenter.ms/v0.1/";

    private final OkHttpClient client;
    private final String organization;
    private final String token;
    private final String urlApps;
    private final String urlUsers;
    private final String urlDistributionGroupsDetails;
    private final String urlDistributionGroups;

    private final String baseUrlOrganization;

    private final String baseUrlWebsite;
    private final String baseUrlInstallWebsite;

    private List<ACApp> cachedApps;

    public AppCenter(String organization, String token) {
        this.organization = organization;
        this.token = token;
        this.urlApps = BASE_URL + "orgs/" + this.organization + "/apps";
        this.urlUsers = BASE_URL + "orgs/" + this.organization + "/users";
        this.urlDistributionGroupsDetails = BASE_URL + "orgs/" + this.organization + "/distribution_groups_details";
        this.urlDistributionGroups = BASE_URL + "orgs/" + this.organization + "/distribution_groups/";
        this.baseUrlOrganization = BASE_URL + "apps/" + this.organization + "/";
        this.baseUrlWebsite = "https://appcenter.ms/orgs/" + this.organization + "/apps/";
        this.baseUrlInstallWebsite = "https://install.appcenter.ms/orgs/" + this.organization + "/apps/";

        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    protected Request.Builder sign(Request.Builder builder) {
        builder.header("accept", "application/json");
        builder.header("X-API-Token", token);
        return builder;
    }

    public String post(String url, RequestBody formBody) throws IOException {
        try (Response response = client.newCall(sign(
                new Request.Builder())
                .url(url)
                .post(formBody)
                .build()
        ).execute()) {
            return response.body() != null ? response.body().string() : null;
        } finally {
            client.connectionPool().evictAll();
        }
    }

    public String put(String url, RequestBody formBody) throws IOException {
        try (Response response = client.newCall(sign(
                new Request.Builder())
                .url(url)
                .put(formBody)
                .build()
        ).execute()) {
            return response.body() != null ? response.body().string() : null;
        } finally {
            client.connectionPool().evictAll();
        }
    }

    public String execute(String url) throws IOException {
        try {
            try (Response response = client.newCall(sign(
                    new Request.Builder())
                    .url(url)
                    .build()
            ).execute()) {
                return response.body() != null ? response.body().string() : null;
            } finally {
                client.connectionPool().evictAll();
            }
        } catch (IOException e) {
            logger.error(url);
            throw e;
        }
    }

    public List<? extends App> performApps() throws IOException {
        if (cachedApps == null) {
            this.cachedApps = JSONModel.convertToModels(ACApp.class, new JSONArray(execute(urlApps)));
        }
        return cachedApps;
    }

    public JSONArray performUsers() throws IOException {
        return new JSONArray(execute(urlUsers));
    }

    public List<ACAppVersion> performVersions(String publicAppId) throws IOException {
        String url = baseUrlOrganization + publicAppId + "/releases";
        String response = execute(url);
        try {
            return JSONModel.convertToModels(ACAppVersion.class, new JSONArray(response));
        } catch (Exception e) {
            logger.error(e);
            logger.error(url);
            logger.error(response);
            return new ArrayList<>();
        }
    }

    public List<ACAppVersion> performRelease(String publicAppId, String releaseId) throws IOException {
        return JSONModel.convertToModels(ACAppVersion.class, new JSONArray(execute(baseUrlOrganization + publicAppId + "/releases/" + releaseId)));
    }

    public AppVersion performVersion(String publicAppId, String version) throws IOException {
        List<ACAppVersion> acAppVersions = JSONModel.convertToModels(ACAppVersion.class, new JSONArray(execute(baseUrlOrganization + publicAppId + "/releases")));
        for (ACAppVersion acAppVersion : acAppVersions) {
            if (acAppVersion.getId().toString().equalsIgnoreCase(version)) {
                return acAppVersion;
            }
        }
        return null;
    }

    public String performDistributionGroupsDetails() throws IOException {
        return execute(urlDistributionGroupsDetails);
    }

    public String performDistributionGroupsMembers(String groupName) throws IOException {
        return execute(membersUrl(groupName));
    }

    public String performDistributionGroupsMembersForApp(String groupName, String app) throws IOException {
        return execute(membersUrl(groupName, app));
    }

    public String addDistributionGroupsMembers(String groupName, String[] membersMails) throws IOException {
        for (String member : membersMails) {
            JSONArray mails = new JSONArray();
            if (member != null) {
                mails.put(member);
            }
            JSONObject jsonObject = new JSONObject().put("user_emails", mails);
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            logger.error(post(membersUrl(groupName), body));
        }
        return "";
    }

    public String addAppToDistributionGroup(String groupName, String appName) throws IOException {
        JSONArray apps = new JSONArray();
        apps.put(new JSONObject().put("name", appName));
        JSONObject jsonObject = new JSONObject().put("apps", apps);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        return post(appsUrl(groupName), body);
    }

    public static String convertGroupName(String groupName) throws UnsupportedEncodingException {
        return URLEncoder.encode(groupName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
    }

    public String groupIdByName(String groupName) throws IOException {
        String response = execute(urlDistributionGroups + convertGroupName(groupName));
        String id = new JSONObject(response).optString("id");
        if (id == null) {
            logger.error("{} is not exist", groupName);
        }
        return id;
    }


    public String membersUrl(String groupName) throws UnsupportedEncodingException {
        return urlDistributionGroups + convertGroupName(groupName) + "/members";
    }

    public String membersUrl(String groupName, String app) throws UnsupportedEncodingException {
        return BASE_URL + "apps/" + organization + "/" + app + "/distribution_groups/" + convertGroupName(groupName) + "/members";
    }

    public String appsUrl(String groupName) throws UnsupportedEncodingException {
        return urlDistributionGroups + convertGroupName(groupName) + "/apps";
    }

    public String shareAppVersion(String publicAppId, String appVersion, String groups) throws IOException {
        String[] splittedGroups = groups.split(",");
        String lastResult = null;
        for (String groupName : splittedGroups) {
            String groupId = groupIdByName(groupName);
            if (groupId == null) {
                continue;
            }

            String bodyString = new JSONObject().put("id", groupId)
                    .put("mandatory_update", false)
                    .put("notify_testers", false)
                    .toString();

            RequestBody body = RequestBody.create(JSON, bodyString);
            lastResult = post(baseUrlOrganization + publicAppId + "/releases/" + appVersion + "/groups", body);
        }
        return lastResult;
    }

    public String convertVersionLinksToDownloadLinks(String input) {
        return input.replaceAll("https://appcenter.ms/orgs/" + organization + "/apps/", "https://install.appcenter.ms/orgs/" + organization + "/apps/")
                .replaceAll("/distribute/releases/", "/releases/");
    }

    public void shareAllApps(String input, BiFunction<App, String, String> groupsFunction) throws IOException {
        List<String> urls = StringUtils.extractUrls(input);
        int index = 0;
        for (String url : urls) {
            logger.info("progress {} / {}", index, urls.size());
            String[] appIdVersion = null;
            if (url.startsWith("https://install.appcenter.ms/orgs/" + organization + "/apps/")) {//465
                appIdVersion = url.replace("https://install.appcenter.ms/orgs/" + organization + "/apps/", "").split("/releases/");
            }
            if (url.startsWith("https://appcenter.ms/orgs/" + organization + "/apps/")) {//465
                appIdVersion = url.replace("https://appcenter.ms/orgs/" + organization + "/apps/", "").split("/distribute/releases/");
            }
            if (appIdVersion != null && appIdVersion.length == 2) {
                App app = findApp(appIdVersion[0]);
                if (app != null) {
                    shareAppVersion(appIdVersion[0], appIdVersion[1], groupsFunction.apply(app, appIdVersion[1]));
                }
            }
            index++;
        }
    }

    private App findApp(String appId) throws IOException {
        List<? extends App> apps = performApps();
        for (App app : apps) {
            if (app.getName().equalsIgnoreCase(appId)) {
                return app;
            }
        }
        return null;
    }

    public String getBaseUrlWebsite() {
        return baseUrlWebsite;
    }

    public String getBaseUrlInstallWebsite() {
        return baseUrlInstallWebsite;
    }
}