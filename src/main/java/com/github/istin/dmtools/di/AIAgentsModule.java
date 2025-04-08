package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.agent.*;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.context.ContextOrchestrator;
import com.github.istin.dmtools.presentation.PresentationMakerOrchestrator;
import com.github.istin.dmtools.search.ConfluenceSearchOrchestrator;
import com.github.istin.dmtools.search.TrackerSearchOrchestrator;
import dagger.Module;
import dagger.Provides;

import java.io.IOException;

@Module
public class AIAgentsModule {

    @Provides
    SourceImpactAssessmentAgent provideSourceImpactAssessmentAgent() {
        return new SourceImpactAssessmentAgent();
    }

    @Provides
    KeywordGeneratorAgent provideKeywordGeneratorAgent() {
        return new KeywordGeneratorAgent();
    }

    @Provides
    SummaryContextAgent provideSummaryContextAgent() {
        return new SummaryContextAgent();
    }

    @Provides
    SnippetExtensionAgent provideSnippetExtensionAgent() {
        return new SnippetExtensionAgent();
    }

    @Provides
    RequestDecompositionAgent provideRequestSimplifierAgent() {
        return new RequestDecompositionAgent();
    }

    @Provides
    TestCaseGeneratorAgent provideTestCaseGeneratorAgent() {
        return new TestCaseGeneratorAgent();
    }

    @Provides
    RelatedTestCasesAgent provideRelatedTestCasesAgent() {
        return new RelatedTestCasesAgent();
    }

    @Provides
    RelatedTestCaseAgent provideRelatedTestCaseAgent() {
        return new RelatedTestCaseAgent();
    }

    @Provides
    TeamAssistantAgent provideTeamAssistantAgent() {
        return new TeamAssistantAgent();
    }

    @Provides
    SearchResultsAssessmentAgent provideSearchResultsAssessmentAgent() {
        return new SearchResultsAssessmentAgent();
    }

    @Provides
    TestCaseVisualizerAgent provideTestCaseVisualizerAgent() {
        return new TestCaseVisualizerAgent();
    }

    @Provides
    TaskExecutionAgent provideTaskExecutionAgent() {
        return new TaskExecutionAgent();
    }

    @Provides
    TaskProgressAgent provideTaskProgressAgent() {
        return new TaskProgressAgent();
    }

    @Provides
    ContentMergeAgent provideContentMergeAgent() {
        return new ContentMergeAgent();
    }

    @Provides
    ConfluenceSearchOrchestrator provideConfluenceSearchOrchestrator() {
        try {
            return new ConfluenceSearchOrchestrator(BasicConfluence.getInstance());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    TrackerSearchOrchestrator provideTrackerSearchOrchestrator() {
        try {
            return new TrackerSearchOrchestrator(BasicJiraClient.getInstance());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    ContextOrchestrator provideContextOrchestrator() {
        return new ContextOrchestrator();
    }

    @Provides
    PresentationMakerOrchestrator providePresentationMakerOrchestrator() {
        return new PresentationMakerOrchestrator();
    }

}
