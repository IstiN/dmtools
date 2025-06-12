package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.agent.ToolSelectorAgent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AIComponentsModule.class})
public interface ToolSelectorAgentComponent {
    void inject(ToolSelectorAgent toolSelectorAgent);
} 