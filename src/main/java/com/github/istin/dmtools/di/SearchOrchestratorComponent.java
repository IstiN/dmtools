package com.github.istin.dmtools.di;

import com.github.istin.dmtools.search.SearchOrchestrator;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AIAgentsModule.class})
public interface SearchOrchestratorComponent {
    void inject(SearchOrchestrator searchOrchestrator);
}