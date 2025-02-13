package com.github.istin.dmtools.search;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.ITextMatch;
import com.github.istin.dmtools.di.SourceCodeFactory;

import java.util.ArrayList;
import java.util.List;

public class CodebaseSearchOrchestrator extends AbstractSearchOrchestrator {

    private final SourceCodeConfig[] sourceCodeConfigs;

    public CodebaseSearchOrchestrator(SourceCodeConfig[] sourceCodeConfigs) {
        this.sourceCodeConfigs = sourceCodeConfigs;
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
        SourceCode sourceCode = (SourceCode) platformContext;
        return sourceCode.getFileContent(file.getSelfLink());
    }

    @Override
    protected String getItemResourceKey(Object item) {
        return ((IFile) item).getSelfLink();
    }

    @Override
    public List<IFile> searchItemsWithKeywords(String keyword, Object platformContext, int itemsLimit) throws Exception {
        List<IFile> result = new ArrayList<>();
        List<SourceCode> sourceCodes = new SourceCodeFactory().createSourceCodes(sourceCodeConfigs);

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
        return sourceCodeConfigs;
    }
}