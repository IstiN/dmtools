package com.github.istin.dmtools.di;

import com.github.istin.dmtools.atlassian.bitbucket.BasicBitbucket;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.github.BasicGithub;
import com.github.istin.dmtools.gitlab.BasicGitLab;
import org.json.JSONArray;

import java.io.IOException;

public class SourceCodeFactory {

    public SourceCode createSourceCode(String sourceType) {
        // Assuming a method is available to configure SourceCode with a certain sourceType
        try {
            return SourceCode.Impl.getConfiguredSourceCodes(new JSONArray().put(sourceType)).get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SourceCode createSourceCode(SourceCodeConfig sourceCodeConfig) throws IOException {
        SourceCodeConfig.Type type = sourceCodeConfig.getType();
        switch (type) {
            case GITHUB:
                return new BasicGithub(sourceCodeConfig);
            case BITBUCKET:
                return new BasicBitbucket(sourceCodeConfig);
            case GITLAB:
                return new BasicGitLab(sourceCodeConfig);
            default:
                throw new IllegalArgumentException("Unsupported source code type: " + type);
        }
    }
}