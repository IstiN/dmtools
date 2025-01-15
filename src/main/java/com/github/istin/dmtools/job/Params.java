package com.github.istin.dmtools.job;

import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.google.gson.annotations.SerializedName;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Params {

    public static final String INPUT_JQL = "inputJql";
    public static final String INITIATOR = "initiator";
    public static final String CONFLUENCE_PAGES = "confluencePages";
    public static final String MODEL = "model";
    public static final String IS_CODE_AS_SOURCE = "isCodeAsSource";

    @SerializedName(INPUT_JQL)
    private String inputJql;

    @SerializedName(INITIATOR)
    private String initiator;

    @SerializedName(MODEL)
    private String model;

    @SerializedName(IS_CODE_AS_SOURCE)
    private boolean isCodeAsSource = false;

    @SerializedName(CONFLUENCE_PAGES)
    private String[] confluencePages;

    @SerializedName(SourceCodeConfig._KEY)
    private SourceCodeConfig[] sourceCodeConfig;

    public void setSourceCodeConfigs(SourceCodeConfig... sourceCodeConfig) {
        this.sourceCodeConfig = sourceCodeConfig;
    }

    public void setConfluencePages(String... confluencePages) {
        this.confluencePages = confluencePages;
    }
}