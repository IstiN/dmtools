package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.common.model.ToText;
import com.github.istin.dmtools.context.ContextOrchestrator;

import java.io.IOException;
import java.util.List;

public class ConfluencePagesContext implements ToText {

    private StringBuffer stringBuffer = new StringBuffer();

    public ConfluencePagesContext(String[] confluencePages, Confluence confluence) throws Exception {
        this(null, confluencePages, confluence);
    }

    public ConfluencePagesContext(ContextOrchestrator contextOrchestrator, String[] confluencePages, Confluence confluence) throws Exception {
        if (confluencePages != null) {
            List<Content> contents = confluence.contentsByUrls(confluencePages);
            stringBuffer.append("Confluence pages\n");
            for (Content content : contents) {
                String pageContent = content.getTitle() + "\n" + content.getStorage().getValue();
                stringBuffer.append(pageContent).append("\n");
                if (contextOrchestrator != null) {
                    contextOrchestrator.processFullContent(content.getViewUrl(confluence.getBasePath()), pageContent, confluence, null, 0);
                }
            }
        }
    }

    @Override
    public String toText() throws IOException {
        return stringBuffer.toString();
    }
}
