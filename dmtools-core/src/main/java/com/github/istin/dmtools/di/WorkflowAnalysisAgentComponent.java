package com.github.istin.dmtools.di;

import com.github.istin.dmtools.projectsetup.agent.WorkflowAnalysisAgent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ConfigurationModule.class, AIComponentsModule.class})
public interface WorkflowAnalysisAgentComponent {
    void inject(WorkflowAnalysisAgent workflowAnalysisAgent);
}
