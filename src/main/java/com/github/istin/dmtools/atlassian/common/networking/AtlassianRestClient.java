package com.github.istin.dmtools.atlassian.common.networking;

import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.networking.RestClient;
import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public abstract class AtlassianRestClient extends AbstractRestClient {

    public static final String BACKUP_503 = "backup:503";
    public static final String NO_SUCH_PARENT_EPICS = "No issues have a parent epic with key or name:400";

    public AtlassianRestClient(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
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

    @Override
    public Request.Builder sign(Request.Builder builder) {
        return builder
                .header("Authorization", authorization)
                .header("X-Atlassian-Token", "nocheck")
                .header("Content-Type", "application/json")
                ;
    }
}