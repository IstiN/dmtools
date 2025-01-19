package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.common.model.ToText;

import java.io.IOException;
import java.util.List;

public class ConfluencePagesContext implements ToText {

    private StringBuffer stringBuffer = new StringBuffer();

    public ConfluencePagesContext(String[] confluencePages, Confluence confluence) throws IOException {
        if (confluencePages != null) {
            List<Content> contents = confluence.contentsByUrls(confluencePages);
            stringBuffer.append("Confluence pages\n");
            for (Content content : contents) {
                stringBuffer.append(content.getTitle()).append("\n").append(content.getStorage().getValue()).append("\n");
            }
        }
    }

    @Override
    public String toText() throws IOException {
        return stringBuffer.toString();
    }
}
