package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.ai.ChunkPreparation;
import java.io.IOException;

public interface IAgent<Params, Result> {

    Result run(Params params) throws Exception;

    String preparePrompt(Params params, int i, ChunkPreparation.Chunk chunk, int size);

    Result transformAIResponse(Params params, String response) throws Exception;

    Class<Params> getParamsClass();
}
