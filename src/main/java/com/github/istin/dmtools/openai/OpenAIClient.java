package com.github.istin.dmtools.openai;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.model.Metadata;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.utils.ImageUtils;
import com.github.istin.dmtools.common.utils.RetryUtil;
import com.github.istin.dmtools.networking.AbstractRestClient;
import com.github.istin.dmtools.openai.model.AIResponse;
import com.github.istin.dmtools.openai.model.Choice;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OpenAIClient extends AbstractRestClient implements AI {

    private static final Logger logger = LogManager.getLogger(OpenAIClient.class);

    private final String model;

    @Setter
    private Metadata metadata;

    @Getter
    @Setter
    private ConversationObserver conversationObserver;

    public OpenAIClient(String basePath, String authorization, String model) throws IOException {
        this(basePath, authorization, model, null);
    }

    public OpenAIClient(String basePath, String authorization, String model, ConversationObserver conversationObserver) throws IOException {
        super(basePath, authorization);
        this.model = model;
        this.conversationObserver = conversationObserver;
        setCachePostRequestsEnabled(true);
    }

    public String getName() {
        return model;
    }

    @Override
    public String path(String path) {
        return getBasePath() + path;// + "?api-version=" + API_VERSION;
    }

    @Override
    public Request.Builder sign(Request.Builder builder) {
        return builder
                .header("api-key", authorization)
                .header("Content-Type", "application/json");
    }

    @Override
    public int getTimeout() {
        return 700;
    }

    @Override
    public String chat(String message) throws Exception {
        return chat(model, message);
    }

    @Override
    public String chat(String model, String message, File imageFile) throws Exception {
        if (model == null) {
            model = this.model;
        }
        logger.info("-------- message to ai --------");
        logger.info(message);
        logger.info("-------- start chat ai --------");
        if (conversationObserver != null) {
            conversationObserver.addMessage(new ConversationObserver.Message("DMTools", message));
        }
        String path = path("openai/deployments/" + model + "/chat/completions");
        logger.info(path);
        GenericRequest postRequest = new GenericRequest(this, path);
//        postRequest.setIgnoreCache(true);

        JSONArray messages = new JSONArray();;
        if (imageFile != null) {
            String extension = ImageUtils.getExtension(imageFile);
            String imageBase64 = ImageUtils.convertToBase64(imageFile, "png");
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", new JSONArray()
                            .put(new JSONObject()
                                    .put("type", "text")
                                    .put("text", message)
                            )
                            .put(new JSONObject()
                                    .put("type", "image_url")
                                    .put("image_url", new JSONObject()
                                            .put("url", "data:image/"+extension+";base64," + imageBase64))
                            )
                    ));
        } else {
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", message));
        }
        JSONObject jsonObject = new JSONObject()
                .put("temperature", 0.1)
                .put("messages", messages);
        if (metadata != null) {
            jsonObject.put("metadata", new JSONObject(new Gson().toJson(metadata)));
        }
        postRequest.setBody(jsonObject.toString());
        String finalModel = model;
        return RetryUtil.executeWithRetry(() -> processResponse(finalModel, postRequest));
    }

    private String processResponse(String model, GenericRequest postRequest) throws IOException {
        String response = post(postRequest);
        logger.info(response);
        List<Choice> choices = new AIResponse(response).getChoices();
        String content;
        if (choices.isEmpty()) {
            if (response.contains("error")) {
                logger.error(response);
                content = response;
            } else {
                content = "";
            }
        } else {
            content = choices.getFirst().getMessage().getContent();
        }
        if (conversationObserver != null) {
            conversationObserver.addMessage(new ConversationObserver.Message(model, content));
        }
        logger.info("-------- ai response --------");
        logger.info(content);
        logger.info("-------- end chat ai --------");
        return content;
    }

    @Override
    public String chat(String model, String message, List<File> files) throws Exception {
        return chat(model, message, files != null && !files.isEmpty() ? files.getFirst() : null);
    }


    public String chat(String model, String message) throws Exception {
        return chat(model, message, (File) null);
    }

    @Override
    protected @NotNull String buildHashForPostRequest(GenericRequest genericRequest, String url) {
        return url + genericRequest.getBody();
    }

}
