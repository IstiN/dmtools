package com.github.istin.dmtools.atlassian.confluence;

import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.confluence.model.ContentResult;
import com.github.istin.dmtools.report.ReportUtils;
import com.github.istin.dmtools.report.freemarker.GenericReport;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;

import java.io.File;
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
}
