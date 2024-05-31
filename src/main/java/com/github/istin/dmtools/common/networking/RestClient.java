package com.github.istin.dmtools.common.networking;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.IOException;

public interface RestClient {

    String execute(GenericRequest jiraRequest) throws IOException;

    String execute(String url) throws IOException;

    String post(GenericRequest jiraRequest) throws IOException;

    String put(GenericRequest jiraRequest) throws IOException;

    String delete(GenericRequest jiraRequest) throws IOException;

    String getBasePath();

    String path(String path);

    Request.Builder sign(Request.Builder builder);

    OkHttpClient getClient();

    class Impl {
        public static File downloadFile(RestClient restClient, GenericRequest genericRequest, File downloadedFile) throws IOException {
            String url = genericRequest.url();
            System.out.println(url);
            if (downloadedFile.exists()) {
                return downloadedFile;
            }
            OkHttpClient client = restClient.getClient();
            try (Response response = client.newCall(restClient.sign(
                            new Request.Builder())
                    .url(url)
//                    .header("User-Agent", "DMTools")
                    .build()
            ).execute()) {
                // Check if the response is successful
                if (response.isSuccessful() && response.body() != null) {
                    // Get the response body
                    ResponseBody responseBody = response.body();
                    // Create a file to save the downloaded data
                    try (BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile))) {
                        // Write the response body to the file
                        sink.writeAll(responseBody.source());
                    }

                    System.out.println("Download successful: " + downloadedFile.getAbsolutePath());
                    return downloadedFile;
                } else {
                    throw new IOException(response.code() + " " + response.body());
                }
            } finally {
                client.connectionPool().evictAll();
            }
        }

        public static String getFileImageExtension(String url) {
            String imageExtension = "";

            if (url.endsWith(".png")) {
                imageExtension = ".png";
            } else if (url.endsWith(".jpg") || url.endsWith(".jpeg")) {
                imageExtension = ".jpg";
            } else if (url.endsWith(".gif")) {
                imageExtension = ".gif";
            } else if (url.endsWith(".bmp")) {
                imageExtension = ".bmp";
            } else if (url.endsWith(".svg")) {
                imageExtension = ".svg";
            } else if (url.endsWith(".webp")) {
                imageExtension = ".webp";
            }
            return imageExtension;
        }
    }
}
