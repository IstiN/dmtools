package com.github.istin.dmtools.context;

import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.di.SourceCodeFactory;
import com.github.istin.dmtools.figma.BasicFigmaClient;
import com.github.istin.dmtools.figma.FigmaClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UriToObjectFactory {

    public List<? extends UriToObject> createUriProcessingSources(SourceCodeConfig... sourceCodeConfigs) throws IOException {
        List<SourceCode> sourceCodesOrDefault = new SourceCodeFactory().createSourceCodesOrDefault(sourceCodeConfigs);
        List<UriToObject> result = new ArrayList<>();
        for (SourceCode sourceCode : sourceCodesOrDefault) {
            if (sourceCode instanceof UriToObject) {
                result.add((UriToObject) sourceCode);
            }
        }
        TrackerClient<? extends ITicket> jiraClient = BasicJiraClient.getInstance();
        if (jiraClient != null) {
            result.add((UriToObject) jiraClient);
        }
        Confluence basicConfluence = BasicConfluence.getInstance();
        if (basicConfluence != null) {
            result.add(basicConfluence);
        }
        FigmaClient figmaClient = BasicFigmaClient.getInstance();
        if (figmaClient != null) {
            result.add((UriToObject) figmaClient);
        }
        return result;
    }

}