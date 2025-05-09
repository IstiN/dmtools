package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.ai.ChunkPreparation;
import com.github.istin.dmtools.di.DaggerTeamAssistantAgentComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.List;

public class TeamAssistantAgent extends AbstractSimpleAgent<TeamAssistantAgent.Params, String> {

    @AllArgsConstructor
    @Getter
    public static class Params implements GetFiles, GetChunks {
        private RequestDecompositionAgent.Result request;
        private List<File> files;
        private List<ChunkPreparation.Chunk> chunks;
        private long chunksProcessingTimeout;

        public Params(RequestDecompositionAgent.Result request) {
            this.request = request;
        }

        public Params(RequestDecompositionAgent.Result request, List<File> files) {
            this.request = request;
            this.files = files;
        }

        public Params(RequestDecompositionAgent.Result request, List<File> files, List<ChunkPreparation.Chunk> chunks) {
            this.request = request;
            this.files = files;
            this.chunks = chunks;
        }
    }

    public TeamAssistantAgent() {
        super("agents/team_assistant");
        DaggerTeamAssistantAgentComponent.create().inject(this);
    }

    @Override
    public String transformAIResponse(Params params, String response) throws Exception {
        // The response is already in HTML format as per prompt requirements
        return response;
    }
}