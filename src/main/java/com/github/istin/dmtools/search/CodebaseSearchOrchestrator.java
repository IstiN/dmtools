package com.github.istin.dmtools.search;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.ITextMatch;
import com.github.istin.dmtools.di.SourceCodeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            if (file.getSelfLink().equalsIgnoreCase((String) key)) {
                return file;
            }
        }
        return null;
    }

    @Override
    protected String getKeyFieldValue() {
        return "url";
    }

    @Override
    protected String getSourceType() {
        return "files";
    }
}