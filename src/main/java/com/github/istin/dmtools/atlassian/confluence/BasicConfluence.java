package com.github.istin.dmtools.atlassian.confluence;

import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.confluence.model.ContentResult;

import java.io.IOException;

public class BasicConfluence extends Confluence {

    private final String defaultSpace;

    public BasicConfluence(String basePath, String authorization, String defaultSpace) throws IOException {
        super(basePath, authorization);
        this.defaultSpace = defaultSpace;
    }

    public Content findOrCreate(String title, String parentId, String body) throws IOException {
        Content content = findContent(title, defaultSpace);
        if (content == null) {
            content = createPage(title, parentId, body, defaultSpace);
        }
        return content;
    }

    public Content updatePage(String contentId, String title, String parentId, String body) throws IOException {
        return updatePage(contentId, title, parentId, body, defaultSpace);
    }

    public Content findContent(String title) throws IOException {
        return findContent(title, defaultSpace);
    }

    public ContentResult content(String title) throws IOException {
        return content(title, defaultSpace);
    }
}
