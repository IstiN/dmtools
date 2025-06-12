package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.agent.PresentationContentGeneratorAgent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AIComponentsModule.class})
public interface PresentationContentGeneratorAgentComponent {
    void inject(PresentationContentGeneratorAgent presentationContentGeneratorAgent);
}