package com.github.istin.dmtools.di;

import javax.inject.Singleton;

import com.github.istin.dmtools.codereview.CodeReviewAgent;

import dagger.Component;

@Singleton
@Component(modules = { SourceCodeModule.class, AIComponentsModule.class })
public interface CodeReviewAgentComponent {
    void inject(CodeReviewAgent codeReviewAgent);
}
