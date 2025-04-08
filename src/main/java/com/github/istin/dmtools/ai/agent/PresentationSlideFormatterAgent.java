package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.di.DaggerPresentationSlideFormatterAgentComponent;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONArray;

public class PresentationSlideFormatterAgent extends AbstractSimpleAgent<PresentationSlideFormatterAgent.Params, JSONArray> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private JSONArray slidesContent;
    }

    public PresentationSlideFormatterAgent() {
        super("agents/presentation_slide_formatter");
        DaggerPresentationSlideFormatterAgentComponent.create().inject(this);
    }

    @Override
    public JSONArray transformAIResponse(Params params, String response) throws Exception {
        return AIResponseParser.parseResponseAsJSONArray(response);
    }
}