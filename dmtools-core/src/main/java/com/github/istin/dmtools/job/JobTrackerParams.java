package com.github.istin.dmtools.job;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobTrackerParams<CustomParams> extends TrackerParams {

    @SerializedName("agentParams")
    private CustomParams agentParams;

}
