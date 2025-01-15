package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.agent.*;
import dagger.Module;
import dagger.Provides;

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
    RequestSimplifierAgent provideRequestSimplifierAgent() {
        return new RequestSimplifierAgent();
    }

}
