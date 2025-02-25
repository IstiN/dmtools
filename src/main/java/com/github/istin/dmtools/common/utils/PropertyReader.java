package com.github.istin.dmtools.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

	private static String PATH_TO_CONFIG_FILE = "/config.properties";

	private static final int DEFAULT_AI_RETRY_AMOUNT = 3;
	private static final long DEFAULT_AI_RETRY_DELAY_STEP = 20000L;

	public static void setConfigFile(String resourcePath) {
		PATH_TO_CONFIG_FILE = resourcePath;
	}


	static Properties prop;

	public String getValue(String propertyKey) {
		if (prop == null) {
			prop = new Properties();
			InputStream input = null;
			try {
				input = getClass().getResourceAsStream(PATH_TO_CONFIG_FILE);
				if (input != null) {
					prop.load(input);
				}
			} catch (IOException e) {
				throw new IllegalStateException("Property file not found");
			} finally {
				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String property = prop.getProperty(propertyKey);
		if (property == null || property.length() == 0) {
			return System.getenv(propertyKey);
		}
		return property;
	}

	public String getJiraLoginPassToken() {
		return getValue("JIRA_LOGIN_PASS_TOKEN");
	}

	public String getJiraBasePath() {
		return getValue("JIRA_BASE_PATH");
	}

	public String getJiraAuthType() {
		return getValue("JIRA_AUTH_TYPE");
	}

	public boolean isJiraWaitBeforePerform() {
		String value = getValue("JIRA_WAIT_BEFORE_PERFORM");
		if (value == null) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	public boolean isJiraLoggingEnabled() {
		String value = getValue("JIRA_LOGGING_ENABLED");
		if (value == null) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	public boolean isJiraClearCache() {
		String value = getValue("JIRA_CLEAR_CACHE");
		if (value == null) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	public String getJiraExtraFieldsProject() {
		return getValue("JIRA_EXTRA_FIELDS_PROJECT");
	}

	public String[] getJiraExtraFields() {
		String value = getValue("JIRA_EXTRA_FIELDS");
		if (value == null) {
			return null;
		}
		return value.split(",");
	}

	public Long getSleepTimeRequest() {
		String value = getValue("SLEEP_TIME_REQUEST");
		if (value == null) {
			return 300l;
		}
		return Long.parseLong(value);
	}


	public String getRallyToken() {
		return getValue("RALLY_TOKEN");
	}

	public String getRallyPath() {
		return getValue("RALLY_PATH");
	}

	public String getAppCenterToken() {
		return getValue("APP_CENTER_TOKEN");
	}

	public String getAppCenterOrganization() {
		return getValue("APP_CENTER_ORGANIZATION");
	}

	public String getBitbucketToken() {
		return getValue("BITBUCKET_TOKEN");
	}

	public String getBitbucketApiVersion() {
		return getValue("BITBUCKET_API_VERSION");
	}

	public String getBitbucketWorkspace() {
		return getValue("BITBUCKET_WORKSPACE");
	}

	public String getBitbucketRepository() {
		return getValue("BITBUCKET_REPOSITORY");
	}

	public String getBitbucketBranch() {
		return getValue("BITBUCKET_BRANCH");
	}

	public String getBitbucketBasePath() {
		return getValue("BITBUCKET_BASE_PATH");
	}

	public String getGithubToken() {
		return getValue("SOURCE_GITHUB_TOKEN");
	}

	public String getGithubWorkspace() {
		return getValue("SOURCE_GITHUB_WORKSPACE");
	}

	public String getGithubRepository() {
		return getValue("SOURCE_GITHUB_REPOSITORY");
	}

	public String getGithubBranch() {
		return getValue("SOURCE_GITHUB_BRANCH");
	}

	public String getGithubBasePath() {
		return getValue("SOURCE_GITHUB_BASE_PATH");
	}

	public String getGitLabToken() {
		return getValue("GITLAB_TOKEN");
	}

	public String getGitLabWorkspace() {
		return getValue("GITLAB_WORKSPACE");
	}

	public String getGitLabRepository() {
		return getValue("GITLAB_REPOSITORY");
	}

	public String getGitLabBranch() {
		return getValue("GITLAB_BRANCH");
	}

	public String getGitLabBasePath() {
		return getValue("GITLAB_BASE_PATH");
	}

	public String getConfluenceBasePath() {
		return getValue("CONFLUENCE_BASE_PATH");
	}

	public String getConfluenceLoginPassToken() {
		return getValue("CONFLUENCE_LOGIN_PASS_TOKEN");
	}

	public String getConfluenceGraphQLPath() {
		return getValue("CONFLUENCE_GRAPHQL_PATH");
	}

	public String getConfluenceDefaultSpace() {
		return getValue("CONFLUENCE_DEFAULT_SPACE");
	}

	public String getOpenAIBathPath() {
		return getValue("OPEN_AI_BATH_PATH");
	}

	public String getOpenAIApiKey() {
		return getValue("OPEN_AI_API_KEY");
	}

	public String getOpenAIModel() {
		return getValue("OPEN_AI_MODEL");
	}

	public String getCurlAIBathPath() {
		return getValue("CURL_AI_BATH_PATH");
	}

	public String getCurlAIAuth() {
		return getValue("CURL_AI_AUTH");
	}

	public String getCurlAIModel() {
		return getValue("CURL_AI_MODEL");
	}

	public String getCurlAiResponseJsonPath() {
		return getValue("CURL_AI_RESPONSE_JSON_PATH");
	}

	public String getCurlAIUrlTemplate() {
		return getValue("CURL_AI_URL_TEMPLATE");
	}

	public String getCurlAIBodyTemplate() {
		return getValue("CURL_AI_BODY_TEMPLATE");
	}

	public String getCurlAIBodyTemplateWithImage() {
		return getValue("CURL_AI_BODY_TEMPLATE_WITH_IMAGE");
	}

	public String getCodeAIModel() {
		return getValue("CODE_AI_MODEL");
	}

	public String getTestAIModel() {
		return getValue("TEST_AI_MODEL");
	}

	public String getFigmaBasePath() {
		return getValue("FIGMA_BASE_PATH");
	}

	public String getFigmaApiKey() {
		return getValue("FIGMA_TOKEN");
	}

	public Integer getDefaultTicketWeightIfNoSPs() {
		String value = getValue("DEFAULT_TICKET_WEIGHT_IF_NO_SP");
		if (value == null) {
			return -1;
		}
		return Integer.parseInt(value);
	}

	public Double getLinesOfCodeDivider() {
		String value = getValue("LINES_OF_CODE_DIVIDER");
		if (value == null) {
			return 1d;
		}
		return Double.parseDouble(value);
	}

	public Double getTimeSpentOnDivider() {
		String value = getValue("TIME_SPENT_ON_DIVIDER");
		if (value == null) {
			return 1d;
		}
		return Double.parseDouble(value);
	}

	public Double getTicketFieldsChangedDivider(String fieldName) {
		String value = getValue("TICKET_FIELDS_CHANGED_DIVIDER_"+ fieldName.toUpperCase());
		if (value == null) {
			String defaultValue = getValue("TICKET_FIELDS_CHANGED_DIVIDER_DEFAULT");
			if (defaultValue != null) {
				return Double.parseDouble(defaultValue);
			}
			return 1d;
		}
		return Double.parseDouble(value);
	}


	public boolean isReadPullRequestDiff() {
		String value = getValue("IS_READ_PULL_REQUEST_DIFF");
		if (value == null) {
			return true;
		}
		return Boolean.parseBoolean(value);
	}

	public String getFirebaseProjectId() {
		return getValue("FIREBASE_PROJECT_ID");
	}

	public String getFirebaseServiceAccountJsonAuth() {
		return getValue("FIREBASE_SERVICE_ACCOUNT_JSON_AUTH");
	}

	/**
	 * Gets the number of retry attempts for AI operations
	 * @return number of retries, default is 3
	 */
	public int getAiRetryAmount() {
		String value = getValue("AI_RETRY_AMOUNT");
		if (value == null || value.trim().isEmpty()) {
			return DEFAULT_AI_RETRY_AMOUNT;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return DEFAULT_AI_RETRY_AMOUNT;
		}
	}

	/**
	 * Gets the delay step in milliseconds between retry attempts
	 * The actual delay will be multiplied by the attempt number
	 * @return delay step in milliseconds, default is 20000 (20 seconds)
	 */
	public long getAiRetryDelayStep() {
		String value = getValue("AI_RETRY_DELAY_STEP");
		if (value == null || value.trim().isEmpty()) {
			return DEFAULT_AI_RETRY_DELAY_STEP;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return DEFAULT_AI_RETRY_DELAY_STEP;
		}
	}

}