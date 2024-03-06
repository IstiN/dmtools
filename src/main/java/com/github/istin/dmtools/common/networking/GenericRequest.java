package com.github.istin.dmtools.common.networking;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GenericRequest {

    private final RestClient restClient;
    private final StringBuilder url;
    private String body;

    private String fieldsKey = "fields";

    private boolean ignoreCache = false;

    public GenericRequest(RestClient restClient, String url) {
        this.restClient = restClient;
        this.url = new StringBuilder(url);
    }

    public GenericRequest(RestClient restClient, String url, String fieldsKey) {
        this.restClient = restClient;
        this.url = new StringBuilder(url);
        this.fieldsKey = fieldsKey;
    }

    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    public void setIgnoreCache(boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
    }

    public GenericRequest fields(String... fields) {
        StringBuilder fieldsBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            fieldsBuilder.append(fields[i]);
            if (i != fields.length - 1) {
                fieldsBuilder.append(",");
            }
        }
        param(fieldsKey, fieldsBuilder.toString());
        return this;
    }

    public GenericRequest param(String param, int value) {
        return param(param, String.valueOf(value));
    }

    public GenericRequest param(String param, String value) {
        if (url.indexOf("?") > 0) {
            url.append("&");
        } else {
            url.append("?");
        }
        try {
            url
                .append(param)
                .append("=")
                .append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    public synchronized String execute() throws IOException {
        return restClient.execute(this);
    }

    public synchronized String post() throws IOException {
        return restClient.post(this);
    }

    public synchronized String put() throws IOException {
        return restClient.put(this);
    }

    public synchronized String delete() throws IOException {
        return restClient.delete(this);
    }

    public String url() {
        return url.toString();
    }

    public GenericRequest setBody(String body) {
        this.body = body;
        return this;
    }

    public String getBody() {
        return body;
    }

}
