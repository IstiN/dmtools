package com.github.istin.dmtools.ai.curl;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurlAIConfig {

    public static final String _KEY = "curl_ai_config";

    public static final String MODEL = "model";
    public static final String BASE_PATH = "base_path";
    public static final String AUTH = "auth";
    public static final String CURL_URL_TEMPLATE = "curl_url_template";
    public static final String CURL_BODY_TEMPLATE = "curl_body_template";
    public static final String CURL_AI_RESPONSE_JSON_PATH = "curl_ai_response_json_path";

    @SerializedName(MODEL)
    private String model;

    @SerializedName(BASE_PATH)
    private String basePath;

    @SerializedName(AUTH)
    private String auth;

    @SerializedName(CURL_URL_TEMPLATE)
    private String curlUrlTemplate;

    @SerializedName(CURL_BODY_TEMPLATE)
    private String curlBodyTemplate;

    @SerializedName(CURL_AI_RESPONSE_JSON_PATH)
    private String curlAiResponseJsonPath;


}
