package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.di.DaggerTeamAssistantAgentComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class TeamAssistantAgent extends AbstractSimpleAgent<TeamAssistantAgent.Params, String> {

    @AllArgsConstructor
    @Getter
    public static class Params {
        private String projectContext;
        private String request;
        private String ticketContent;
        private String previousDiscussion;
    }

    public TeamAssistantAgent() {
        super("agents/team_assistant");
        DaggerTeamAssistantAgentComponent.create().inject(this);
    }

    @Override
    String transformAIResponse(Params params, String response) throws Exception {
        // The response is already in HTML format as per prompt requirements
        return response;
    }
}