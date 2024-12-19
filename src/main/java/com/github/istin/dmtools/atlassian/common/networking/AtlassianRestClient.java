package com.github.istin.dmtools.atlassian.common.networking;

import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.Request;

import java.io.IOException;

public abstract class AtlassianRestClient extends AbstractRestClient {

    public AtlassianRestClient(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
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