package com.github.istin.dmtools.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

	private static String PATH_TO_CONFIG_FILE = "/config.properties";

	public static void setConfigFile(String resourcePath) {
		PATH_TO_CONFIG_FILE = resourcePath;
	}


	private static Properties prop;

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
		return getValue("GITHUB_TOKEN");
	}

	public String getGithubWorkspace() {
		return getValue("GITHUB_WORKSPACE");
	}

	public String getGithubRepository() {
		return getValue("GITHUB_REPOSITORY");
	}

	public String getGithubBranch() {
		return getValue("GITHUB_BRANCH");
	}

	public String getGithubBasePath() {
		return getValue("GITHUB_BASE_PATH");
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

	public String getCodeAIModel() {
		return getValue("CODE_AI_MODEL");
	}

	public String getFigmaBasePath() {
		return getValue("FIGMA_BASE_PATH");
	}

	public String getFigmaApiKey() {
		return getValue("FIGMA_TOKEN");
	}

}