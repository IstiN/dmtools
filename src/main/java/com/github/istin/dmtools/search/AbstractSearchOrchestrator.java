package com.github.istin.dmtools.search;

import com.github.istin.dmtools.ai.agent.KeywordGeneratorAgent;
import com.github.istin.dmtools.ai.agent.SearchResultsAssessmentAgent;
import com.github.istin.dmtools.ai.agent.SnippetExtensionAgent;
import com.github.istin.dmtools.ai.agent.SummaryContextAgent;
import com.github.istin.dmtools.common.model.ToText;
import com.github.istin.dmtools.di.DaggerAbstractSearchOrchestratorComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.*;

public abstract class AbstractSearchOrchestrator {

    public static class SearchStats {
        private final List<IterationStats> iterations = new ArrayList<>();
        private final Set<String> processedKeys = new HashSet<>();
        private int totalItemsProcessed = 0;

        public static class IterationStats {
            private final int iterationNumber;
            private final List<String> keywords;
            private final Map<String, KeywordStats> keywordStats = new HashMap<>();

            public IterationStats(int iterationNumber, List<String> keywords) {
                this.iterationNumber = iterationNumber;
                this.keywords = keywords;
            }

            public static class KeywordStats {
                private final int itemsFound;
                private final List<String> processedKeys;
                private final List<String> successfulKeys;

                public KeywordStats(int itemsFound, List<String> processedKeys, List<String> successfulKeys) {
                    this.itemsFound = itemsFound;
                    this.processedKeys = processedKeys;
                    this.successfulKeys = successfulKeys;
                }

                public JSONObject toJson() {
                    JSONObject json = new JSONObject();
                    json.put("itemsFound", itemsFound);
                    json.put("processedKeys", new JSONArray(processedKeys));
                    json.put("successfulKeys", new JSONArray(successfulKeys));
                    return json;
                }
            }

            public JSONObject toJson() {
                JSONObject json = new JSONObject();
                json.put("iterationNumber", iterationNumber);
                json.put("keywords", new JSONArray(keywords));
                JSONObject keywordStatsJson = new JSONObject();
                keywordStats.forEach((keyword, stats) -> keywordStatsJson.put(keyword, stats.toJson()));
                json.put("keywordStats", keywordStatsJson);
                return json;
            }
        }

        public void addIterationStats(IterationStats stats) {
            iterations.add(stats);
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("totalItemsProcessed", totalItemsProcessed);
            json.put("totalProcessedKeys", new JSONArray(processedKeys));
            JSONArray iterationsJson = new JSONArray();
            iterations.forEach(iteration -> iterationsJson.put(iteration.toJson()));
            json.put("iterations", iterationsJson);
            return json;
        }
    }

    private SearchStats searchStats;

    public SearchStats getSearchStats() {
        return searchStats;
    }

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

    protected abstract void setupDependencyInjection();

    protected boolean processItem(ProcessingType processingType, Object item, String fullTask, StringBuffer contextSummary, Object platformContext) throws Exception {
        String itemSnippet = getItemSnippet(item, platformContext);
        String resourceKey = getItemResourceKey(item);

        if (processingType == ProcessingType.ONE_BY_ONE) {
            if (snippetExtensionAgent.run(new SnippetExtensionAgent.Params(itemSnippet, fullTask))) {
                if (processFullContent(item, fullTask, contextSummary, platformContext, resourceKey)) return true;
            }
            String snippetResponse = summaryContextAgent.run(new SummaryContextAgent.Params(
                    fullTask,
                    formatSnippetResponse(resourceKey, itemSnippet)
            ));
            if (!snippetResponse.isEmpty()) {
                contextSummary.append("\n").append(snippetResponse);
                return true;
            }
        } else {
            if (processFullContent(item, fullTask, contextSummary, platformContext, resourceKey)) return true;
        }

        return false;
    }

    private boolean processFullContent(Object item, String fullTask, StringBuffer contextSummary, Object platformContext, String resourceKey) throws Exception {
        String fullContent = getFullItemContent(item, platformContext);
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

    protected abstract String getItemSnippet(Object item, Object platformContext) throws Exception;
    protected abstract String getFullItemContent(Object item, Object platformContext) throws Exception;
    protected abstract String getItemResourceKey(Object item);

    protected String formatSnippetResponse(String resourceKey, String snippetContent) {
        return "Snippet for [" + resourceKey + "]:\n" + snippetContent;
    }

    protected String formatFullItemResponse(String resourceKey, String fullContent) {
        return "Full content for [" + resourceKey + "]:\n" + fullContent;
    }

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
        searchStats = new SearchStats();
        Object platformContext = createInitialPlatformContext();

        for (int iteration = 0; iteration < iterations; iteration++) {
            JSONArray keywords = generateKeywords(fullTask, keywordsBlacklist, contextSummary.toString());
            List<String> keywordsList = new ArrayList<>();
            for (int i = 0; i < keywords.length(); i++) {
                keywordsList.add(keywords.getString(i));
            }

            SearchStats.IterationStats iterationStats = new SearchStats.IterationStats(iteration + 1, keywordsList);

            for (String keyword : keywordsList) {
                List<?> items = searchItemsWithKeywords(keyword, platformContext, itemsLimit);
                List<String> processedKeysForKeyword = new ArrayList<>();
                List<String> successfulKeysForKeyword = new ArrayList<>();

                if (processingType == ProcessingType.ONE_BY_ONE) {
                    for (Object item : items) {
                        String itemKey = getItemResourceKey(item);
                        if (searchStats.processedKeys.contains(itemKey)) continue;

                        processedKeysForKeyword.add(itemKey);
                        if (processItem(processingType, item, fullTask, contextSummary, platformContext)) {
                            successfulKeysForKeyword.add(itemKey);
                            searchStats.processedKeys.add(itemKey);
                        }
                    }
                } else {
                    if (!items.isEmpty()) {
                        String string;
                        if (items.get(0) instanceof ToText) {
                            string = ToText.Utils.toText((List<? extends ToText>) items);
                        } else {
                            string = items.toString();
                        }

                        SearchResultsAssessmentAgent.Params params = new SearchResultsAssessmentAgent.Params(
                                getSourceType(),
                                getKeyFieldValue(),
                                fullTask,
                                string
                        );

                        JSONArray relevantKeys = searchResultsAssessmentAgent.run(params);

                        for (int j = 0; j < relevantKeys.length(); j++) {
                            Object key = relevantKeys.get(j);
                            String keyStr = String.valueOf(key);
                            if (searchStats.processedKeys.contains(keyStr)) continue;

                            Object itemByKey = getItemByKey(key, items);
                            if (itemByKey != null) {
                                processedKeysForKeyword.add(keyStr);
                                if (processItem(processingType, itemByKey, fullTask, contextSummary, platformContext)) {
                                    successfulKeysForKeyword.add(keyStr);
                                    searchStats.processedKeys.add(keyStr);
                                }
                            }
                        }
                    }
                }

                iterationStats.keywordStats.put(keyword, new SearchStats.IterationStats.KeywordStats(
                        items.size(),
                        processedKeysForKeyword,
                        successfulKeysForKeyword
                ));

                searchStats.totalItemsProcessed += items.size();
            }

            searchStats.addIterationStats(iterationStats);
            keywordsBlacklist = updateBlacklist(keywordsBlacklist, keywords);
        }

        return contextSummary.toString();
    }

    protected abstract Object getItemByKey(Object key, List<?> items);
    protected abstract String getKeyFieldValue();
    protected abstract String getSourceType();
}