package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.prompt.PromptContext;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractSimpleAgent<Params, Result> implements IAgent<Params, Result> {

    public interface GetFiles {
        List<File> getFiles();
    }

    @Inject
    AI ai;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    private final String promptName;

    @Override
    public Result run(Params params) throws Exception {
        String prompt = promptTemplateReader.read(promptName, new PromptContext(params));
        String aiResponse = null;
        if (params instanceof GetFiles) {
            aiResponse = ai.chat(null, prompt, ((GetFiles) params).getFiles());
        } else {
            aiResponse = ai.chat(prompt);
        }
        return transformAIResponse(params, aiResponse);
    }

    abstract Result transformAIResponse(Params params, String response) throws Exception;

}
