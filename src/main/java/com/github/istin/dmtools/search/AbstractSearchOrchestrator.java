package com.github.istin.dmtools.search;

import com.github.istin.dmtools.ai.agent.KeywordGeneratorAgent;
import com.github.istin.dmtools.ai.agent.SnippetExtensionAgent;
import com.github.istin.dmtools.ai.agent.SummaryContextAgent;
import com.github.istin.dmtools.di.DaggerAbstractSearchOrchestratorComponent;
import com.github.istin.dmtools.di.DaggerSearchOrchestratorComponent;
import org.json.JSONArray;

import javax.inject.Inject;
import java.util.*;

public abstract class AbstractSearchOrchestrator {

    @Inject
    protected KeywordGeneratorAgent keywordGeneratorAgent;

    @Inject
    protected SnippetExtensionAgent snippetExtensionAgent;

    @Inject
    protected SummaryContextAgent summaryContextAgent;

    public AbstractSearchOrchestrator() {
        DaggerAbstractSearchOrchestratorComponent.create().inject(this);
        setupDependencyInjection();
    }

    // Setup DI for platform-specific implementations
    protected abstract void setupDependencyInjection();

    /**
     * Process a single item iteratively: small fragments first, followed by full content if needed.
     */
    protected boolean processItem(Object item, String fullTask, StringBuffer contextSummary, Object platformContext) throws Exception {
        String itemSnippet = getItemSnippet(item, platformContext); // Abstract method for getting snippet
        String resourceKey = getItemResourceKey(item); // Abstract method for unique identifier (e.g., URI or key)

        // Step 1: Process the snippet first using SnippetExtensionAgent
        if (snippetExtensionAgent.run(new SnippetExtensionAgent.Params(itemSnippet, fullTask))) {
            String fullContent = getFullItemContent(item, platformContext); // Abstract method for full content
            String response = summaryContextAgent.run(new SummaryContextAgent.Params(
                    fullTask,
                    formatFullItemResponse(resourceKey, fullContent)
            ));
            if (!response.isEmpty()) {
                contextSummary.append("\n").append(response);
                return true;
            }
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

    public String run(String fullTask, String keywordsBlacklist, int itemsLimit, int iterations) throws Exception {
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
                for (Object item : items) {
                    if (processedItems.contains(getItemResourceKey(item))) continue;

                    if (processItem(item, fullTask, contextSummary, platformContext)) {
                        processedItems.add(getItemResourceKey(item));
                    }
                }
            }

            // Step 3: Update blacklist for the next iteration
            keywordsBlacklist = updateBlacklist(keywordsBlacklist, keywords);
        }

        return contextSummary.toString();
    }
}