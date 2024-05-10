package com.github.istin.dmtools.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
	private static final String PATH_TO_CONFIG_FILE = "/config.properties";

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

	public String getBitbucketBasePath() {
		return getValue("BITBUCKET_BASE_PATH");
	}

	public String getConfluenceBasePath() {
		return getValue("CONFLUENCE_BASE_PATH");
	}

	public String getConfluenceLoginPassToken() {
		return getValue("CONFLUENCE_LOGIN_PASS_TOKEN");
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

}