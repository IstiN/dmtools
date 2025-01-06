package com.github.istin.dmtools.sync;

import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParserParams;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceCodeCommitTrackerSyncParams {

    public static final String ISSUE_ID_CODES = "issue_id_codes";
    public static final String SYNC_TYPE = "sync_type";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String SOURCE_CODE_CONFIG = "source_code_config";
    public static final String ISSUES_IDS_PARSER_PARAMS = "issues_ids_parser_params";

    public enum SyncType {
        ALL, ONE_DAY, RANGE
    }

    @SerializedName(ISSUE_ID_CODES)
    private String[] issueIdCodes;

    @SerializedName(SYNC_TYPE)
    private SyncType syncType;

    @SerializedName(START_DATE)
    private String startDate;

    @SerializedName(END_DATE)
    private String endDate;

    @SerializedName(SOURCE_CODE_CONFIG)
    private SourceCodeConfig sourceCodeConfig;

    @SerializedName(ISSUES_IDS_PARSER_PARAMS)
    private IssuesIDsParserParams issuesIDsParserParams;
}