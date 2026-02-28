package com.github.istin.dmtools.projectsetup;

import com.github.istin.dmtools.job.Params;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSetupAnalysisJobParams extends Params {

    public static final String PROJECT_KEY = "projectKey";

    @SerializedName(PROJECT_KEY)
    private String projectKey;
}
