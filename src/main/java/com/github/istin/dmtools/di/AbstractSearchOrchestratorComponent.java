package com.github.istin.dmtools.di;

import com.github.istin.dmtools.search.AbstractSearchOrchestrator;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AIAgentsModule.class})
public interface AbstractSearchOrchestratorComponent {
    void inject(AbstractSearchOrchestrator searchOrchestrator);
}