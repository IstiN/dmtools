package com.github.istin.dmtools.di;

import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.io.IOException;

@Module
public class ConfluenceModule {

    @Provides
    @Singleton
    Confluence provideConfluence() {
        try {
            return BasicConfluence.getInstance();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Confluence instance", e);
        }
    }
}