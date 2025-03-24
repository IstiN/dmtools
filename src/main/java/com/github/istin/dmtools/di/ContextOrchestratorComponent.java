package com.github.istin.dmtools.di;

import com.github.istin.dmtools.context.ContextOrchestrator;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AIAgentsModule.class})
public interface ContextOrchestratorComponent {
    void inject(ContextOrchestrator contextOrchestrator);
}