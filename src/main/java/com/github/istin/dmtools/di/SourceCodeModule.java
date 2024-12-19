package com.github.istin.dmtools.di;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.dev.CommitsTriageParams;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class SourceCodeModule {

    @Provides
    @Singleton
    SourceCodeFactory provideSourceCodeFactory() {
        return new SourceCodeFactory();
    }

    @Provides
    SourceCode provideSourceCode(SourceCodeFactory factory, CommitsTriageParams params) {
        return factory.createSourceCode(params.getSourceType());
    }
}