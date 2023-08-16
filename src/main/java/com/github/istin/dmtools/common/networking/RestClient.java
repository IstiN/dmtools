package com.github.istin.dmtools.common.networking;

import java.io.IOException;

public interface RestClient {

    String execute(GenericRequest jiraRequest) throws IOException;

    String execute(String url) throws IOException;

    String post(GenericRequest jiraRequest) throws IOException;

    String put(GenericRequest jiraRequest) throws IOException;

    String delete(GenericRequest jiraRequest) throws IOException;

    String getBasePath();

    String path(String path);
}
