package com.github.istin.dmtools.networking;

import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.networking.RestClient;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public abstract class AbstractRestClient implements RestClient {
    private static final Logger logger = LogManager.getLogger(AbstractRestClient.class);
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    protected final OkHttpClient client;
    protected String basePath;
    protected String authorization;
    protected boolean isClearCache = false;
    private boolean isCacheGetRequestsEnabled = true;

    public boolean isCachePostRequestsEnabled() {
        return isCachePostRequestsEnabled;
    }

    public void setCachePostRequestsEnabled(boolean cachePostRequestsEnabled) {
        isCachePostRequestsEnabled = cachePostRequestsEnabled;
    }

    @Override
    public OkHttpClient getClient() {
        return client;
    }

    private boolean isCachePostRequestsEnabled = false;
    private boolean isWaitBeforePerform = false;
    private HashMap<String, Long> timeMeasurement = new HashMap<>();

    public AbstractRestClient(String basePath, String authorization) throws IOException {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(getTimeout(), TimeUnit.SECONDS)
                .readTimeout(getTimeout(), TimeUnit.SECONDS)
                .build();
        this.basePath = basePath;
        this.authorization = authorization;

        reinitCache();
    }

    public int getTimeout() {
        return 60;
    }

    private void reinitCache() throws IOException {
        File cache = new File(getCacheFolderName());
        logger.info("cache folder: {}", cache.getAbsolutePath());
        if (isClearCache) {
            cache.mkdirs();
            FileUtils.deleteDirectory(cache);
        }
    }

    public static IOException printAndCreateException(Request request, Response response) throws IOException {
        int code = response.code();
        String body = response.body() != null ? response.body().string() : "";
        if (code == 503) {
            return new IOException(RestClientException.BACKUP_503);
        } else if (code == 400) {
            if (body.contains("No issues have a parent epic with key or name")) {
                return new IOException(RestClientException.NO_SUCH_PARENT_EPICS);
            }
        } else if (body.contains("rate")) {
            return new RateLimitException("rate limit", body, response);
        }
        String responseError = "printAndCreateException error: " + request.url() + "\n" + body + "\n" + response.message() + "\n" + code;
        logger.error(responseError);
        return new RestClient.RestClientException(responseError, body);
    }

    public void setClearCache(boolean clearCache) throws IOException {
        if (clearCache != isClearCache) {
            isClearCache = clearCache;
            reinitCache();
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

    protected void clearRequestIfExpired(GenericRequest genericRequest, Long updated) throws IOException {
        File cachedFile = getCachedFile(genericRequest);
        clearRequestIfExpired(genericRequest, updated, cachedFile);
    }

    protected void clearRequestIfExpired(GenericRequest genericRequest, Long updated, File cachedFile) throws IOException {
        if (cachedFile.exists() && updated != null) {
            BasicFileAttributes attr = Files.readAttributes(cachedFile.toPath(), BasicFileAttributes.class);
            FileTime fileTime = attr.lastModifiedTime();
            if (fileTime.toMillis() < updated) {
                clearCache(genericRequest);
            }
        }
    }
    public File getCachedFile(GenericRequest jiraRequest) {
        String value = DigestUtils.md5Hex(jiraRequest.url());
        File cachedFile = new File(getCacheFolderName() + "/" + value);
        return cachedFile;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public String execute(GenericRequest genericRequest) throws IOException {
        String url = genericRequest.url();
        return execute(url, true, genericRequest.isIgnoreCache(), genericRequest);
    }

    @Override
    public String execute(String url) throws IOException {
        return execute(url, true, false, new GenericRequest(this, url));
    }

    private Request.Builder applyHeaders(Request.Builder builder, GenericRequest genericRequest) {
        for (String key : genericRequest.getHeaders().keySet()) {
            builder.header(key, genericRequest.getHeaders().get(key));
        }
        return builder;
    }

    private String execute(String url, boolean isRepeatIfFails, boolean isIgnoreCache, GenericRequest genericRequest) throws IOException {
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
                    Thread.currentThread().sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Request request = applyHeaders(sign(new Request.Builder())
                        .header("User-Agent", "DMTools"), genericRequest)
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
                        throw AbstractRestClient.printAndCreateException(request, response);
                    }
                }
            } catch (SocketTimeoutException e) {
                logger.info(url);
                if (isRepeatIfFails) {
                    if (isWaitBeforePerform) {
                        try {
                            Thread.currentThread().sleep(100);
                        } catch (InterruptedException socketException) {
                            socketException.printStackTrace();
                        }
                    }
                    return execute(url, false, isIgnoreCache, genericRequest);
                } else {
                    throw e;
                }
            }
        } finally {
            Long prevTime = timeMeasurement.get(url);
            long time = System.currentTimeMillis() - 200 - prevTime;
            logger.info("{} {}", time, url);
            client.connectionPool().evictAll();
        }
    }

    protected String getCacheFolderName() {
        return "cache" + getClass().getSimpleName();
    }

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

        if (isCachePostRequestsEnabled && !genericRequest.isIgnoreCache()) {
            String value = DigestUtils.md5Hex(buildHashForPostRequest(genericRequest, url));
            File cache = new File(getCacheFolderName());
            cache.mkdirs();
            File cachedFile = new File(getCacheFolderName() + "/" + value);
            if (cachedFile.exists()) {
                logger.info("Read From Cache: ");
                return FileUtils.readFileToString(cachedFile);
            } else {
                logger.info("Network Request: ");
            }
        } else {
            logger.info("Network Request: ");
        }

        RequestBody body = RequestBody.create(JSON, genericRequest.getBody());
        try (Response response = client.newCall(applyHeaders(sign(
                new Request.Builder())
                .url(url)
                .header("User-Agent", "DMTools"), genericRequest)
                .post(body)
                .build()
        ).execute()) {
            String responseAsString = response.body().string();
            if (response.isSuccessful()) {
                if (isCachePostRequestsEnabled) {
                    String value = DigestUtils.md5Hex(buildHashForPostRequest(genericRequest, url));
                    File cache = new File(getCacheFolderName());
                    cache.mkdirs();
                    File cachedFile = new File(getCacheFolderName() + "/" + value);
                    FileUtils.writeStringToFile(cachedFile, responseAsString);
                }
            }
            return responseAsString;
        } finally {
            client.connectionPool().evictAll();
        }
    }

    protected @NotNull String buildHashForPostRequest(GenericRequest genericRequest, String url) {
        return url + genericRequest.getBody();
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
        try (Response response = client.newCall(applyHeaders(sign(
                new Request.Builder())
                .url(url)
                .header("User-Agent", "DMTools"), genericRequest)
                .put(body)
                .build()
        ).execute()) {
            return response.body().string();
        } finally {
            client.connectionPool().evictAll();
        }
    }

    @Override
    public String patch(GenericRequest genericRequest) throws IOException {
        String url = genericRequest.url();
        if (isWaitBeforePerform) {
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        RequestBody body = RequestBody.create(JSON, genericRequest.getBody());
        try (Response response = client.newCall(applyHeaders(sign(
                new Request.Builder())
                .url(url)
                .header("User-Agent", "DMTools"), genericRequest)
                .patch(body)
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
        try (Response response = client.newCall(applyHeaders(sign(
                new Request.Builder())
                .url(url)
                .header("User-Agent", "DMTools"), genericRequest)
                .delete(body)
                .build()
        ).execute()) {
            return response.body().string();
        } finally {
            client.connectionPool().evictAll();
        }
    }

    public void setWaitBeforePerform(boolean waitBeforePerform) {
        isWaitBeforePerform = waitBeforePerform;
    }

    public interface Performer<T> {

        boolean perform(T model) throws Exception;

    }
}
