package com.github.istin.dmtools.ai.agent;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ChunkPreparation;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.prompt.PromptContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractSimpleAgent<Params, Result> implements IAgent<Params, Result> {

    public interface GetFiles {
        List<File> getFiles();
    }

    public interface GetChunks {
        List<ChunkPreparation.Chunk> getChunks();

        default long getChunksProcessingTimeout() {
            return 0;
        }
    }

    @Inject
    AI ai;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    @Getter
    private final String promptName;

    @Override
    public Result run(Params params) throws Exception {
        if (params instanceof GetChunks chunksParams) {
            List<ChunkPreparation.Chunk> chunks = chunksParams.getChunks();
            long chunksProcessingTimeout = chunksParams.getChunksProcessingTimeout();
            long started = System.currentTimeMillis();
            if (chunks != null && !chunks.isEmpty()) {
                if (chunks.size() > 1) {
                    StringBuilder chunksSummaries = new StringBuilder();
                    for (int i = 0; i < chunks.size(); i++) {
                        if (chunksProcessingTimeout == 0 || chunksProcessingTimeout + started < System.currentTimeMillis()) {
                            ChunkPreparation.Chunk chunk = chunks.get(i);
                            if (i != 0) {
                                chunksSummaries.append("\n");
                            }
                            Result result = processSinglePrompt(params, i + 1, chunk, chunks.size());
                            chunksSummaries.append(result);
                        } else {
                            System.out.println("Timeout of chunks processing was processed only " + (i+1) + " chunks from " + chunks.size());
                            break;
                        }
                    }
                    return processSinglePrompt(params, 1, new ChunkPreparation.Chunk(chunksSummaries.toString(), null, 0), 1);
                } else {
                    return processSinglePrompt(params, 1, chunks.getFirst(), 1);
                }
            } else {
                return processSinglePrompt(params, -1, null, 0);
            }
        } else {
            return processSinglePrompt(params, -1, null, 0);
        }
    }

    private Result processSinglePrompt(Params params, int i, ChunkPreparation.Chunk chunk, int size) throws Exception {
        String prompt = preparePrompt(params, i, chunk, size);
        String aiResponse = null;
        List<File> files = new ArrayList<>();
        if (chunk != null) {
            List<File> chunkFiles = chunk.getFiles();
            if (chunkFiles != null) {
                files.addAll(chunkFiles);
            }
        }

        if (params instanceof GetFiles) {
            List<File> paramFiles = ((GetFiles) params).getFiles();
            if (paramFiles != null) {
                files.addAll(paramFiles);
            }
        }
        try {
            aiResponse = executePrompt(files, prompt);
            return transformAIResponse(params, aiResponse);
        } catch (Exception e) {
            System.out.println("Wrong Response Format: \n" + aiResponse);
            aiResponse = executePrompt(files, prompt + "\n Your previous response was: " + aiResponse +  "\nDuring processing of response was error: "+e.getMessage()+"\nReturn fixed response. If it was cut because of limitation, you must cut last part (last item in JSONArray) and return valid object.");
            return transformAIResponse(params, aiResponse);
        }
    }

    private String executePrompt(List<File> files, String prompt) throws Exception {
        String aiResponse;
        if (!files.isEmpty()) {
            aiResponse = ai.chat(null, prompt, files);
        } else {
            aiResponse = ai.chat(prompt);
        }
        return aiResponse;
    }

    public String preparePrompt(Params params, int i, ChunkPreparation.Chunk chunk, int size) {
        try {
            PromptContext context = new PromptContext(params);
            context.set("chunkIndex", i);
            context.set("totalChunks", size);
            context.set("chunk", chunk);
            return promptTemplateReader.read(promptName, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
