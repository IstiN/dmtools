package com.github.istin.dmtools.gitlab;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.utils.PropertyReader;

import java.io.IOException;

public class BasicGitLab extends GitLab {

    private static final String BASE_PATH;
    private static final String TOKEN;
    private static final String WORKSPACE;
    private static final String REPOSITORY;
    private static final String BRANCH;

    static {
        PropertyReader propertyReader = new PropertyReader();
        BASE_PATH = propertyReader.getGitLabBasePath();
        TOKEN = propertyReader.getGitLabToken();
        WORKSPACE = propertyReader.getGitLabWorkspace();
        REPOSITORY = propertyReader.getGitLabRepository();
        BRANCH = propertyReader.getGitLabBranch();
    }


    public BasicGitLab() throws IOException {
        super(BASE_PATH, TOKEN);
    }

    private static BasicGitLab instance;

    public static SourceCode getInstance() throws IOException {
        if (instance == null) {
            instance = new BasicGitLab();
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
        return BASE_PATH != null || TOKEN != null || WORKSPACE != null;
    }

}