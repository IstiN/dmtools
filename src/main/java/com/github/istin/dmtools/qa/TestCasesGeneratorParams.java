package com.github.istin.dmtools.qa;

import com.github.istin.dmtools.atlassian.jira.model.Relationship;
import com.github.istin.dmtools.job.Params;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCasesGeneratorParams extends Params {

    public static final String EXISTING_TEST_CASES_JQL = "existingTestCasesJql";
    public static final String OUTPUT_TYPE = "outputType";
    public static final String TEST_CASES_PRIORITIES = "testCasesPriorities";
    public static final String RELATED_TEST_CASES_RULES = "relatedTestCasesRules";
    public static final String TEST_CASE_ISSUE_TYPE = "testCaseIssueType";
    public static final String TEST_CASE_LINK_RELATIONSHIP = "testCaseLinkRelationship";

    public enum OutputType {
        comment, creation
    }

    @SerializedName(EXISTING_TEST_CASES_JQL)
    private String existingTestCasesJql;
    @SerializedName(TEST_CASES_PRIORITIES)
    private String testCasesPriorities;
    @SerializedName(RELATED_TEST_CASES_RULES)
    private String relatedTestCasesRules;
    @SerializedName(TEST_CASE_ISSUE_TYPE)
    private String testCaseIssueType = "Test Case";
    @SerializedName(TEST_CASE_LINK_RELATIONSHIP)
    private String testCaseLinkRelationship = Relationship.IS_TESTED_BY;

    @SerializedName(OUTPUT_TYPE)
    private OutputType outputType = OutputType.comment;
}