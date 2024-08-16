package com.github.istin.dmtools.github;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.utils.PropertyReader;

import java.io.IOException;

public class BasicGithub extends GitHub {

    private static final String BASE_PATH;
    private static final String TOKEN;
    private static final String WORKSPACE;
    private static final String REPOSITORY;
    private static final String BRANCH;

    static {
        PropertyReader propertyReader = new PropertyReader();
        BASE_PATH = propertyReader.getGithubBasePath();
        TOKEN = propertyReader.getGithubToken();
        WORKSPACE = propertyReader.getGithubWorkspace();
        REPOSITORY = propertyReader.getGithubRepository();
        BRANCH = propertyReader.getGithubBranch();
    }


    public BasicGithub() throws IOException {
        super(BASE_PATH, TOKEN);
    }

    private static BasicGithub instance;

    public static SourceCode getInstance() throws IOException {
        if (instance == null) {
            instance = new BasicGithub();
        }
        return instance;
    }


    @Override
    public String getDefaultRepository() {
        return REPOSITORY;
    }

    @Override
    public String getDefaultBranch() {
        return BRANCH;
    }

    @Override
    public String getDefaultWorkspace() {
        return WORKSPACE;
    }

    @Override
    public boolean isConfigured() {
        return BASE_PATH != null || TOKEN != null || REPOSITORY != null || BRANCH != null || WORKSPACE != null;
    }

}