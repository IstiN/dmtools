package com.github.istin.dmtools.di;

import com.github.istin.dmtools.common.code.SourceCode;
import org.json.JSONArray;

import java.io.IOException;

public class SourceCodeFactory {

    public SourceCode createSourceCode(String sourceType) {
        // Assuming a method is available to configure SourceCode with a certain sourceType
        try {
            return SourceCode.Impl.getConfiguredSourceCodes(new JSONArray().put(sourceType)).get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}