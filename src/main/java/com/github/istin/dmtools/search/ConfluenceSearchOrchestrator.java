package com.github.istin.dmtools.search;

import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.confluence.model.SearchResult;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
        List<SearchResult> searchResults = confluence.searchContentByText(keyword, itemsLimit);
        // Filter out results where entityId is null or type is "attachment"
        return searchResults.stream()
                .filter(searchResult -> searchResult.getEntityId() != null) // Keep only results with non-null entityId
                .filter(searchResult -> !"attachment".equalsIgnoreCase(searchResult.getType())) // Exclude type "attachment"
                .collect(Collectors.toList());
    }

    @Override
    public Object createInitialPlatformContext() {
        return confluence;
    }

    @Override
    protected Object getItemByKey(Object key, List<?> items) {
        for (Object o : items) {
            SearchResult searchResult = (SearchResult) o;
            if (searchResult.getEntityId().equalsIgnoreCase((String) key)) {
                return searchResult;
            }
        }
        return null;
    }

    @Override
    protected String getKeyFieldValue() {
        return SearchResult.ENTITY_ID;
    }

    @Override
    protected String getSourceType() {
        return "confluence";
    }
}