package com.github.istin.dmtools.prompt;

public interface IPromptTemplateReader {

    String read(String promptName, PromptContext context) throws Exception;

}
