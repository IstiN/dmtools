package com.github.istin.dmtools.di;

import javax.inject.Singleton;

import com.github.istin.dmtools.codereview.GuidelinesAgent;

import dagger.Component;

@Singleton
@Component(modules = { SourceCodeModule.class, AIComponentsModule.class })
public interface GuidelinesAgentComponent {
    void inject(GuidelinesAgent guidelinesAgent);
}
