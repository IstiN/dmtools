package com.github.istin.dmtools.atlassian.bitbucket;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.utils.PropertyReader;

import java.io.IOException;

public class BasicBitbucket extends Bitbucket {

    private static final String BASE_PATH;
    private static final String TOKEN;
    private static final String WORKSPACE;
    private static final String REPOSITORY;
    private static final String BRANCH;
    private static final Bitbucket.ApiVersion API_VERSION;

    static {
        PropertyReader propertyReader = new PropertyReader();
        BASE_PATH = propertyReader.getBitbucketBasePath();
        TOKEN = propertyReader.getBitbucketToken();
        API_VERSION = Bitbucket.ApiVersion.valueOf(propertyReader.getBitbucketApiVersion());
        WORKSPACE = propertyReader.getBitbucketWorkspace();
        REPOSITORY = propertyReader.getBitbucketRepository();
        BRANCH = propertyReader.getBitbucketBranch();
    }

    public BasicBitbucket() throws IOException {
        super(BASE_PATH, TOKEN);
        setApiVersion(API_VERSION);
    }

    private static BasicBitbucket instance;

    public static SourceCode getInstance() throws IOException {
        if (instance == null) {
            instance = new BasicBitbucket();
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

}
