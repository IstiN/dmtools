package com.github.istin.dmtools.atlassian.confluence;

import com.github.istin.dmtools.atlassian.confluence.model.Attachment;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.confluence.model.ContentResult;
import com.github.istin.dmtools.common.kb.KnowledgeBaseConfig;
import com.github.istin.dmtools.common.utils.PropertyReader;
import com.github.istin.dmtools.report.ReportUtils;
import com.github.istin.dmtools.report.freemarker.GenericReport;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BasicConfluence extends Confluence {


    public static KnowledgeBaseConfig CONFIG;

    static {
        PropertyReader propertyReader = new PropertyReader();
        CONFIG = new KnowledgeBaseConfig();
        CONFIG.setPath(propertyReader.getConfluenceBasePath());
        CONFIG.setAuth(propertyReader.getConfluenceLoginPassToken());
        CONFIG.setType(KnowledgeBaseConfig.Type.CONFLUENCE);
        CONFIG.setWorkspace(propertyReader.getConfluenceDefaultSpace());
        CONFIG.setGraphQLPath(propertyReader.getConfluenceGraphQLPath());
    }

    private static BasicConfluence instance;

    public static BasicConfluence getInstance() throws IOException {
        if (instance == null) {
            instance = new BasicConfluence(CONFIG.getPath(), CONFIG.getAuth(), CONFIG.getWorkspace());
            instance.setGraphQLPath(CONFIG.getGraphQLPath());
        }
        return instance;
    }

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

    public Content updatePage(Content content, String body) throws IOException {
        return updatePage(content.getId(), content.getTitle(), content.getParentId(), body, defaultSpace);
    }

    public Content updatePage(String contentId, String title, String parentId, String body) throws IOException {
        return updatePage(contentId, title, parentId, body, defaultSpace);
    }

    public String getDefaultSpace() {
        return defaultSpace;
    }

    public Content findContent(String title) throws IOException {
        return findContent(title, defaultSpace);
    }

    public ContentResult content(String title) throws IOException {
        return content(title, defaultSpace);
    }

    public List<Attachment> contentAttachments(String contentId) throws IOException {
        return getContentAttachments(contentId);
    }

    public Content publishPageToDefaultSpace(String rootPage, String subPage, GenericReport genericReport) throws IOException, TemplateException {
        String name = genericReport.getName();
        new ReportUtils().write(name, "table_report", genericReport, null);
        String subPageName = subPage == null ? rootPage : subPage;
        Content content = findOrCreate(name, findContent(subPageName).getId(), "");
        File file = new File("reports/" + ReportUtils.getReportFileName(name) + ".html");

        String fileContent = FileUtils.readFileToString(file, "UTF-8");

        updatePage(content.getId(), name, findContent(subPageName).getId(), macroCloudHTML(fileContent));
        return content;
    }

    public void attachFileToPageInDefaultSpace(String contentTitle, File file) throws IOException {
        Content content = findContent(contentTitle);
        attachFileToPage(content.getId(), file);
        insertImageInPageBody(defaultSpace, content.getId(), file.getName());
    }

    public List<Content> getChildrenOfContentByName(String contentName) throws IOException {
        return getChildrenOfContentByName(getDefaultSpace(), contentName);
    }
}
