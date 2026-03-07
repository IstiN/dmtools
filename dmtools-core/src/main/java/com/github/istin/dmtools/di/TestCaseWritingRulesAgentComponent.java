package com.github.istin.dmtools.di;

import com.github.istin.dmtools.projectsetup.agent.TestCaseWritingRulesAgent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ConfigurationModule.class, AIComponentsModule.class})
public interface TestCaseWritingRulesAgentComponent {
    void inject(TestCaseWritingRulesAgent testCaseWritingRulesAgent);
}
