package com.github.istin.dmtools.atlassian.jira.utils;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuesIDsParserParams {

    public enum Transformation {
        NONE,
        UPPERCASE,
        LOWERCASE
    }

    private Transformation transformation = Transformation.NONE;

    @SerializedName("replace_characters")
    private String[] replaceCharacters;
    @SerializedName("replace_values")
    private String[] replaceValues;

}
