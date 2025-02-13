package com.github.istin.dmtools.search;

import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.confluence.model.SearchResult;

import java.io.IOException;
import java.util.List;

public class ConfluenceSearchOrchestrator extends AbstractSearchOrchestrator {

    private final Confluence confluence;

    public ConfluenceSearchOrchestrator(Confluence confluence) {
        this.confluence = confluence;
    }

    @Override
    protected void setupDependencyInjection() {

    }

    @Override
    protected String getItemSnippet(Object item, Object platformContext) {
        SearchResult searchResult = (SearchResult) item;
        return searchResult.getTitle() +
                "\n" +
                searchResult.getExcerpt() +
                "\n" +
                searchResult.getType() +
                "\n" +
                searchResult.getUrl();
    }

    @Override
    protected String getFullItemContent(Object item, Object platformContext) throws IOException {
        SearchResult searchResult = (SearchResult) item;
        Content content = confluence.contentById(searchResult.getEntityId());
        String value = content.getStorage().getValue();
        return content.getTitle() +
                "\n" +
                value +
                "\n" +
                searchResult.getType() +
                "\n" +
                searchResult.getUrl();
    }

    @Override
    protected String getItemResourceKey(Object item) {
        return ((SearchResult) item).getEntityId();
    }

    @Override
    public List<?> searchItemsWithKeywords(String keyword, Object platformContext, int itemsLimit) throws Exception {
       return confluence.searchContentByText(keyword, itemsLimit);
    }

    @Override
    public Object createInitialPlatformContext() {
        return confluence;
    }
}