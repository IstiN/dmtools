package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.agent.RequestSimplifierAgent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AIComponentsModule.class})
public interface RequestSimplifierAgentComponent {
    void inject(RequestSimplifierAgent requestSimplifierAgent);
}