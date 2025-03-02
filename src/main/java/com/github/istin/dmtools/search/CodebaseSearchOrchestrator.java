package com.github.istin.dmtools.search;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.ITextMatch;
import com.github.istin.dmtools.di.SourceCodeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CodebaseSearchOrchestrator extends AbstractSearchOrchestrator {

    private final SourceCodeConfig[] sourceCodeConfigs;
    private List<SourceCode> sourceCodes;
    public CodebaseSearchOrchestrator(SourceCodeConfig[] sourceCodeConfigs) throws IOException {
        this.sourceCodeConfigs = sourceCodeConfigs;
        sourceCodes = new SourceCodeFactory().createSourceCodes(sourceCodeConfigs);
    }

    @Override
    protected void setupDependencyInjection() {
    }

    @Override
    protected String getItemSnippet(Object item, Object platformContext) {
        IFile file = (IFile) item;
        StringBuilder snippet = new StringBuilder();
        for (ITextMatch match : file.getTextMatches()) {
            snippet.append(match.getFragment()).append("\n");
        }
        return snippet.toString();
    }

    @Override
    protected String getFullItemContent(Object item, Object platformContext) throws Exception {
        IFile file = (IFile) item;
        StringBuffer stringBuffer = new StringBuffer();
        for (SourceCode sourceCode : sourceCodes) {
            String fileContent = sourceCode.getFileContent(file.getSelfLink());
            stringBuffer.append(file).append("\n");
            stringBuffer.append(fileContent).append("\n");
        }
        return stringBuffer.toString();
    }

    @Override
    protected String getItemResourceKey(Object item) {
        return ((IFile) item).getSelfLink();
    }

    @Override
    protected void preprocessing(ProcessingType processingType, StringBuffer contextSummary, SearchStats searchStats, String fullTask, Object platformContext) throws Exception {
        List<IFile> result = new ArrayList<>();
        for (SourceCode sourceCode : sourceCodes) {
            List<IFile> files = sourceCode.getListOfFiles(
                    sourceCode.getDefaultWorkspace(),
                    sourceCode.getDefaultRepository(),
                    sourceCode.getDefaultBranch()
            );
            result.addAll(files);
        }
        result = result.stream().filter(file -> !file.isDir()).collect(Collectors.toList());

        // Split the result list into chunks of 5000
        int chunkSize = 2000;
        List<List<IFile>> chunks = new ArrayList<>();
        for (int i = 0; i < result.size(); i += chunkSize) {
            chunks.add(result.subList(i, Math.min(i + chunkSize, result.size())));
        }

        // Create a thread pool with 10 threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Submit tasks for parallel processing
        List<Future<Void>> futures = new ArrayList<>();
        for (List<IFile> chunk : chunks) {
            futures.add(executorService.submit(() -> {
                checkBulkSearchResults(processingType, fullTask, chunk, null, contextSummary, platformContext, null);
                return null;
            }));
        }

        // Wait for all tasks to complete
        for (Future<Void> future : futures) {
            future.get(); // This will block until the task is complete
        }

        // Shutdown the executor service
        executorService.shutdown();
    }

    @Override
    public List<IFile> searchItemsWithKeywords(String keyword, Object platformContext, int itemsLimit) throws Exception {
        List<IFile> result = new ArrayList<>();

        for (SourceCode sourceCode : sourceCodes) {
            List<IFile> files = sourceCode.searchFiles(
                    sourceCode.getDefaultWorkspace(),
                    sourceCode.getDefaultRepository(),
                    keyword,
                    itemsLimit
            );
            result.addAll(files);
        }

        return result;
    }

    @Override
    public Object createInitialPlatformContext() {
        return sourceCodes;
    }

    @Override
    protected Object getItemByKey(Object key, List<?> items) {
        for (Object o : items) {
            IFile file = (IFile) o;
            if (file.getPath().equalsIgnoreCase((String) key)) {
                return file;
            }
        }
        return null;
    }

    @Override
    protected String getKeyFieldValue() {
        return "path";
    }

    @Override
    protected String getSourceType() {
        return "files";
    }
}