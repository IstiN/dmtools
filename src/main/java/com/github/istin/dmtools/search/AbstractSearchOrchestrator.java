package com.github.istin.dmtools.search;

import com.github.istin.dmtools.ai.agent.KeywordGeneratorAgent;
import com.github.istin.dmtools.ai.agent.SearchResultsAssessmentAgent;
import com.github.istin.dmtools.ai.agent.SnippetExtensionAgent;
import com.github.istin.dmtools.ai.agent.SummaryContextAgent;
import com.github.istin.dmtools.di.DaggerAbstractSearchOrchestratorComponent;
import org.json.JSONArray;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractSearchOrchestrator {

    @Inject
    protected KeywordGeneratorAgent keywordGeneratorAgent;

    @Inject
    protected SnippetExtensionAgent snippetExtensionAgent;

    @Inject
    protected SummaryContextAgent summaryContextAgent;

    @Inject
    protected SearchResultsAssessmentAgent searchResultsAssessmentAgent;

    public AbstractSearchOrchestrator() {
        DaggerAbstractSearchOrchestratorComponent.create().inject(this);
        setupDependencyInjection();
    }

    // Setup DI for platform-specific implementations
    protected abstract void setupDependencyInjection();

    /**
     * Process a single item iteratively: small fragments first, followed by full content if needed.
     */
    protected boolean processItem(ProcessingType processingType, Object item, String fullTask, StringBuffer contextSummary, Object platformContext) throws Exception {
        String itemSnippet = getItemSnippet(item, platformContext); // Abstract method for getting snippet
        String resourceKey = getItemResourceKey(item); // Abstract method for unique identifier (e.g., URI or key)

        // Step 1: Process the snippet first using SnippetExtensionAgent
        if (processingType == ProcessingType.ONE_BY_ONE) {
            if (snippetExtensionAgent.run(new SnippetExtensionAgent.Params(itemSnippet, fullTask))) {
                if (processeFullContent(item, fullTask, contextSummary, platformContext, resourceKey)) return true;
            }
            // Step 2: Fallback to processing just the snippet
            String snippetResponse = summaryContextAgent.run(new SummaryContextAgent.Params(
                    fullTask,
                    formatSnippetResponse(resourceKey, itemSnippet)
            ));
            if (!snippetResponse.isEmpty()) {
                contextSummary.append("\n").append(snippetResponse);
                return true;
            }
        } else {
            if (processeFullContent(item, fullTask, contextSummary, platformContext, resourceKey)) return true;
        }

        return false;
    }

    private boolean processeFullContent(Object item, String fullTask, StringBuffer contextSummary, Object platformContext, String resourceKey) throws Exception {
        String fullContent = getFullItemContent(item, platformContext); // Abstract method for full content
        String response = summaryContextAgent.run(new SummaryContextAgent.Params(
                fullTask,
                formatFullItemResponse(resourceKey, fullContent)
        ));
        if (!response.isEmpty()) {
            contextSummary.append("\n").append(response);
            return true;
        }
        return false;
    }

    // Abstract method to get the snippet for a platform-specific item
    protected abstract String getItemSnippet(Object item, Object platformContext) throws Exception;

    // Abstract method to get the full content for the platform-specific item
    protected abstract String getFullItemContent(Object item, Object platformContext) throws Exception;

    // Abstract method to get the unique resource key for an item (e.g., file selfLink or ticket key)
    protected abstract String getItemResourceKey(Object item);

    // Helper to format response for snippet processing
    protected String formatSnippetResponse(String resourceKey, String snippetContent) {
        return "Snippet for [" + resourceKey + "]:\n" + snippetContent;
    }

    // Helper to format response for full content processing
    protected String formatFullItemResponse(String resourceKey, String fullContent) {
        return "Full content for [" + resourceKey + "]:\n" + fullContent;
    }

    // The rest of the agent interaction logic (like in the previous refactor)
    protected JSONArray generateKeywords(String fullTask, String blacklist, String contextSummary) throws Exception {
        String extendedTask = fullTask;
        if (!contextSummary.isEmpty()) {
            extendedTask += "\nContext:\n" + contextSummary;
        }
        return keywordGeneratorAgent.run(new KeywordGeneratorAgent.Params(extendedTask, blacklist));
    }

    protected String updateBlacklist(String currentBlacklist, JSONArray keywords) {
        StringBuilder updatedBlacklist = new StringBuilder(currentBlacklist);
        for (int i = 0; i < keywords.length(); i++) {
            if (updatedBlacklist.length() > 0) {
                updatedBlacklist.append(",");
            }
            updatedBlacklist.append(keywords.getString(i));
        }
        return updatedBlacklist.toString();
    }

    public abstract List<?> searchItemsWithKeywords(String keyword, Object platformContext, int itemsLimit) throws Exception;

    public abstract Object createInitialPlatformContext();

    public enum ProcessingType {
        ONE_BY_ONE, BULK
    }

    public String run(ProcessingType processingType, String fullTask, String keywordsBlacklist, int itemsLimit, int iterations) throws Exception {
        StringBuffer contextSummary = new StringBuffer();
        Set<String> processedItems = new HashSet<>();
        Object platformContext = createInitialPlatformContext();

        for (int iteration = 0; iteration < iterations; iteration++) {

            // Step 1: Generate keywords
            JSONArray keywords = generateKeywords(fullTask, keywordsBlacklist, contextSummary.toString());
            System.out.println("Iteration " + (iteration + 1) + " Keywords: " + keywords);

            // Step 2: Search items using the generated keywords
            for (int i = 0; i < keywords.length(); i++) {
                String keyword = keywords.getString(i);

                List<?> items = searchItemsWithKeywords(keyword, platformContext, itemsLimit);
                if (processingType == ProcessingType.ONE_BY_ONE) {
                    for (Object item : items) {
                        if (processedItems.contains(getItemResourceKey(item))) continue;

                        if (processItem(processingType, item, fullTask, contextSummary, platformContext)) {
                            processedItems.add(getItemResourceKey(item));
                        }
                    }
                } else {
                    if (items.isEmpty()) {
                        continue;
                    }

                    // Run assessment
                    SearchResultsAssessmentAgent.Params params = new SearchResultsAssessmentAgent.Params(
                            getSourceType(),
                            getKeyFieldValue(),
                            fullTask,
                            items.toString()
                    );

                    JSONArray relevantKeys = searchResultsAssessmentAgent.run(params);

                    // Convert JSONArray to List<String>
                    for (int j = 0; j < relevantKeys.length(); j++) {
                        Object key = relevantKeys.get(j);
                        if (processedItems.contains(key)) continue;
                        Object itemByKey = getItemByKey(key, items);
                        if (itemByKey != null && processItem(processingType, itemByKey, fullTask, contextSummary, platformContext)) {
                            processedItems.add(String.valueOf(key));
                        }
                    }
                }
            }

            // Step 3: Update blacklist for the next iteration
            keywordsBlacklist = updateBlacklist(keywordsBlacklist, keywords);
        }

        return contextSummary.toString();
    }

    protected abstract Object getItemByKey(Object key, List<?> items);

    protected abstract String getKeyFieldValue();

    protected abstract String getSourceType();
}