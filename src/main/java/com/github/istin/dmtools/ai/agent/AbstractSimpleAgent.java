package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.prompt.PromptContext;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;

@RequiredArgsConstructor
public abstract class AbstractSimpleAgent<Params, Result> implements IAgent<Params, Result> {

    @Inject
    AI ai;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    private final String promptName;

    @Override
    public Result run(Params params) throws Exception {
        String prompt = promptTemplateReader.read(promptName, new PromptContext(params));
        String aiResponse = ai.chat(prompt);
        return transformAIResponse(params, aiResponse);
    }

    abstract Result transformAIResponse(Params params, String response) throws Exception;

}
