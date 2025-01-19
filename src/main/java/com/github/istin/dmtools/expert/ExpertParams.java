package com.github.istin.dmtools.expert;

import com.github.istin.dmtools.job.Params;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertParams extends Params {

    public static final String PROJECT_CONTEXT = "projectContext";
    public static final String REQUEST = "request";
    public static final String OUTPUT_TYPE = "outputType";
    public static final String FIELD_NAME = "fieldName";
    public static final String KEYWORDS_BLACKLIST = "keywordsBlacklist";

    public enum OutputType {
        comment, field
    }

    @SerializedName(PROJECT_CONTEXT)
    private String projectContext;

    @SerializedName(REQUEST)
    private String request;

    @SerializedName(FIELD_NAME)
    private String fieldName;

    @SerializedName(KEYWORDS_BLACKLIST)
    private String keywordsBlacklist;

    @SerializedName(OUTPUT_TYPE)
    private OutputType outputType = OutputType.comment;

}