package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import dagger.Module;
import dagger.Provides;

import java.io.IOException;

@Module
public class AIComponentsModule {

    @Provides
    ConversationObserver provideConversationObserver() {
        return new ConversationObserver();
    }

    @Provides
    AI provideAI(ConversationObserver observer) {
        try {
            return new BasicOpenAI(observer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    IPromptTemplateReader providePromptTemplateReader() {
        return new PromptManager();
    }

}