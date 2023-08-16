package com.github.istin.dmtools.atlassian.common.networking;

import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.networking.RestClient;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public abstract class AtlassianRestClient implements RestClient {

    public static final String BACKUP_503 = "backup:503";
    public static final String NO_SUCH_PARENT_EPICS = "No issues have a parent epic with key or name:400";
    private final OkHttpClient client;
    private String basePath;
    private String authorization;

    private boolean isCacheGetRequestsEnabled = true;

    public void setClearCache(boolean clearCache) {
        isClearCache = clearCache;
    }

    private boolean isWaitBeforePerform = false;

    private boolean isClearCache = false;

    public interface Performer<T extends JSONModel> {

        boolean perform(T model) throws Exception;

    }

    public AtlassianRestClient(String basePath, String authorization) throws IOException {
        this.basePath = basePath;
        this.authorization = authorization;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        File cache = new File(getCacheFolderName());
        System.out.println("cache folder: " + cache.getAbsolutePath());
        if (isClearCache) {
            cache.mkdirs();
            FileUtils.deleteDirectory(cache);
        }
    }

    public void setCacheGetRequestsEnabled(boolean cacheGetRequestsEnabled) {
        isCacheGetRequestsEnabled = cacheGetRequestsEnabled;
    }

    public void clearCache(GenericRequest jiraRequest) {
        if (isCacheGetRequestsEnabled) {
            File cachedFile = getCachedFile(jiraRequest);
            if (cachedFile.exists()) {
                cachedFile.delete();
            }
        }
    }

    public File getCachedFile(GenericRequest jiraRequest) {
        String value = DigestUtils.md5Hex(jiraRequest.url());
        File cachedFile = new File(getCacheFolderName() + "/" + value);
        return cachedFile;
    }

    private Request.Builder sign(Request.Builder builder) {
        return builder
                .header("Authorization", authorization)
                .header("Content-Type", "application/json");
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public String execute(GenericRequest genericRequest) throws IOException {
        String url = genericRequest.url();
        return execute(url, true, genericRequest.isIgnoreCache());
    }

    private HashMap<String, Long> timeMeasurement = new HashMap<>();

    @Override
    public String execute(String url) throws IOException {
        return execute(url, true, false);
    }

    private String execute(String url, boolean isRepeatIfFails, boolean isIgnoreCache) throws IOException {
        try {
            timeMeasurement.put(url, System.currentTimeMillis());
            if (isCacheGetRequestsEnabled && !isIgnoreCache) {
                String value = DigestUtils.md5Hex(url);
                File cache = new File(getCacheFolderName());
                cache.mkdirs();
                File cachedFile = new File(getCacheFolderName() + "/" + value);
                if (cachedFile.exists()) {
                    return FileUtils.readFileToString(cachedFile);
                }
            }
            if (isWaitBeforePerform) {
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Request request = sign(new Request.Builder())
                        .url(url)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String result = response.body() != null ? response.body().string() : null;
                        if (isCacheGetRequestsEnabled) {
                            String value = DigestUtils.md5Hex(url);
                            File cache = new File(getCacheFolderName());
                            cache.mkdirs();
                            File cachedFile = new File(getCacheFolderName() + "/" + value);
                            FileUtils.writeStringToFile(cachedFile, result);
                        }
                        return result;
                    } else {
                        throw printAndCreateException(request, response);
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println(url);
                if (isRepeatIfFails) {
                    if (isWaitBeforePerform) {
                        try {
                            Thread.currentThread().sleep(100);
                        } catch (InterruptedException socketException) {
                            socketException.printStackTrace();
                        }
                    }
                    return execute(url, false, isIgnoreCache);
                } else {
                    throw e;
                }
            }
        } finally {
            Long prevTime = timeMeasurement.get(url);
            long time = System.currentTimeMillis() - 200 - prevTime;
            System.out.println(time + " " + url);
            client.connectionPool().evictAll();
        }
    }

    public static class JiraException extends IOException {

        private final String body;

        public JiraException(String message, String body) {
            super(message);
            this.body = body;
        }

        public String getBody() {
            return body;
        }
    }

    public static IOException printAndCreateException(Request request, Response response) throws IOException {
        int code = response.code();
        String body = response.body() != null ? response.body().string() : "";
        if (code == 503) {
            return new IOException(BACKUP_503);
        } else if (code == 400) {
            if (body.contains("No issues have a parent epic with key or name")) {
                return new IOException(NO_SUCH_PARENT_EPICS);
            }
        }
        String responseError = "printAndCreateException error: " + request.url() + "\n" + body + "\n" + response.message() + "\n" + code;
        System.err.println(responseError);
        return new JiraException(responseError, body);
    }

    private String getCacheFolderName() {
        return "cache" + getClass().getSimpleName();
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    public String post(GenericRequest genericRequest) throws IOException {
        String url = genericRequest.url();

        if (isWaitBeforePerform) {
            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        RequestBody body = RequestBody.create(JSON, genericRequest.getBody());
        try (Response response = client.newCall(sign(
                new Request.Builder())
                .url(url)
                .post(body)
                .build()
        ).execute()) {
            return response.body().string();
        } finally {
            client.connectionPool().evictAll();
        }
    }

    @Override
    public String put(GenericRequest genericRequest) throws IOException {
        String url = genericRequest.url();
        if (isWaitBeforePerform) {
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        RequestBody body = RequestBody.create(JSON, genericRequest.getBody());
        try (Response response = client.newCall(sign(
                new Request.Builder())
                .url(url)
                .put(body)
                .build()
        ).execute()) {
            return response.body().string();
        } finally {
            client.connectionPool().evictAll();
        }
    }

    @Override
    public String delete(GenericRequest genericRequest) throws IOException {
        String url = genericRequest.url();
        if (isWaitBeforePerform) {
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String jiraRequestBody = genericRequest.getBody();
        RequestBody body = null;
        if (jiraRequestBody != null) {
            body = RequestBody.create(JSON, jiraRequestBody);
        }
        try (Response response = client.newCall(sign(
                new Request.Builder())
                .url(url)
                .delete(body)
                .build()
        ).execute()) {
            return response.body().string();
        } finally {
            client.connectionPool().evictAll();
        }
    }
}