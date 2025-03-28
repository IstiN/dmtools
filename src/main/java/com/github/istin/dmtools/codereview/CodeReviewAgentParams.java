package com.github.istin.dmtools.codereview;

import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeReviewAgentParams {

    public static final String SOURCE_CODE_CONFIG = "source_code_config";
    public static final String PULL_REQUEST_ID = "pull_request_id";

    @SerializedName(SOURCE_CODE_CONFIG)
    private SourceCodeConfig sourceCodeConfig;

    @SerializedName(PULL_REQUEST_ID)
    private String pullRequestId;

}
