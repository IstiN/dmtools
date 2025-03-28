package com.github.istin.dmtools.codereview;

import java.util.Date;

import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GuidelinesAgentParams {

    public static final String SOURCE_CODE_CONFIG = "source_code_config";
    public static final String PROJECT_NAME = "project_name";
    public static final String HISTORY_START_DATE = "history_start_date";

    @SerializedName(SOURCE_CODE_CONFIG)
    private SourceCodeConfig sourceCodeConfig;

    @SerializedName(PROJECT_NAME)
    private String projectName;

    @SerializedName(HISTORY_START_DATE)
    private Date historyStartDate;

}
