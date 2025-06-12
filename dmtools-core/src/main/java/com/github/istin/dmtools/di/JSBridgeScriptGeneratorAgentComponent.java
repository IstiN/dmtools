package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.agent.JSBridgeScriptGeneratorAgent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AIComponentsModule.class})
public interface JSBridgeScriptGeneratorAgentComponent {
    void inject(JSBridgeScriptGeneratorAgent jsBridgeScriptGeneratorAgent);
} 