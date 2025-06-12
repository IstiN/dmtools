package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.agent.RelatedTestCasesAgent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AIComponentsModule.class})
public interface RelatedTestCasesAgentComponent {
    void inject(RelatedTestCasesAgent relatedTestCasesAgent);
}