package com.github.istin.dmtools.di;

import com.github.istin.dmtools.projectsetup.agent.FinalStatusDetectionAgent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ConfigurationModule.class, AIComponentsModule.class})
public interface FinalStatusDetectionAgentComponent {
    void inject(FinalStatusDetectionAgent finalStatusDetectionAgent);
}
