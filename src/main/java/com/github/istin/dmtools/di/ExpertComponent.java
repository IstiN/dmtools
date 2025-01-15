package com.github.istin.dmtools.di;

import com.github.istin.dmtools.expert.Expert;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {JiraModule.class, SourceCodeModule.class, AIComponentsModule.class, ConfluenceModule.class, AIAgentsModule.class})
public interface ExpertComponent {
    void inject(Expert expert);
}
