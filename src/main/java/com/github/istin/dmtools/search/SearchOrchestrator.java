package com.github.istin.dmtools.search;

import com.github.istin.dmtools.ai.agent.KeywordGeneratorAgent;
import com.github.istin.dmtools.ai.agent.RequestSimplifierAgent;
import com.github.istin.dmtools.ai.agent.SnippetExtensionAgent;
import com.github.istin.dmtools.ai.agent.SummaryContextAgent;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.ITextMatch;
import com.github.istin.dmtools.di.DaggerSearchOrchestratorComponent;
import com.github.istin.dmtools.di.SourceCodeFactory;
import org.json.JSONArray;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

public class SearchOrchestrator {

    @Inject
    KeywordGeneratorAgent keywordGeneratorAgent;

    @Inject
    SnippetExtensionAgent snippetExtensionAgent;

    @Inject
    SummaryContextAgent summaryContextAgent;


    public SearchOrchestrator() {
        DaggerSearchOrchestratorComponent.create().inject(this);
    }

    private JSONArray generateKeywords(String fullTask, String blacklist, String contextSummary) throws Exception {
        String extendedTask = fullTask;
        if (!contextSummary.isEmpty()) {
            extendedTask += "\nContext from previous search:\n" + contextSummary;
        }
        return keywordGeneratorAgent.run(new KeywordGeneratorAgent.Params(extendedTask, blacklist));
    }

    private String updateBlacklist(String currentBlacklist, JSONArray keywords) {
        StringBuilder updatedBlacklist = new StringBuilder(currentBlacklist);
        for (int i = 0; i < keywords.length(); i++) {
            if (updatedBlacklist.length() > 0) {
                updatedBlacklist.append(",");
            }
            updatedBlacklist.append(keywords.getString(i));
        }
        return updatedBlacklist.toString();
    }

    private Map<String, List<IFile>> searchFilesWithKeywords(JSONArray keywords, Set<String> checkedFiles,
                                                             String fullTask, StringBuffer filesContextSummary, SourceCodeConfig[] sourceCodeConfig, int filesLimit) throws Exception {
        Map<String, List<IFile>> mapping = new HashMap<>();

        for (int i = 0; i < keywords.length(); i++) {
            String keyword = keywords.getString(i);
            Map<SourceCode, List<IFile>> sourceCodeFiles = searchInSourceCodes(keyword, sourceCodeConfig, filesLimit);

            if (sourceCodeFiles.isEmpty()) {
                continue;
            }

            List<IFile> allFiles = new ArrayList<>();
            for (Map.Entry<SourceCode, List<IFile>> entry : sourceCodeFiles.entrySet()) {
                processFiles(entry.getValue(), checkedFiles, fullTask, filesContextSummary,
                        filesLimit, entry.getKey());
                allFiles.addAll(entry.getValue());
            }
            mapping.put(keyword, allFiles);
        }

        return mapping;
    }

    private Map<SourceCode, List<IFile>> searchInSourceCodes(String keyword, SourceCodeConfig[] sourceCodeConfigs, int filesLimit) throws IOException, InterruptedException {
        Map<SourceCode, List<IFile>> sourceCodeFiles = new HashMap<>();
        List<SourceCode> sourceCodes = new SourceCodeFactory().createSourceCodes(sourceCodeConfigs);

        for (SourceCode sourceCode : sourceCodes) {
            List<IFile> files = sourceCode.searchFiles(
                    sourceCode.getDefaultWorkspace(),
                    sourceCode.getDefaultRepository(),
                    keyword,
                    filesLimit
            );
            if (!files.isEmpty()) {
                sourceCodeFiles.put(sourceCode, files);
            }
        }
        return sourceCodeFiles;
    }

    private void processFiles(List<IFile> files, Set<String> checkedFiles, String fullTask,
                              StringBuffer filesContextSummary, int filesLimit, SourceCode sourceCode) throws Exception {
        int counterOfInvalidResponses = 0;
        int filesCounter = 0;

        for (IFile file : files) {
            String selfLink = file.getSelfLink();
            if (checkedFiles.contains(selfLink)) {
                continue;
            }

            if (processFile(file, selfLink, fullTask, filesContextSummary, sourceCode)) {
                counterOfInvalidResponses = 0;
            } else {
                counterOfInvalidResponses++;
            }

            checkedFiles.add(selfLink);
            filesCounter++;

            if (shouldBreakProcessing(counterOfInvalidResponses, filesCounter, filesLimit)) {
                break;
            }
        }
    }

    private boolean processFile(IFile file, String selfLink, String fullTask, StringBuffer filesContextSummary, SourceCode sourceCode) throws Exception {
        StringBuffer buffer = new StringBuffer();
        for (ITextMatch textMatch : file.getTextMatches()) {
            buffer.append(textMatch.getFragment()).append("\n");
        }

        System.out.println("file selflink: " + selfLink);
        String response = getFileResponse(buffer.toString(), selfLink, fullTask, sourceCode);

        if (!response.isEmpty()) {
            filesContextSummary.append("\n").append(response);
            return true;
        }
        return false;
    }

    private String getFileResponse(String content, String selfLink, String fullTask, SourceCode sourceCode) throws Exception {
        if (snippetExtensionAgent.run(new SnippetExtensionAgent.Params(content, fullTask))) {
            return summaryContextAgent.run(new SummaryContextAgent.Params(
                    fullTask,
                    "file " + selfLink + " " + sourceCode.getFileContent(selfLink)
            ));
        }
        return summaryContextAgent.run(new SummaryContextAgent.Params(
                fullTask,
                "file snippet " + selfLink + " " + content
        ));
    }

    private boolean shouldBreakProcessing(int invalidResponses, int filesCounter, int filesLimit) {
        return invalidResponses > 10 || filesCounter >= filesLimit;
    }

    public String run(RequestSimplifierAgent.Result structuredRequest, String keywordsBlacklist, SourceCodeConfig[] sourceCodeConfig, int filesLimit) throws Exception {

        String fullTask = structuredRequest.toString();
        StringBuffer filesContextSummary = new StringBuffer();

        // Process for up to 2 iterations
        Set<String> checkedFiles = new HashSet<>();
        for (int iteration = 0; iteration < 1; iteration++) {

            JSONArray keywords = generateKeywords(fullTask, keywordsBlacklist, filesContextSummary.toString());
            System.out.println("Iteration " + (iteration + 1) + " keywords: " + keywords);

            Map<String, List<IFile>> mapping = searchFilesWithKeywords(keywords, checkedFiles, fullTask, filesContextSummary, sourceCodeConfig, filesLimit);

            keywordsBlacklist = updateBlacklist(keywordsBlacklist, keywords);
        }

        return filesContextSummary.toString();
    }
}
