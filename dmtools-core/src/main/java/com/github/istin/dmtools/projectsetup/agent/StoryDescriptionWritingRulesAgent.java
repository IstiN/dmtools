package com.github.istin.dmtools.projectsetup.agent;

import com.github.istin.dmtools.ai.agent.AbstractSimpleAgent;
import com.github.istin.dmtools.ai.utils.AIResponseParser;
import com.github.istin.dmtools.di.DaggerStoryDescriptionWritingRulesAgentComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;

public class StoryDescriptionWritingRulesAgent extends AbstractSimpleAgent<StoryDescriptionWritingRulesAgent.Params, JSONObject> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String projectKey;
        private String storyDescriptionsData;
    }

    public StoryDescriptionWritingRulesAgent() {
        super("agents/story_description_writing_rules");
        DaggerStoryDescriptionWritingRulesAgentComponent.create().inject(this);
    }

    @Override
    public JSONObject transformAIResponse(Params params, String response) throws Exception {
        return AIResponseParser.parseResponseAsJSONObject(response);
    }
}
